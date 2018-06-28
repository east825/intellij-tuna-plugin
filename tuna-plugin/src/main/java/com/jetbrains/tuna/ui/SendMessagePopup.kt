package com.jetbrains.tuna.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper.DEFAULT_ACTION
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.layout.*
import com.intellij.ui.layout.CCFlags.*
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackUser
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.border.EmptyBorder

class SendMessagePopup(private val project: Project,
                       private val slackUser: SlackUser?,
                       private val editor: Editor) {
  private val sendMessagePanel: SendMessagePanel = SendMessagePanel(project, slackUser, editor)
  private val myBalloon: JBPopup

  init {
    val sendButton = JButton(SendAction())

    val panel = panel {
      row { sendMessagePanel(grow, push) }
      row {
        right {
          sendButton(pushX)
        }
      }
    }.apply { border = EmptyBorder(10, 10, 0, 10) }

    myBalloon = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, sendMessagePanel.preferableFocusComponent)
      .setProject(project)
      .setResizable(false)
      .setModalContext(false)
      .setCancelOnClickOutside(false)
      .setRequestFocus(true)
      .setCancelKeyEnabled(true)
      /*
            .setCancelCallback {
              saveSearchText()
              saveLocation()
              true
            }
      */
      .addUserData("SIMPLE_WINDOW")
      .setResizable(true)
      .setMovable(true)
      .createPopup()

    myBalloon.setMinimumSize(Dimension(300, 50))
  }

  fun showInBestPositionForEditor() {
    myBalloon.showInBestPositionFor(editor)
  }

  inner class SendAction : AbstractAction("Send to Slack") {
    init {
      putValue(DEFAULT_ACTION, true)
    }

    override fun actionPerformed(e: ActionEvent) {
      val slackUser = slackUser ?: return
      val slackMessages = TunaProjectComponent.getInstance(project).slackMessages

      val codeSnippet = sendMessagePanel.getCodeSnippet()
      if (codeSnippet == null) {
        slackMessages?.session?.sendMessageToUser(slackUser, sendMessagePanel.getMessage(), null)
      }
      else {
        slackMessages?.postMessageWithCodeSnippet(slackUser, sendMessagePanel.getMessage(), codeSnippet)
      }

      myBalloon.closeOk(null)
    }
  }
}