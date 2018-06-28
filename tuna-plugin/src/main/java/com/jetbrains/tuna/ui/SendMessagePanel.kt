package com.jetbrains.tuna.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.ullink.slack.simpleslackapi.SlackUser
import java.awt.BorderLayout
import javax.swing.JComponent

class SendMessagePanel(project: Project, slackUser: SlackUser?, editor: Editor)
  : JBPanel<SendMessagePanel>(BorderLayout()) {
  private val codeSnippetTextField: EditorTextField
  private val messageField: EditorTextField = createMessageTextField(project)
  private val slackUserField: JBTextField = JBTextField(slackUser?.userName ?: "").apply { isEditable = false }

  val preferableFocusComponent: JComponent
    get() = messageField

  init {
    val document = editor.document
    val codeSnippet = editor.selectionModel.selectedText?.let { removeCommonIndent(it) } ?: document.text
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)

    codeSnippetTextField = createCodeSnippetTextField(project, codeSnippet, psiFile?.language ?: PlainTextLanguage.INSTANCE)

    add(panel {
      row("Message:") {
        messageField(CCFlags.grow, CCFlags.push)
      }
      row("Code:") { codeSnippetTextField(CCFlags.grow, CCFlags.pushX) }
      row("To:") { slackUserField(CCFlags.growX) }
    }, BorderLayout.CENTER)
  }

  fun getMessage(): String = messageField.text

  fun getCodeSnippet(): String? = codeSnippetTextField.text

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