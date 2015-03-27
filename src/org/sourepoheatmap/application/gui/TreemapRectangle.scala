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

import scalafx.Includes.handle
import scalafx.scene.control.Tooltip
import scalafx.scene.paint.Color
import scalafx.scene.shape.{StrokeType, Rectangle}
import scalafx.scene.text.TextAlignment

/** Rectangular element of the treemap.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class TreemapRectangle(
    pos: (Double, Double),
    dimen: (Double, Double),
    diffInfo: (String, Int),
    color: Color) extends Rectangle { rect =>

  private val mTooltip = new Tooltip {
    text = "Object: %s\nChanges: %d".format(diffInfo._1, diffInfo._2)
    autoFix = true
    textAlignment = TextAlignment.Center
  }
  Tooltip.install(this, mTooltip)

  onMouseEntered = handle { rect.stroke = Color.White }
  onMouseExited = handle { rect.stroke = Color.Black }

  x = pos._1
  y = pos._2
  width = dimen._1
  height = dimen._2
  fill = color
  stroke = Color.Black
  strokeType = StrokeType.Inside
  strokeWidth = 1

  // TODO Show diffInfo if the rectangle is big enough.
}

object TreemapRectangle {
  def apply(pos: (Double, Double), dimen: (Double, Double), diffInfo: (String, Int), color: Color):
      TreemapRectangle = {
    new TreemapRectangle(pos, dimen, diffInfo, color)
  }
}
