package com.jetbrains.tuna

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

class PostMessageAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val component = e.project!!.getComponent(TunaProjectComponent::class.java)
    val token = component.state!!.myAccessToken ?: return
    val message = Messages.showInputDialog(e.project, "Enter message", "Enter message", null)
    ApplicationManager.getApplication().executeOnPooledThread {
      val session = SlackSessionFactory.getSlackSessionBuilder(token).build()
      session.connect()
      val user = session.findUserByUserName("qsolo825")
      if (user != null) {
        session.sendMessageToUser(user, message, null)
      }
    }
  }
}
