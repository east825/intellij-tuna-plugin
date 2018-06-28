package com.jetbrains.tuna.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackUser
import javax.swing.JComponent

class SendMessageDialog(private val project: Project,
                        initialSlackUser: SlackUser? = null,
                        codeSnippet: CodeSnippet? = null) : DialogWrapper(project) {
  private val receiverName = JBTextField(20)
  private val codeSnippetTextField: EditorTextField?
  private val messageField: EditorTextField = createMessageTextField(project).apply { }

  private var slackUser: SlackUser? = initialSlackUser

  init {
    initialSlackUser?.let {
      receiverName.text = "To: ${it.userName}"
      receiverName.isEnabled = false
    }

    codeSnippetTextField = codeSnippet?.let {
      createCodeSnippetTextField(project, codeSnippet)
    }?.apply { text = codeSnippet.editor.document.text }

    init()
    title = "Send to Slack"
  }

  override fun createCenterPanel(): JComponent? {
    return panel {
      codeSnippetTextField?.let { row { it(CCFlags.grow, CCFlags.push) } }
      row { messageField(CCFlags.grow, CCFlags.push) }
      row { receiverName(CCFlags.pushX) }
    }
  }

  override fun doOKAction() {
    val slackUser = slackUser ?: return
    val slackMessages = TunaProjectComponent.getInstance(project).slackMessages
    slackMessages?.postMessageWithCodeSnippet(slackUser, messageField.text, codeSnippetTextField?.text ?: "")

    super.doOKAction()
  }
}