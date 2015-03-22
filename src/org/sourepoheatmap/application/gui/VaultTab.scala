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
import java.time.{ZoneId, LocalDate}

import org.sourepoheatmap.vault.VaultInfoAdapter
import org.sourepoheatmap.vault.VaultInfoAdapter.VaultException

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{DatePicker, Button, TextField, Tab, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.text.Text
import scalafx.stage.DirectoryChooser
import scalafx.util.StringConverter

/** Tab containing controls to select repository location,
  * commits and heat map.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class VaultTab(title: String) extends Tab { thisTab =>
  private val mTitle: Text = new Text(title) {
    onMouseClicked = (event: MouseEvent) => {
      if (event.clickCount == 2) {
        mEditTitle.text = text.value
        thisTab.graphic = mEditTitle
        mEditTitle.requestFocus()
      }
    }
  }
  private val mEditTitle: TextField = new TextField {
    prefColumnCount = 6

    focused.onChange((_, _, newValue) => {
      if (newValue == false) {
        mTitle.text = text.value
        thisTab.graphic = mTitle
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

  private val mTreemapPane = new StackPane {
    alignment = Pos.Center

    children = Label("Place for heatmap")
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
      LoggerArea.clear()
      var vaultAdaptorOption: Option[VaultInfoAdapter] = None
      try {
        vaultAdaptorOption = VaultInfoAdapter(mPathTextField.text.value)
        val Some(vaultAdaptor) = vaultAdaptorOption

        val fromTime = convertDateToEpochTime(mFromDatePicker.value.value)
        val toTime = convertDateToEpochTime(mToDatePicker.value.value)

        // Get commits between specified dates
        val commits = vaultAdaptor.getCommitIdsBetween(fromTime, toTime)
        // Get Map of Tuple2(<file>, <changed lines>) and filter it to remove binary changes.
        val commitsDiff = vaultAdaptor.getChangedCount(commits.head, commits.last).filterNot(_._2 == 0)

        thisTab.mTreemapPane.children = new TreemapPane(800, 595, commitsDiff) // TODO get width and height from the window
      } catch {
        case ex: MatchError => LoggerArea.logInfo("Please specify path to a repository.")
        case ex: IllegalArgumentException => LoggerArea.logInfo("'To date' must be greater than 'From date'.")
        case ex: VaultException => LoggerArea.logError(ex.getMessage)
      } finally {
        vaultAdaptorOption match {
          case Some(adaptor) => adaptor.terminate()
          case _ =>
        }
      }
    }
  }

  private def convertDateToEpochTime(date: LocalDate): Int = {
    date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond.toInt
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

    left = new AnchorPane {
      prefHeight = 600
      padding = Insets(5)
      vgrow = Priority.Always
      maxHeight = Double.MaxValue
      style = "-fx-border-style: solid;" +
        "-fx-border-color: grey;" +
        "-fx-border-width: 0 1px 0 0;"

      val fromDateLayout = new VBox(5, Label("From date:"), mFromDatePicker) {
        alignment = Pos.TopLeft
        padding = Insets(0, 0, 0, 10)
      }
      val toDateLayout = new VBox(5, Label("To date:"), mToDatePicker) {
        alignment = Pos.TopLeft
        padding = Insets(0, 0, 0, 10)
      }
      val refreshLayout = new HBox(mRefreshButton) {
        alignment = Pos.BottomRight
        alignmentInParent = Pos.BottomRight
        hgrow = Priority.Always
      }
      AnchorPane.setTopAnchor(fromDateLayout, 10)
      AnchorPane.setTopAnchor(toDateLayout, 80)
      AnchorPane.setBottomAnchor(refreshLayout, 5)
      AnchorPane.setRightAnchor(refreshLayout, 5)

      children = List(
        fromDateLayout,
        toDateLayout,
        refreshLayout
      )
    }

    center = mTreemapPane
  }
}
