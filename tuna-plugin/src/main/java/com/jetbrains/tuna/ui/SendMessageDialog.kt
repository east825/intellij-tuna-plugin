package com.jetbrains.tuna.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackUser
import javax.swing.JComponent

class SendMessageDialog(private val project: Project,
                        private val slackUser: SlackUser? = null,
                        private val editor: Editor) : DialogWrapper(project) {
  private val sendMessagePanel: SendMessagePanel = SendMessagePanel(project, slackUser, editor)

  init {
    init()
    title = "Send to Slack"
  }

  override fun createCenterPanel(): JComponent? {
    return sendMessagePanel
  }

  override fun doOKAction() {
    val slackUser = slackUser ?: return
    val slackMessages = TunaProjectComponent.getInstance(project).slackMessages

    val codeSnippet = sendMessagePanel.getCodeSnippet()
    if (codeSnippet == null) {
      slackMessages?.session?.sendMessageToUser(slackUser, sendMessagePanel.getMessage(), null)
    }
    else {
      val fileName = FileDocumentManager.getInstance().getFile(editor.document)?.name ?: "unknown.txt"
      slackMessages?.postMessageWithCodeSnippet(slackUser, sendMessagePanel.getMessage(), codeSnippet, fileName)
    }

    super.doOKAction()
  }
}