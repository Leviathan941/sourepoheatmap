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

package org.sourepoheatmap.repository

import java.io.{IOException, File}

import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.{ListBranchCommand, Git}
import org.eclipse.jgit.lib.{Ref => JGitRef, Repository}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.sourepoheatmap.repository.GitVaultInfoAdapter.GitVaultException

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Class for providing ability to get information from a repository.
 *
 * @author Alexey Kuzin <amkuzink@gmail.com>
 */
class GitVaultInfoAdapter(repoPath: String) {
  require(!repoPath.isEmpty)

  private val mRepo: Repository =
    try {
      new FileRepositoryBuilder().readEnvironment().findGitDir(new File(repoPath))
        .build()
    } catch {
      case ex: java.lang.Exception => throw new GitVaultException("Failed to find Git repository " + repoPath)
    }

  def getHeadBranchFullName(): String =
    getHeadBranch(_.getFullBranch)

  def getHeadBranchName(): String =
    getHeadBranch(_.getBranch)

  private def getHeadBranch(getBranchName: Repository => String): String = {
    mRepo.incrementOpen

    try
      getBranchName(mRepo)
    catch {
      case ex: IOException => throw new GitVaultException("Failed to get HEAD branch name: " + ex.getMessage)
    } finally mRepo.close
  }

  def getLocalBranches(): List[String] =
    getBranches()

  def getRemoteBranches(): List[String] =
    getBranches(_.setListMode(ListBranchCommand.ListMode.REMOTE))

  def getAllBranches(): List[String] =
    getBranches(_.setListMode(ListBranchCommand.ListMode.ALL))

  private def getBranches(getBranchList: ListBranchCommand => ListBranchCommand = (cmd => cmd)): List[String] = {
    mRepo.incrementOpen
    try {
      val git = new Git(mRepo)
      val branchList: mutable.Buffer[JGitRef] = getBranchList(git.branchList).call
      for (branch <- branchList.toList) yield branch.getName
    } catch {
      case ex: GitAPIException => throw new GitVaultException("Failed to get branches: " + ex.getMessage)
    } finally mRepo.close
  }

  def getCommitIdAfter(since: Int): String = {
    getCommitId(_.getCommitTime >= since, _(0))
  }

  def getCommitIdUntil(until: Int): String = {
    getCommitId(_.getCommitTime <= until, _.reverse(0))
  }

  private def getCommitId(commitCondition: RevCommit => Boolean,
                          selectElement: List[String] => String): String = {
    val commits = getCommitIds(commitCondition)
    if (commits.size == 0) "" else selectElement(commits)
  }

  def getCommitIdsBetween(since: Int, until: Int): List[String] = {
    getCommitIds(commit => (commit.getCommitTime >= since && commit.getCommitTime <= until))
  }

  private def getCommitIds(commitCondition: RevCommit => Boolean): List[String] = {
    mRepo.incrementOpen
    try {
      val git = new Git(mRepo)
      val commitsBetween = git.log.call.toList
        .filter(commitCondition(_))
        .sortWith(_.getCommitTime < _.getCommitTime)
      for (commit <- commitsBetween) yield
        commit.getName
    } catch {
      case ex: java.lang.Exception => throw new GitVaultException("Failed to get commit IDs: " + ex.getMessage)
    } finally mRepo.close()
  }

//  def printDiff(): Unit = {
//    mRepo.incrementOpen
//
//    val reader = mRepo.newObjectReader()
//    val head = mRepo.resolve(Constants.HEAD)
//    val preHead = mRepo.resolve(Constants.HEAD + "~1")
//    val out = new ByteArrayOutputStream
//    val df = new DiffFormatter(out)
//    df.setRepository(mRepo)
//    val entries = df.scan(preHead, head)
//    for (entry <- entries) {
//      df.setContext(0)
//      df.format(entry)
//
//      println(out.toString)
//      out.reset()
//    }
//  }
}

object GitVaultInfoAdapter {
  class GitVaultException(msg: String) extends Exception(msg)
}
