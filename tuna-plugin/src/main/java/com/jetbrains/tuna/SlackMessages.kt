package com.jetbrains.tuna

import com.ullink.slack.simpleslackapi.SlackChatConfiguration
import com.ullink.slack.simpleslackapi.SlackSession


class SlackMessages(private val session: SlackSession) {
  fun sendMessageToCurrentUser(message: String, asBot: Boolean) {
    val current = session.sessionPersona()
    val reply = session.openDirectMessageChannel(session.findUserById(current.id))
    val channel = reply.reply.slackChannel
    val config = if (asBot) {
      SlackChatConfiguration.getConfiguration()
    }
    else {
      SlackChatConfiguration.getConfiguration().asUser()
    }
    session.sendMessage(channel, message, null, config);
  }
}