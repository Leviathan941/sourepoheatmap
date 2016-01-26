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

package sourepoheatmap.application.gui

import java.io.File

import sourepoheatmap.vault.VaultInfoAdapter
import sourepoheatmap.vault.VaultInfoAdapter.VaultException

import scala.util.control
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.{Tab, Button, Label, TextField, ScrollPane}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.text.Text
import scalafx.stage.DirectoryChooser

/** Tab containing controls to select repository location,
  * commits and heat map.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class VaultTab(title: String) extends Tab { thisTab =>
  private lazy val mTitle: Text = new Text(title) {
    onMouseClicked = (event: MouseEvent) => {
      if (event.clickCount == 2) {
        mEditTitle.text = text.value
        thisTab.graphic = mEditTitle
        mEditTitle.requestFocus()
      }
    }
  }
  private lazy val mEditTitle: TextField = new TextField {
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

  private val mApplyButton = new Button("Apply") {
    alignment = Pos.Center
    hgrow = Priority.Never
    padding = Insets(5)
    onAction = handle {
      applyVault()
    }
  }

  private var mVaultInfoAdapter: Option[VaultInfoAdapter] = None

  private class TreemapPaneHolder extends StackPane {
    private val mPlaceholderLabel = Label("Place for heatmap")

    alignment = Pos.Center
    children = mPlaceholderLabel

    def clear(): Unit = {
      children = mPlaceholderLabel
    }
  }

  private val mTreemapPane = new TreemapPaneHolder

  private lazy val mRefreshButton: Button = new Button("Refresh") {
    padding = Insets(5)
    onAction = handle {
      LoggerArea.clear()
      if (refreshTreemap()) {
        visible = false
        mClearButton.visible = true
      }
    }
  }

  private lazy val mClearButton: Button = new Button("Clear") {
    padding = Insets(5)
    visible = false
    onAction = handle {
      LoggerArea.clear()
      clear()
      visible = false
      mRefreshButton.visible = true
    }
  }

  def clear(): Unit = mTreemapPane.clear()

  def refreshTreemap(): Boolean = {
    val impl = (adapter: VaultInfoAdapter) => {
      val filter = mFilterPane.getFilter

      val diffs: Map[String, Int] = filter.getBranchesFilter match {
        case branch :: Nil => getBranchDiff(adapter, branch, filter.getDateTimeFilter)
        case head :: rest => getBranchesDiff(adapter, filter.getBranchesFilter, filter.getDateTimeFilter)
        case _ => throw new VaultException("No branches found")
      }

      if (diffs.isEmpty)
        LoggerArea.logInfo("There are no commits between specified dates.")
      else
        thisTab.mTreemapPane.children = new TreemapPane(mTreemapPane.width.value, mTreemapPane.height.value,
          diffs)
    }

    try {
      val Some(vaultAdaptor) = mVaultInfoAdapter

      val either = control.Exception.catching(classOf[IllegalArgumentException], classOf[VaultException]
        ).either(impl(vaultAdaptor))

      either match {
        case Left(exc) => exc match {
          case ex: IllegalArgumentException => LoggerArea.logInfo("'To date' must be greater than 'From date'.")
          case ex: VaultException => LoggerArea.logError(ex.getMessage)
        }
          false
        case _ => true
      }
    } catch {
      case ex: MatchError => LoggerArea.logWarning("Please, specify path to a repository."); false
    }
  }

  private def terminateVaultAdapter(): Unit = {
    mVaultInfoAdapter match {
      case Some(adapter) => adapter.terminate(); mVaultInfoAdapter = None
      case _ =>
    }
  }

  private def applyVault(): Unit = {
    terminateVaultAdapter()
    mVaultInfoAdapter = VaultInfoAdapter(mPathTextField.text.value)
    mVaultInfoAdapter match {
      case Some(adapter) => mFilterPane.adjustFilters(adapter)
      case None => LoggerArea.logWarning("Please, specify path to a repository.")
    }
  }

  private def getCommitsDiff(vaultAdapter: VaultInfoAdapter, commits: List[String]): Map[String, Int] = {
    if (commits.isEmpty) Map.empty
    else if (commits.size == 1) vaultAdapter.getChangedCount(commits.head)
    else vaultAdapter.getChangedCount(commits.head, commits.last)
  }

  private def getBranchDiff(adapter: VaultInfoAdapter, branch: String,
      dateTimeFilter: (Int, Int)): Map[String, Int] = {
    // Switch to the specified branch
    adapter.switchBranch(branch)
    // Get commits between specified dates
    val commits = adapter.getCommitIdsBetween(dateTimeFilter._1, dateTimeFilter._2)
    // Get Map of Tuple2(<file>, <changed lines>) and filter it to remove binary changes.
    getCommitsDiff(adapter, commits).filterNot(_._2 == 0)
  }

  private def getBranchesDiff(adapter: VaultInfoAdapter, branches: List[String],
      dateTimeFilter: (Int, Int)): Map[String, Int] = {
    val branchesDiff = for(
      branch <- branches;
      branchDiff = getBranchDiff(adapter, branch, dateTimeFilter)
      if branchDiff.nonEmpty;
      branchChanges = (0 /: branchDiff.values) (_ + _)
    ) yield branch -> branchChanges
    branchesDiff.toMap
  }

  private val mFilterPane = new FilterPane

  onClosed = handle { terminateVaultAdapter() }

  graphic = mTitle
  content = new BorderPane {
    top = new HBox(10) {
      alignment = Pos.BaselineLeft
      style = "-fx-border-style: solid;" +
        "-fx-border-color: grey;" +
        "-fx-border-width: 0 0 1px 0;"
      padding = Insets(5, 10, 5, 10)
      children = Seq(mPathTextField, mOpenButton, mApplyButton)
      HBox.setHgrow(mPathTextField, Priority.Always)
    }

    left = new ScrollPane {
      hbarPolicy = ScrollBarPolicy.NEVER
      vbarPolicy = ScrollBarPolicy.AS_NEEDED
      fitToHeight = true
      style = "-fx-border-style: solid;" +
        "-fx-border-color: grey;" +
        "-fx-border-width: 0 1px 0 0;"

      content = new VBox {
        padding = Insets(0)
        spacing = 10

        val refreshLayout = new StackPane {
          padding = Insets(10, 5, 5, 5)
          alignment = Pos.BottomRight
          children = List(mClearButton, mRefreshButton)
        }

        children = List(
          mFilterPane,
          refreshLayout
        )
      }
    }

    center = mTreemapPane
  }
}
