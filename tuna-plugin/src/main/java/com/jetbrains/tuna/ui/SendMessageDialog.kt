package com.jetbrains.tuna.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackUser
import javax.swing.JComponent

@Deprecated("Use `SendMessagePopup` instead")
class SendMessageDialog(private val project: Project,
                        private val initialSlackUser: SlackUser? = null,
                        private val editor: Editor) : DialogWrapper(project) {
  private val sendMessagePanel: SendMessagePanel = SendMessagePanel(project, initialSlackUser, editor)

  init {
    init()
    title = "Send to Slack"
  }

  override fun createCenterPanel(): JComponent? {
    return sendMessagePanel
  }

  override fun doOKAction() {
    val slackUser = initialSlackUser ?: return
    val slackMessages = TunaProjectComponent.getInstance(project).slackMessages
    slackMessages?.postMessageWithCodeSnippet(slackUser, sendMessagePanel.getMessage(), sendMessagePanel.getCodeSnippet() ?: "")

    super.doOKAction()
  }
}