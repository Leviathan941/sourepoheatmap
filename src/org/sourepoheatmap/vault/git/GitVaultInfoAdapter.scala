/*
 * Copyright (c) 2015, Alexey Kuzin <amkuzink@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sourepoheatmap.vault.git

import java.io.{ByteArrayOutputStream, File, IOException}

import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.{Git, ListBranchCommand}
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.{AnyObjectId, AbbreviatedObjectId, Repository}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.{AbstractTreeIterator, CanonicalTreeParser, EmptyTreeIterator}
import org.sourepoheatmap.vault.git.GitDiffParser.{DeletedLine, AddedLine, LineChange, FileDiff}
import org.sourepoheatmap.vault.{VcsMatch, VaultInfoAdapter}
import org.sourepoheatmap.vault.VaultInfoAdapter.VaultException

import scala.collection.JavaConversions._

/** Class for providing ability to get information from a Git repository.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class GitVaultInfoAdapter(path: String) extends VaultInfoAdapter {
  require(!path.isEmpty)

  private val mRepo: Repository =
    try {
      new FileRepositoryBuilder().readEnvironment().findGitDir(new File(path))
        .build()
    } catch {
      case ex: java.lang.Exception => throw new VaultException("Failed to find Git repository %s.\n%s".format(
        path, ex.getMessage))
    }
  println(mRepo.getDirectory.getPath)

  def terminate(): Unit = {
    mRepo.close()
  }

  def getCurrentBranchName: String =
    getHeadBranch(_.getFullBranch)

  def getCurrentBranchShortenName: String =
    getHeadBranch(_.getBranch)

  private def getHeadBranch(getBranchName: Repository => String): String = {
    mRepo.incrementOpen()
    try
      getBranchName(mRepo)
    catch {
      case ex: IOException => throw new VaultException("Failed to get HEAD branch name: " + ex.getMessage)
    } finally mRepo.close()
  }

  def getLocalBranches: List[String] =
    getBranches()

  def getRemoteBranches: List[String] =
    getBranches(_.setListMode(ListBranchCommand.ListMode.REMOTE))

  def getBranches: List[String] =
    getBranches(_.setListMode(ListBranchCommand.ListMode.ALL))

  private def getBranches(getBranchList: ListBranchCommand => ListBranchCommand = cmd => cmd): List[String] = {
    mRepo.incrementOpen()
    try {
      val git = new Git(mRepo)
      val branchList = getBranchList(git.branchList).call.toList
      for (branch <- branchList) yield branch.getName
    } catch {
      case ex: GitAPIException => throw new VaultException("Failed to get branches: " + ex.getMessage)
    } finally mRepo.close()
  }

  def getCommitIdAfter(since: Int): Option[String] = {
    getCommitId(_.getCommitTime >= since, _.head)
  }

  def getCommitIdUntil(until: Int): Option[String] = {
    getCommitId(_.getCommitTime <= until, _.reverse.head)
  }

  private def getCommitId(commitCondition: RevCommit => Boolean,
                          selectElement: List[String] => String): Option[String] = {
    val commits = getCommitIds(commitCondition)
    if (commits.isEmpty) None else Some(selectElement(commits))
  }

  def getCommitIdsBetween(since: Int, until: Int): List[String] = {
    require(until >= since, "'until' time should be greater than 'since'")
    getCommitIds(commit => commit.getCommitTime >= since && commit.getCommitTime <= until)
  }

  private def getCommitIds(commitCondition: RevCommit => Boolean): List[String] = {
    mRepo.incrementOpen()
    try {
      val git = new Git(mRepo)
      val commitsBetween = git.log.call.toList
        .filter(commitCondition)
        .sortWith(_.getCommitTime < _.getCommitTime)
      for (commit <- commitsBetween) yield
        commit.getName
    } catch {
      case ex: java.lang.Exception => throw new VaultException("Failed to get commit IDs: " + ex.getMessage)
    } finally mRepo.close()
  }

  def getDiff(commitId: String): List[String] =
    getDiffFormatted(commitId, _ => ())

  def getDiff(oldCommitId: String, newCommitId: String): List[String] =
    getDiffFormatted(oldCommitId, newCommitId, _ => ())

  private def getDiffFormatted(commitId: String, formatConfig: DiffFormatter => Unit): List[String] = {
    requireValidCommitHexes(commitId)

    getDiffFormattedImpl(
      getTreeIterator(commitId, getParentTreeIterator),
      getTreeIterator(commitId, getCanonicalTreeIterator),
      formatConfig
    )
  }

  private def getDiffFormatted(
    oldCommitId: String,
    newCommitId: String,
    formatConfig: DiffFormatter => Unit
  ): List[String] = {
    requireValidCommitHexes(oldCommitId, newCommitId)

    getDiffFormattedImpl(
      getTreeIterator(oldCommitId, getCanonicalTreeIterator),
      getTreeIterator(newCommitId, getCanonicalTreeIterator),
      formatConfig
    )
  }

  private def getDiffFormattedImpl(
    oldTreeIter: RevWalk => AbstractTreeIterator,
    newTreeIter: RevWalk => AbstractTreeIterator,
    formatConfig: DiffFormatter => Unit
  ): List[String] = {

    mRepo.incrementOpen()
    val revWalk = new RevWalk(mRepo)
    val outStream = new ByteArrayOutputStream
    val diffFormatter = new DiffFormatter(outStream)
    try {
      diffFormatter.setRepository(mRepo)
      formatConfig(diffFormatter)

      val diffEntries = diffFormatter.scan(
        oldTreeIter(revWalk),
        newTreeIter(revWalk)
      ).toList

      for (entry <- diffEntries) yield {
        outStream.reset()
        diffFormatter.format(entry)
        outStream.toString
      }
    } catch {
      case ex: java.lang.Exception => throw new VaultException("Failed to get difference: " + ex.getMessage)
    } finally {
      diffFormatter.close()
      revWalk.close()
      mRepo.close()
    }
  }

  def getAddedCount(commitId: String): Int =
    countDiffLines(commitId, addedFilter)

  def getAddedCount(oldCommitId: String, newCommitId: String): Int =
    countDiffLinesBetween(oldCommitId, newCommitId, addedFilter)

  def getRemovedCount(commitId: String): Int =
    countDiffLines(commitId, removedFilter)

  def getRemovedCount(oldCommitId: String, newCommitId: String): Int =
    countDiffLinesBetween(oldCommitId, newCommitId, removedFilter)

  def getChangedCount(commitId: String): Int =
    countDiffLines(commitId, changedFilter)

  def getChangedCount(oldCommitId: String, newCommitId: String): Int =
    countDiffLinesBetween(oldCommitId, newCommitId, changedFilter)

  private def addedFilter(line: LineChange): Boolean = line match {
    case AddedLine(_) => true
    case _ => false
  }

  private def removedFilter(line: LineChange): Boolean = line match {
    case DeletedLine(_) => true
    case _ => false
  }

  private def changedFilter(line: LineChange): Boolean = {
    addedFilter(line) || removedFilter(line)
  }

  private def countDiffLines(commitId: String, countFilter: LineChange => Boolean): Int = {
    val gitDiff = getDiffFormatted(
      commitId,
      df => { df.setContext(0); df.setDetectRenames(false) }
    )
    countDiffLinesImpl(GitDiffParser(gitDiff.mkString), countFilter)
  }

  private def countDiffLinesBetween(oldCommitId: String, newCommitId: String, countFilter: LineChange => Boolean) = {
    val gitDiff = getDiffFormatted(
      oldCommitId,
      newCommitId,
      df => { df.setContext(0); df.setDetectRenames(false) }
    )
    countDiffLinesImpl(GitDiffParser(gitDiff.mkString), countFilter)
  }

  private def countDiffLinesImpl(filesDiff: List[FileDiff], countFilter: LineChange => Boolean): Int = {
    var count = 0
    for (
      fileDiff <- filesDiff;
      GitDiffParser.TextChunk(_, _, lines) <- fileDiff.chunks
    ) count += lines.count(countFilter)
    count
  }

  private def getTreeIterator(commitId: String, treeIter: (AnyObjectId, RevWalk) => AbstractTreeIterator)
      (revWalk: RevWalk): AbstractTreeIterator =
    treeIter(mRepo.resolve(commitId), revWalk)

  private def getParentTreeIterator(commitObjId: AnyObjectId, revWalk: RevWalk): AbstractTreeIterator = {
    val commit = revWalk.parseCommit(commitObjId)
    if (commit.getParentCount > 0)
      getCanonicalTreeIterator(commit.getParent(0).getId, revWalk)
    else
      new EmptyTreeIterator
  }

  private def getCanonicalTreeIterator(commitObjId: AnyObjectId, revWalk: RevWalk): AbstractTreeIterator =
    new CanonicalTreeParser(null, revWalk.getObjectReader, revWalk.parseTree(commitObjId))

  private def requireValidCommitHexes(hexes: String*): Unit = {
    for (hex <- hexes)
      require(AbbreviatedObjectId.isId(hex), "Argument should be valid commit hex")
  }
}

object GitVaultInfoAdapter extends VcsMatch {
  override def unapply(path: String): Boolean =
    new FileRepositoryBuilder().readEnvironment().findGitDir(new File(path)).getGitDir != null
}
