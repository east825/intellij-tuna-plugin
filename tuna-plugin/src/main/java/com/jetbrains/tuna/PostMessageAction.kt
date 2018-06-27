package com.jetbrains.tuna

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages

class PostMessageAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val message = Messages.showInputDialog(e.project, "Enter message", "Enter message", null)
    val tuna = e.project!!.getComponent(TunaProjectComponent::class.java)
    val session = tuna.slackSession ?: return
    ApplicationManager.getApplication().executeOnPooledThread {
      val user = session.findUserByUserName("qsolo825")
      if (user != null) {
        session.sendMessageToUser(user, message, null)
      }
    }
  }
}
