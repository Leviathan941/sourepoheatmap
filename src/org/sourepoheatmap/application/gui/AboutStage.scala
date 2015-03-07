/*
 * Copyright (c) 2015, Alexey Kuzin <amkuzink@gmail.com>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sourepoheatmap.application.gui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{TextArea, Label}
import scalafx.scene.layout.BorderPane
import scalafx.scene.paint.Color
import scalafx.stage.Stage

/** About stage contains information about creators.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class AboutStage extends Stage { about =>
  val mAppNameLabel = new Label("Sourepo Heatmap") {
    alignment = Pos.BaselineLeft
    padding = Insets(10)
    style = "-fx-font-size: 24pt"
  }

  val mCreatorsTextArea = new TextArea {
    editable = false
    text = "Copyright (C) 2015\n" +
      "Licensed under 3-clause BSD License.\n\n" +
      "Alexey Kuzin (amkuzink@gmail.com)\n" +
      "Author"
  }

  title = "About Sourepo Heatmap"
  resizable = false
  width = 340
  height = 220
  scene = new Scene {
    fill = Color.LightGrey
    content = new BorderPane {
      top = mAppNameLabel
      bottom = mCreatorsTextArea
    }
  }
  focused.onChange((_, _, newValue) => {
    if (newValue == false) about.close()
  })
}
