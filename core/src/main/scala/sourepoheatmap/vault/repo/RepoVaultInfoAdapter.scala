/*
 * Copyright (c) 2015, 2016 Alexey Kuzin <amkuzink@gmail.com>
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

package sourepoheatmap.vault.repo

import sourepoheatmap.vault.{VcsMatch, VaultInfoAdapter}
import sourepoheatmap.vault.VaultInfoAdapter.{VaultException, VaultType}
import sourepoheatmap.vault.VaultInfoAdapter.VaultType.VaultType

import scala.io.Source
import scala.reflect.io.File

/** Class for providing ability to get information from a Repo repository.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
private[vault] class RepoVaultInfoAdapter(path: String) extends VaultInfoAdapter {
  require(!path.isEmpty)

  private val mVaults: Map[String, VaultInfoAdapter] = {
    val vaultAdapters = for (
      gitVaultPath <- Source.fromFile(RepoVaultInfoAdapter.repoListFilename(path)).getLines();
      absVaultPath = path + File.separator + gitVaultPath
    ) yield absVaultPath -> VaultInfoAdapter(absVaultPath).getOrElse(
        throw new VaultException("Unknown repository type"))
    vaultAdapters.toMap
  }

  private var mCurrentVault: (String, VaultInfoAdapter) = mVaults.head

  override def terminate(): Unit = mVaults.foreach(_._2.terminate())

  override def getVaultType: VaultType = VaultType.Repo

  override def switchVault(path: String): VaultInfoAdapter = mCurrentVault._2

  override def getCurrentBranchName: String = mCurrentVault._1

  override def getBranches: List[String] = mVaults.keys.toList

  override def switchBranch(branch: String): Unit = {
    mCurrentVault = branch -> mVaults(branch)
  }

  override def getCommitIdUntil(until: Int): Option[String] =
    mCurrentVault._2.getCommitIdUntil(until)

  override def getCommitIdsBetween(since: Int, until: Int): List[String] =
    mCurrentVault._2.getCommitIdsBetween(since, until)

  override def getCommitIdAfter(since: Int): Option[String] =
    mCurrentVault._2.getCommitIdAfter(since)

  override def getAddedCount(commitId: String): Map[String, Int] =
    mCurrentVault._2.getAddedCount(commitId)

  override def getAddedCount(oldCommitId: String, newCommitId: String): Map[String, Int] =
    mCurrentVault._2.getAddedCount(oldCommitId, newCommitId)

  override def getRemovedCount(commitId: String): Map[String, Int] =
    mCurrentVault._2.getRemovedCount(commitId)

  override def getRemovedCount(oldCommitId: String, newCommitId: String): Map[String, Int] =
    mCurrentVault._2.getRemovedCount(oldCommitId, newCommitId)

  override def getDiff(commitId: String): List[String] =
    mCurrentVault._2.getDiff(commitId)

  override def getDiff(oldCommitId: String, newCommitId: String): List[String] =
    mCurrentVault._2.getDiff(oldCommitId, newCommitId)

  override def getChangedCount(commitId: String): Map[String, Int] =
    mCurrentVault._2.getChangedCount(commitId)

  override def getChangedCount(oldCommitId: String, newCommitId: String): Map[String, Int] =
    mCurrentVault._2.getChangedCount(oldCommitId, newCommitId)
}

object RepoVaultInfoAdapter extends VcsMatch {
  private val REPO_DIR_NAME = ".repo"
  private val REPO_LIST_FILE_NAME = "project.list"

  private def repoListFilename(repoDirPath: String): String =
    new StringBuilder(repoDirPath).
      append(File.separator).
      append(REPO_DIR_NAME).
      append(File.separator).
      append(REPO_LIST_FILE_NAME).
      toString()

  override def unapply(path: String): Boolean = {
    File(repoListFilename(path)).exists
  }
}
