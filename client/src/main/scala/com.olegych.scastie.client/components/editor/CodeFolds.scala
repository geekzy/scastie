package com.olegych.scastie.client.components.editor

import codemirror.TextAreaEditor

import japgolly.scalajs.react.Callback

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement

object CodeFoldingAnnotations {
  private def findFolds(code: String): Set[RangePosititon] = {
    val (folds, _, _) = {
      val lines = code.split("\n").toList

      lines.foldLeft((Set.empty[RangePosititon], Option.empty[Int], 0)) {
        case ((folds, open, indexTotal), line) => {
          val (folds0, open0) =
            if (line == "// fold") {
              if (open.isEmpty) (folds, Some(indexTotal))
              else (folds, open)
            } else if (line == "// end-fold") {
              open match {
                case Some(start) =>
                  (folds + RangePosititon(start, indexTotal + line.length),
                   None)

                case None => (folds, None)
              }
            } else {
              (folds, open)
            }

          (folds0, open0, indexTotal + line.length + 1)
        }
      }
    }

    folds
  }

  def apply(editor: TextAreaEditor, props: Editor): Callback = {
    Callback {
      val doc = editor.getDoc()
      findFolds(props.code).foreach { range =>
        val posStart = doc.posFromIndex(range.indexStart)
        val posEnd = doc.posFromIndex(range.indexEnd)

        var boom: Option[Annotation] = None
        val noop: (HTMLElement => Unit) = element => {
          element.onclick = (event: dom.MouseEvent) => {
            boom.foreach(_.clear())
          }
        }
        val annot = Annotation.fold(editor,
                                    posStart,
                                    posEnd,
                                    "-- Click to unfold --",
                                    noop)
        boom = Some(annot)

        (range, annot)
      }
    }
  }
}
