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

package sourepoheatmap.gui

import sourepoheatmap.gui.BranchesFilterPane.Item

import scala.collection.JavaConversions._

import scalafx.beans.property.BooleanProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{Label, ListView}
import scalafx.scene.control.cell.CheckBoxListCell
import scalafx.scene.layout.VBox

/** [[FilteringPane]] for providing branches selection.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class BranchesFilterPane extends VBox with FilteringPane[List[String]] {

  private val mListView = new ListView[Item] {
    cellFactory = CheckBoxListCell.forListView(_.selected)
    prefHeight = 300
  }

  override def getFilter: List[String] = {
    for(item: Item <- mListView.getItems.toList
      if item.isSelected
    ) yield item.toString
  }

  def update(branches: List[String]): Unit = {
    mListView.items = ObservableBuffer[Item](branches.map((b: String) => new Item(false, b)))
    mListView.getItems.head.setSelected(true)
  }

  style = "-fx-border-style: solid;" +
    "-fx-border-color: grey;" +
    "-fx-border-width: 0 0 1px 0;"
  padding = Insets(10, 5, 10, 5)
  spacing = 5
  children = List(
    Label("Select branches:"),
    mListView
  )
}

private object BranchesFilterPane {
  private class Item(initialSelection: Boolean, private val name: String) {
    val selected = BooleanProperty(initialSelection)
    def setSelected(select: Boolean): Unit = selected.value = select
    def isSelected: Boolean = selected.value
    override def toString: String = name
  }
}
