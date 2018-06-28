package com.jetbrains.tuna.ui

import com.intellij.lang.Language
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.ui.*
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import javax.swing.border.CompoundBorder

fun createCodeSnippetTextField(project: Project, text: String, language: Language): EditorTextField {
  val multiline = true

  val document = EditorFactory.getInstance().createDocument(text)
  return object : EditorTextField(document, project, language.associatedFileType, true, !multiline) {
    override fun createEditor(): EditorEx {
      val editor = super.createEditor()
      editor.setHorizontalScrollbarVisible(multiline)
      editor.setVerticalScrollbarVisible(multiline)
      editor.settings.isUseSoftWraps = true
      editor.settings.lineCursorWidth = EditorUtil.getDefaultCaretWidth()
      editor.colorsScheme.editorFontName = font.fontName
      editor.colorsScheme.editorFontSize = font.size
      editor.contentComponent.border = CompoundBorder(editor.contentComponent.border, JBUI.Borders.emptyLeft(2))
      return editor
    }

    override fun getPreferredSize(): Dimension {
      return Dimension(600, 400)
    }
  }
}

fun createMessageTextField(project: Project): EditorTextField {
  val features = hashSetOf<EditorCustomization>()
  features.add(SoftWrapsEditorCustomization.ENABLED)
  val textField = EditorTextFieldProvider.getInstance().getEditorField(FileTypes.PLAIN_TEXT.language, project, features)
  TextFieldWithAutoCompletion.installCompletion(textField.document, project, SlackRecipientProvider(project), false)
  return textField
}
