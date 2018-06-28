package com.jetbrains.tuna

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages

class PostMessageToYourselfAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val message = Messages.showInputDialog(e.project, "Enter message", "Enter message", null).orEmpty()
    val tuna = e.project!!.getComponent(TunaProjectComponent::class.java)
    val session = tuna.slackMessages ?: return
    ApplicationManager.getApplication().executeOnPooledThread {
      session.sendMessageToCurrentUser(message, asBot = false)
    }
  }
}
