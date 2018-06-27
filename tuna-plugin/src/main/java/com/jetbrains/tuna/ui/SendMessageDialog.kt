package com.jetbrains.tuna.ui

import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorCustomization
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import javax.swing.JComponent

class SendMessageDialog(project: Project) : DialogWrapper(project) {
  private val receiverName = tuneTextField(project)
  private val messageField = JBTextField(20)

  init {
    init()
    title = "Send Message"
  }

  override fun createCenterPanel(): JComponent? {
    return panel {
      row("To:") { receiverName(CCFlags.pushX) }
      row("Message:") { messageField(CCFlags.pushX) }
    }
  }

  override fun doOKAction() {
    super.doOKAction()
    // TODO
  }

  private fun tuneTextField(project: Project): JComponent {
    val features = hashSetOf<EditorCustomization>()
    val textField = EditorTextFieldProvider.getInstance().getEditorField(FileTypes.PLAIN_TEXT.language, project, features);
    TextFieldWithAutoCompletion.installCompletion(textField.document, project, SlackRecipientProvider(project), false)
    return textField
  }

}