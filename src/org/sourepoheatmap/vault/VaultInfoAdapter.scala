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

package org.sourepoheatmap.vault

import org.sourepoheatmap.vault.VaultInfoAdapter.VaultType.VaultType
import org.sourepoheatmap.vault.git.GitVaultInfoAdapter
import org.sourepoheatmap.vault.repo.RepoVaultInfoAdapter

/** Trait for providing ability to get information from a repository.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
trait VaultInfoAdapter {
  def terminate(): Unit

  def getVaultType: VaultType

  def switchVault(path: String): VaultInfoAdapter

  def getCurrentBranchName: String
  def getBranches: List[String]
  def switchBranch(branch: String): Unit

  def getCommitIdAfter(since: Int): Option[String]
  def getCommitIdUntil(until: Int): Option[String]
  def getCommitIdsBetween(since: Int, until: Int): List[String]

  def getDiff(commitId: String): List[String]
  def getDiff(oldCommitId: String, newCommitId: String): List[String]

  def getAddedCount(commitId: String): Map[String, Int]
  def getAddedCount(oldCommitId: String, newCommitId: String): Map[String, Int]
  def getRemovedCount(commitId: String): Map[String, Int]
  def getRemovedCount(oldCommitId: String, newCommitId: String): Map[String, Int]
  def getChangedCount(commitId: String): Map[String, Int]
  def getChangedCount(oldCommitId: String, newCommitId: String): Map[String, Int]
}

object VaultInfoAdapter {
  class VaultException(msg: String) extends Exception(msg)

  object VaultType extends Enumeration {
    type VaultType = Value
    val Git, Repo = Value
  }

  def apply(vaultPath: String): Option[VaultInfoAdapter] = vaultPath match {
    case GitVaultInfoAdapter() => Some(new GitVaultInfoAdapter(vaultPath))
    case RepoVaultInfoAdapter() => Some(new RepoVaultInfoAdapter(vaultPath))
    case _ => None
  }
}
