package com.jetbrains.tuna.ui

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackUser

interface SlackRecipient

const val SLACK_USER_PREFIX = "@"
const val SLACK_CHANNEL_PREFIX = "#"

class SlackUserRecipient(val slackUser: SlackUser) : SlackRecipient {
  override fun toString(): String = SLACK_USER_PREFIX + slackUser.userName

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SlackUserRecipient

    if (slackUser.userName != other.slackUser.userName) return false

    return true
  }

  override fun hashCode(): Int {
    return slackUser.hashCode()
  }
}

class SlackChannelRecipient(private val slackChannel: SlackChannel) : SlackRecipient {
  override fun toString(): String = SLACK_CHANNEL_PREFIX + slackChannel.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SlackChannelRecipient

    if (slackChannel.name != other.slackChannel.name) return false

    return true
  }

  override fun hashCode(): Int {
    return slackChannel.hashCode()
  }
}