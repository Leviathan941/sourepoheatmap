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

package org.sourepoheatmap.application

import java.io.IOException

import org.sourepoheatmap.repository.GitVaultInfoAdapter

/**
 * Placeholder to test other parts.
 *
 * @author Alexey Kuzin <amkuzink@gmail.com>
 */
object Application {
  def main(args: Array[String]) {
    val gitAdapter = new GitVaultInfoAdapter("/home/leviathan/projects/melange")
    try {
      println(gitAdapter.getHeadBranchFullName)
      println(gitAdapter.getHeadBranchName)
      println
      gitAdapter.getAllBranches.foreach(println)
      println
      gitAdapter.getCommitIdsBetween(1390471304, 1424523705).foreach(println)
      println
      println(gitAdapter.getCommitIdAfter(1390471304))
      println
      val lastCommit = gitAdapter.getCommitIdUntil(1424523705)
      println(lastCommit)
      println("\n")

      //gitDiffAdapter.getCommitDiff("e2f71aea8dda960a70ef79549c2b51d6615657b1").foreach(println)
      gitAdapter.getDiff("dae531a17c796862ee", "0ba64d02722329").foreach(println)
    } catch {
      case ex: IOException => println(ex)
    }
  }
}
