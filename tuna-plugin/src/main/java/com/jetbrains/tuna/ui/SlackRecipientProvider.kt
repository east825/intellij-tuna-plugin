package com.jetbrains.tuna.ui

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.project.Project
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackSession

/**
 * Use for messages to complete @mentions and #channels.
 */
class SlackRecipientProvider(project: Project) : TextFieldWithAutoCompletionListProvider<SlackRecipient>(null) {
  private val slackSession: SlackSession? = TunaProjectComponent.getInstance(project).slackSession

  override fun getLookupString(item: SlackRecipient): String {
    return item.toString()
  }

  override fun getItems(prefix: String?, cached: Boolean, parameters: CompletionParameters?): MutableCollection<SlackRecipient> {
    val result = mutableListOf<SlackRecipient>()
    if (prefix == null || slackSession == null) {
      return result
    }
    result.addAll(slackSession.users.map { slackUser -> SlackUserRecipient(slackUser) })
    result.addAll(slackSession.channels.filter { ch -> ch.name != null }.map { ch -> SlackChannelRecipient(ch) })
    return result
  }
}