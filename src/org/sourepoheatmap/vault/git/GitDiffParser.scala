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

import scala.util.parsing.combinator.RegexParsers

/** Parser for "git diff" formatted text.
  *
  * Only simple git diff format is supported at the moment.
  * Combined and other formats are not supported.
  *
  * =Formal grammar=
  * allDiffs        ::= { fileDiff }
  * fileDiff        ::= gitHeader extendedHeader [ unifiedHeader diffChunks ]
  * gitHeader       ::= "diff --git a/" filename [ " b/" filename ] newline
  * extendedHeader  ::= [ modeChanged ] [ similarity ] [ copiedFile | renamedFile
  *                     | deletedFile | newFile ] index
  * unifiedHeader   ::= "--- " [ "a/" ] filename newline
  *                     "+++ " [ "b/" ] filename newline
  * diffChunks      ::= { changeChunk }
  * filename        ::= """[&#94;*&%\s]+"""
  * newline         ::= """\r?\n"""
  * modeChanged     ::= "old mode " mode newline
  *                     "new mode " mode newline
  * similarity      ::= ( "similarity" | "dissimilarity" ) "index " number "%" newline
  * copiedFile      ::= "copy from " filename newline
  *                     "copy to " filename newline
  * renamedFile     ::= "rename from " filename newline
  *                     "rename to " filename newline
  * deletedFile     ::= "deleted file mode " mode newline
  * newFile         ::= "new file mode " mode newline
  * index           ::= "index " hash ".." hash [ " " mode ] newline
  * changeChunk     ::= binaryChange | ( chunkHeader { lineChange } )
  * mode            ::= """\d{6}"""
  * number          ::= """\d+"""
  * hash            ::= """[0-9a-f]{7,}"""
  * binaryChange    ::= "Binary files " [ "a/" ] filename [ " b/" ] filename
  *                     " differ" newline
  * chunkHeader     ::= rangeInfo [ contextLine ] [ newline ]
  * lineChange      ::= warningLine | contextLine | addedLine | deletedLine
  * rangeInfo       ::= "@@ -" number [ "," number ] " +" number [ "," number ] " @@"
  * warningLine     ::= "\ " commonLine newline
  * contextLine     ::= " " commonLine newline
  * addedLine       ::= "+" commonLine newline
  * deletedLine     ::= "-" commonLine newline
  * commonLine      ::= """.*"""
  *
  * @see [[https://www.kernel.org/pub/software/scm/git/docs/git-diff.html Git diff man]]
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
private[git] object GitDiffParser extends RegexParsers {
  override val skipWhitespace = false

  case class FileDiff(oldFile: String, newFile: String, fileChange: FileChange, chunks: List[ChangeChunk])

  sealed trait FileChange
  case class NewFile(mode: Int) extends FileChange
  case class DeletedFile(mode: Int) extends FileChange
  case class RenamedFile(fromPath: String, toPath: String) extends FileChange
  case class CopiedFile(fromPath: String, toPath: String) extends FileChange
  case object ModifiedFile extends FileChange

  case class Index(preHash: String, postHash: String, mode: Int)

  sealed trait LineChange { def line: String }
  case class ContextLine(line: String) extends LineChange
  case class DeletedLine(line: String) extends LineChange
  case class AddedLine(line: String) extends LineChange
  case class WarningLine(line: String) extends LineChange

  case class RangeInfo(oldStartLine: Int, oldLineNumber: Int, newStartLine: Int, newLineNumber: Int) {
    override def toString = "@@ -%d,%d +%d,%d @@".format(oldStartLine, oldLineNumber, newStartLine, newLineNumber)
  }

  sealed trait ChangeChunk
  case class TextChunk(rangeInformation: RangeInfo, context: ContextLine, changeLines: List[LineChange]) extends
    ChangeChunk
  case object BinaryChunk extends ChangeChunk

  def allDiffs: Parser[List[FileDiff]] = rep1(fileDiff)
  def fileDiff: Parser[FileDiff] = gitHeader ~ extendedHeader ~ opt(unifiedHeader ~> diffChunks) ^^
    { case files ~ change ~ chunks => FileDiff(files._1, files._2, change, chunks getOrElse Nil)}
  def diffChunks: Parser[List[ChangeChunk]] = rep1(changeChunk)
  def changeChunk: Parser[ChangeChunk] = binaryChange | (chunkHeader ~ rep1(lineChange)) ^^ {
    case h ~ lines => TextChunk(h._1, h._2, lines)
    case bin => BinaryChunk
  }

  def gitHeader: Parser[(String, String)] =
    "diff --git" ~ " " ~ oldFilePrefix ~> filename ~ (" " ~ newFilePrefix ~> filename) <~ newline ^^
      { case f1 ~ f2 => (f1, f2) }
  def extendedHeader: Parser[FileChange] =
    opt(modeChanged) ~ opt(similarity) ~> opt(copiedFile | renamedFile | deletedFile | newFile) <~ index ^^
      { _.getOrElse(ModifiedFile) }
  def unifiedHeader: Parser[(String, String)] =
    "--- " ~ opt(oldFilePrefix) ~> filename ~ (newline ~
    "+++ " ~ opt(newFilePrefix) ~> filename) <~ newline ^^ { case f1 ~ f2 => (f1, f2) }
  def chunkHeader: Parser[(RangeInfo, ContextLine)] = rangeInfo ~ opt(contextLine) <~ opt(newline) ^^
    { case ri ~ ctx => (ri, ctx getOrElse ContextLine("")) }

  def modeChanged: Parser[(Int, Int)] = "old mode " ~> mode ~ (newline ~ "new mode " ~> mode) <~ newline ^^
    { case mode1 ~ mode2 => (mode1, mode2) }
  def similarity: Parser[Int] = ("similarity " | "dissimilarity ") ~ "index " ~> number <~ "%" ~ newline
  def copiedFile: Parser[CopiedFile] = "copy from " ~> filename ~
    (newline ~ "copy to " ~> filename) <~ newline ^^ { case f1 ~ f2 =>  CopiedFile(f1, f2) }
  def renamedFile: Parser[RenamedFile] = "rename from " ~> filename ~
    (newline ~ "rename to " ~> filename) <~ newline ^^ { case f1 ~ f2 =>  RenamedFile(f1, f2) }
  def deletedFile: Parser[DeletedFile] = "deleted file mode " ~> mode <~ newline ^^ DeletedFile
  def newFile: Parser[NewFile] = "new file mode " ~> mode <~ newline ^^ NewFile
  def index: Parser[Index] = "index " ~> hash ~ (".." ~> hash) ~ opt(" " ~> mode) <~ newline ^^
    { case h1 ~ h2 ~ m => Index(h1, h2, m getOrElse 0) }
  def rangeInfo: Parser[RangeInfo] = "@@ -" ~> number ~ opt("," ~> number) ~
    (" +" ~> number) ~ opt("," ~> number) <~ " @@" ^^
    { case sl1 ~ ln1 ~ sl2 ~ ln2 => RangeInfo(sl1, ln1 getOrElse 0, sl2, ln2 getOrElse 0) }
  def binaryChange: Parser[ChangeChunk] = "Binary files " ~ opt(opt(oldFilePrefix) ~ filename ~
    " " ~ opt(newFilePrefix) ~ filename ~ " ") ~ "differ" ~ newline ^^ { case _ => BinaryChunk }

  def filename: Parser[String] = """[^*&%\s]+""".r
  def newline: Parser[String] = """\r?\n""".r
  def mode: Parser[Int] = """\d{6}""".r ^^ { _.toInt }
  def number: Parser[Int] = """\d+""".r ^^ { _.toInt }
  def hash: Parser[String] = """[0-9a-f]{7,}""".r
  def commonLine: Parser[String] = """.*""".r

  def lineChange: Parser[LineChange] = warningLine | contextLine | addedLine | deletedLine
  def warningLine: Parser[LineChange] = "\\ " ~> commonLine <~ newline ^^ WarningLine
  def contextLine: Parser[ContextLine] = " " ~> commonLine <~ newline ^^ ContextLine
  def addedLine: Parser[AddedLine] = "+" ~> commonLine <~ newline ^^ AddedLine
  def deletedLine: Parser[DeletedLine] = "-" ~> commonLine <~ newline ^^ DeletedLine

  def oldFilePrefix = "a/"
  def newFilePrefix = "b/"

  def apply(diff: String): List[FileDiff] = parseAll(allDiffs, diff) match {
    case Success(result, _) => result
    case failure: Failure => throw new DiffParseException(failure.toString())
    case error: Error => throw new DiffParseException("Fatal error: " + error.toString())
  }

  class DiffParseException(msg: String) extends Exception(msg)
}
