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

import java.time.{LocalTime, LocalDateTime, LocalDate}
import java.time.format.DateTimeFormatter

import scalafx.geometry.{Pos, Insets}
import scalafx.scene.control.{Label, DatePicker}
import scalafx.scene.layout.VBox
import scalafx.util.StringConverter

/** Pane for filtering vault information.
  *
  * @author Alexey Kuzin <amkuzink@gmail.com>
  */
class FilterPane extends VBox {

  private class DateStringConverter extends StringConverter[LocalDate] {
    private val mDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override def fromString(dateString: String): LocalDate = {
      Option(dateString) match {
        case Some(s) if !s.isEmpty => LocalDate.parse(dateString, mDateFormatter)
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

  private val fromDateLayout = new VBox(5, Label("From date:"), mFromDatePicker) {
    alignment = Pos.TopLeft
    padding = Insets(0, 0, 0, 10)
  }
  private val toDateLayout = new VBox(5, Label("To date:"), mToDatePicker) {
    alignment = Pos.TopLeft
    padding = Insets(0, 0, 0, 10)
  }

  def getFilter: TreemapFilter = {
    new TreemapFilter(LocalDateTime.of(mFromDatePicker.getValue, LocalTime.of(0, 0)),
      LocalDateTime.of(mToDatePicker.getValue, LocalTime.of(0, 0)))
  }

  style = "-fx-border-style: solid;" +
    "-fx-border-color: black;" +
    "-fx-border-width: 0 0 1px 0;"
  padding = Insets(10, 5, 20, 5)
  spacing = 30
  children = List(
    fromDateLayout,
    toDateLayout
  )
}
