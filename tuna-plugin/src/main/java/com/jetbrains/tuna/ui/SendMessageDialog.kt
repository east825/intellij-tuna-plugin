package com.jetbrains.tuna.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackUser
import javax.swing.JComponent

class SendMessageDialog(private val project: Project,
                        private val slackUser: SlackUser,
                        editor: Editor) : DialogWrapper(project) {

  private val receiverName = JBTextField(20)
  private val codeSnippetTextField: EditorTextField?
  private val messageField: EditorTextField = createMessageTextField(project)

  init {
    title = "Send to Slack"

    receiverName.text = "To: ${slackUser.userName}"
    receiverName.isEnabled = false

    val document = editor.document
    val codeSnippet = editor.selectionModel.selectedText?.let { removeCommonIndent(it) } ?: document.text
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)

    codeSnippetTextField = createCodeSnippetTextField(project, codeSnippet, psiFile?.language ?: PlainTextLanguage.INSTANCE)

    init()
  }

  override fun createCenterPanel(): JComponent? {
    return panel {
      codeSnippetTextField?.let { row { it(CCFlags.grow, CCFlags.push) } }
      row { messageField(CCFlags.grow, CCFlags.push) }
      row { receiverName(CCFlags.pushX) }
    }
  }

  override fun doOKAction() {
    val slackMessages = TunaProjectComponent.getInstance(project).slackMessages
    slackMessages?.postMessageWithCodeSnippet(slackUser, messageField.text, codeSnippetTextField?.text ?: "")

    super.doOKAction()
  }

  private fun removeCommonIndent(text: String): String {
    // TODO Handle mixed tabs and spaces
    val indentPattern = "^([ \t]*).*".toPattern()
    val commonIndentSize = text
                             .lines()
                             .filter { it.isNotEmpty() }
                             .map { indentPattern.matcher(it) }
                             .map { if (it.matches()) it.toMatchResult().group(1).length else 0 }
                             .min() ?: 0

    return text
      .lines()
      .joinToString("\n") { if (it.isEmpty()) it else it.substring(commonIndentSize) }
  }
}