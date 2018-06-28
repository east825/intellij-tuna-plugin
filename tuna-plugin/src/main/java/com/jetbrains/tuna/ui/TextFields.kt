package com.jetbrains.tuna.ui

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.ui.*

fun createCodeSnippetTextField(project: Project): EditorTextField {
  val features = hashSetOf<EditorCustomization>()
  features.add(SoftWrapsEditorCustomization.ENABLED)
  val textField = EditorTextFieldProvider.getInstance().getEditorField(JavaFileType.INSTANCE.language, project, features)
  TextFieldWithAutoCompletion.installCompletion(textField.document, project, SlackRecipientProvider(project), false)
  return textField
}

fun createMessageTextField(project: Project): EditorTextField {
  val features = hashSetOf<EditorCustomization>()
  features.add(SoftWrapsEditorCustomization.ENABLED)
  val textField = EditorTextFieldProvider.getInstance().getEditorField(FileTypes.PLAIN_TEXT.language, project, features)
  TextFieldWithAutoCompletion.installCompletion(textField.document, project, SlackRecipientProvider(project), false)
  return textField
}
