package com.jetbrains.tuna

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.ullink.slack.simpleslackapi.SlackChatConfiguration
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.SlackUser
import java.net.URL
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.ImageIcon


class SlackMessages(val session: SlackSession) {
  private val myIconCache = CacheBuilder.newBuilder()
    .build(object : CacheLoader<String, Icon>() {
      override fun load(userId: String?): Icon {
        val reply = session.postGenericSlackCommand(mapOf("user" to userId), "users.info")
        val plainAnswer = reply.reply.plainAnswer
        val elem = GsonBuilder().create().fromJson(plainAnswer, JsonElement::class.java)
        val url = elem.asJsonObject["user"].asJsonObject["profile"].asJsonObject["image_32"].asString
        return ImageIcon(URL(url))
      }
    })


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
    session.sendMessage(channel, message, null, config)
  }

  fun getUserIcon(userId: String): Icon? {
    try {
      return myIconCache[userId]
    }
    catch (e: ExecutionException) {
      TunaProjectComponent.LOG.error(e)
    }
    return null
  }

  fun postMessageWithCodeSnippet(user: SlackUser, message: String, snippet: String) {
    val handler = session.openDirectMessageChannel(user)
    handler.waitForReply(5, TimeUnit.SECONDS)
    val reply = handler.reply
    if (!reply.isOk) {
      println("Failed to open DM to ${user.userName}: ${reply.errorMessage}")
    }
    val channel = reply.slackChannel
    session.sendFile(channel, snippet.toByteArray(), "file.name", "This is Snippet Title", message)
  }
}