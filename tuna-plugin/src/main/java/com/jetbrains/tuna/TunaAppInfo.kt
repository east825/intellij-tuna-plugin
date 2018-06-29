package com.jetbrains.tuna

import java.util.*

object TunaAppInfo {
  val CLIENT_ID by lazy { loadCredentials("tuna.slack.app.client.id") }
  val CLIENT_SECRET by lazy { loadCredentials("tuna.slack.app.client.secret") }

  private fun loadCredentials(key: String): String {
    System.getProperty(key)?.let { return it }

    val storage = Properties()
    javaClass.getResourceAsStream("/credentials.properties").use {
      storage.load(it)
    }
    return storage.getProperty(key)
  }

  const val OAUTH_AUTHORIZE_URL = "https://slack.com/oauth/authorize"
  const val OAUTH_ACCESS_TOKEN_URL = "https://slack.com/api/oauth.access"
  const val REDIRECT_URI = "http://localhost:8000/oauth/callback"
}