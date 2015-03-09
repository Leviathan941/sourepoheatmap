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

package org.sourepoheatmap.application.gui

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.LocalDate

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{DatePicker, Button, TextField, Tab, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{VBox, HBox, Priority, BorderPane}
import scalafx.scene.text.Text
import scalafx.stage.DirectoryChooser
import scalafx.util.StringConverter

/** Tab containing controls to select repository location,
  * commits and heat map.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class VaultTab(title: String) extends Tab { tab =>
  private val mTitle: Text = new Text(title) {
    onMouseClicked = (event: MouseEvent) => {
      if (event.clickCount == 2) {
        mEditTitle.text = text.value
        tab.graphic = mEditTitle
        mEditTitle.requestFocus()
      }
    }
  }
  private val mEditTitle: TextField = new TextField {
    prefColumnCount = 6

    focused.onChange((_, _, newValue) => {
      if (newValue == false) {
        mTitle.text = text.value
        tab.graphic = mTitle
      }
    })
  }

  private val mPathTextField = new TextField {
    alignment = Pos.BaselineLeft
    promptText = "Enter the path"
    hgrow = Priority.Sometimes
    padding = Insets(5)
  }

  private val mOpenButton = new Button("Open") {
    alignment = Pos.Center
    hgrow = Priority.Never
    padding = Insets(5)
    onAction = handle { openBtnAction() }

    private def openBtnAction = () => {
      new DirectoryChooser {
        title = "Select path to repository"
        initialDirectory = new File(System.getProperty("user.home"))
      }.showDialog(GuiApplication.stage) match {
        case dir: File => mPathTextField.text = dir.getPath
        case null => LoggerArea.logWarning("Repository has not been chosen.")
      }
    }
  }

  private class DateStringConverter extends StringConverter[LocalDate] {
    private val mDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    override def fromString(dateString: String): LocalDate = {
      Option(dateString) match {
        case Some(s) if (!s.isEmpty) => LocalDate.parse(dateString, mDateFormatter)
        case _ => null
      }
    }

    override def toString(localDate: LocalDate): String = {
      Option(localDate) match {
        case Some(ld) => mDateFormatter.format(ld)
        case _ => ""
      }
    }
  }

  private val mFromDatePicker = new DatePicker(LocalDate.now()) {
    converter = new DateStringConverter
  }
  private val mToDatePicker = new DatePicker(LocalDate.now()) {
    converter = new DateStringConverter
  }

  private val mRefreshButton = new Button("Refresh") {
    alignmentInParent = Pos.BottomRight
    hgrow = Priority.Never
    padding = Insets(5)
    onAction = handle { refreshHeatmap() }

    private def refreshHeatmap = () => {
      println("REFRESH")
      // TODO Implement creating heatmap here
    }
  }

  graphic = mTitle
  content = new BorderPane {
    hgrow = Priority.Always
    vgrow = Priority.Always

    top = new HBox(10) {
      alignment = Pos.BaselineLeft
      hgrow = Priority.Always
      style = "-fx-border-style: solid;" +
        "-fx-border-color: grey;" +
        "-fx-border-width: 0 0 1px 0;"
      padding = Insets(5, 10, 5, 10)
      children = Seq(mPathTextField, mOpenButton)
      HBox.setHgrow(mPathTextField, Priority.Always)
    }

    left = new VBox(30) {
      prefHeight = 600
      fillWidth = true
      padding = Insets(10, 5, 10, 5)
      vgrow = Priority.Always
      maxHeight = Double.MaxValue
      style = "-fx-border-style: solid;" +
        "-fx-border-color: grey;" +
        "-fx-border-width: 0 1px 0 0;"
      children = List(
        new VBox(5, new Label("From date:"), mFromDatePicker),
        new VBox(5, new Label("To date:"), mToDatePicker),
        new HBox(mRefreshButton) {
          alignment = Pos.BottomRight
          alignmentInParent = Pos.BottomRight
          hgrow = Priority.Always
        }
      )
    }
  }
}
