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

package sourepoheatmap.application.cli

import sourepoheatmap.vault.VaultInfoAdapter
//import sourepoheatmap.vault.git.GitDiffParser

/** Placeholder for future command line frontend.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
object CliApplication {
  def main(args: Array[String]) {
    val Some(vaultAdapter) = VaultInfoAdapter("/home/leviathan/projects/melange")
    println("Vault type = " + vaultAdapter.getVaultType)

//    testRepoVaultInfoAdapter(vaultAdapter)
//    testGitVaultInfoAdapter(vaultAdapter)
  }

  private def testRepoVaultInfoAdapter(vaultAdapter: VaultInfoAdapter): Unit = {
    println("Branches:")
    vaultAdapter.getBranches.foreach(println)
  }

  private def testGitVaultInfoAdapter(vaultAdapter: VaultInfoAdapter): Unit = {
    vaultAdapter.getBranches.foreach(println)
    println()
    println("Current branch before = " + vaultAdapter.getCurrentBranchName)
    vaultAdapter.switchBranch("refs/heads/master")
    println("Current branch after = " + vaultAdapter.getCurrentBranchName)

    println()
    vaultAdapter.getCommitIdsBetween(1390471304, 1424523705).foreach(println)

    println()
    vaultAdapter.getCommitIdAfter(1390471304) match {
      case Some(s) => println("Commit id after: " + s)
      case None => println("No such commit")
    }
    println()

    val lastCommit = vaultAdapter.getCommitIdUntil(1424523705)
    lastCommit match {
      case Some(s) => println("Commit id until: " + s)
      case None => println("No such commit")
    }
    println("\n")

    val diff1 = vaultAdapter.getDiff("0ba64d027223")
    diff1.foreach(println)

    val diff2 = vaultAdapter.getDiff("4bd442c137fc0", "0ba64d027223")
    diff2.foreach(println)
    //    GitDiffParser(diff2.mkString).foreach(printGitDiff)

    println("\n")
    println("Added lines in %s:".format("0ba64d027223"))
    vaultAdapter.getAddedCount("0ba64d027223").foreach(println)

    println("Removed lines in %s:".format("0ba64d027223"))
    vaultAdapter.getRemovedCount("0ba64d027223").foreach(println)

    println("Changed lines:")
    vaultAdapter.getChangedCount("0ba64d027223").foreach(println)

    println("Changed lines between %s and %s:".format("6cc0951f3bcd9aae", "8b418af7fb7d8b1"))
    vaultAdapter.getChangedCount("6cc0951f3bcd9aae", "8b418af7fb7d8b1").foreach(println)
  }

//  private def printGitDiff(gitDiff: GitDiffParser.FileDiff): Unit = {
//    println("Old file name: " + gitDiff.oldFile)
//    println("New file name: " + gitDiff.newFile)
//    gitDiff.fileChange match {
//      case file: GitDiffParser.NewFile => println("new file mode " + file.mode)
//      case file: GitDiffParser.DeletedFile => println("deleted file mode " + file.mode)
//      case file: GitDiffParser.RenamedFile => println("rename from %s\nrename to %s".
//        format(file.fromPath, file.toPath))
//      case file: GitDiffParser.CopiedFile => println("copy from %s\ncopy to %s".
//        format(file.fromPath, file.toPath))
//      case _ => println("modified file")
//    }
//
//    for (chunk <- gitDiff.chunks) chunk match {
//      case text: GitDiffParser.TextChunk => {
//        println(text.rangeInformation)
//        for (line <- text.changeLines) line match {
//          case l: GitDiffParser.AddedLine => println("+" + l.line)
//          case l: GitDiffParser.DeletedLine => println("-" + l.line)
//          case l: GitDiffParser.ContextLine => println(" " + l.line)
//          case l: GitDiffParser.WarningLine => println("\\ " + l.line)
//        }
//      }
//      case GitDiffParser.BinaryChunk => println("Binary files differ")
//    }
//  }
}
