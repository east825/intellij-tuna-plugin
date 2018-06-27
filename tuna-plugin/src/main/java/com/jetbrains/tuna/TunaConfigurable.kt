package com.jetbrains.tuna

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.jetbrains.tuna.oauth.ClientRequest
import com.jetbrains.tuna.oauth.Server
import org.jetbrains.annotations.Nls
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.JComponent


class TunaConfigurable(private val myProject: Project) : Configurable {
  private val myTunaComponent = TunaProjectComponent.getInstance(myProject)
  private val myAccessTokenField = JBTextField()

  @Nls(capitalization = Nls.Capitalization.Title)
  override fun getDisplayName(): String {
    return "Tuna Settings"
  }

  override fun createComponent(): JComponent? {
    return panel {
      row("Access token:") { myAccessTokenField(CCFlags.pushX) }
      row {
        link("Authorize in Slack") {
          BrowserUtil.open("${TunaAppInfo.OAUTH_AUTHORIZE_URL}?" +
                           "client_id=${urlEncode(TunaAppInfo.CLIENT_ID)}&" +
                           // TODO find fine-grained scopes for all these activities
                           // rtm:stream is required for simple-slack-api library
                           // it's granted together with client scope
                           "scope=${urlEncode("read post client")}&" +
                           "redirect_uri=${urlEncode(TunaAppInfo.REDIRECT_URI)}")

          val app = ApplicationManager.getApplication()
          val modality = ModalityState.stateForComponent(myAccessTokenField)

          app.executeOnPooledThread {
            val code = interceptCodeAndRequestToken()
            if (code != null) {
              myTunaComponent.restartSession()
              app.invokeLater({ myAccessTokenField.text = code }, modality)
            }
          }
        }
      }
    }
  }

  private fun interceptCodeAndRequestToken(): String? {
    val server = Server.start(8000)
    try {
      val code = server.getCode()
      return requestToken(code)
    }
    finally {
      server.shutdown()
    }
  }

  private fun requestToken(code: String): String? {
    val gson = GsonBuilder().create()
    val payload = hashMapOf(
      "client_id" to TunaAppInfo.CLIENT_ID,
      "client_secret" to TunaAppInfo.CLIENT_SECRET,
      "code" to code,
      "redirect_uri" to TunaAppInfo.REDIRECT_URI
    )

    val formEncoded = payload.entries
      .map { "${urlEncode(it.key)}=${urlEncode(it.value)}" }
      .joinToString(separator = "&")

    println("Request: $formEncoded")

    val response = ClientRequest().post(TunaAppInfo.OAUTH_ACCESS_TOKEN_URL, formEncoded)

    val responseContent = response.content().toString(Charsets.UTF_8)

    println("Response: $responseContent")
    val json = gson.fromJson(responseContent, JsonElement::class.java).asJsonObject
    return json["access_token"].asString
  }


  private fun urlEncode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())

  override fun reset() {
    myAccessTokenField.text = myTunaComponent.accessToken
  }

  override fun isModified(): Boolean {
    return myTunaComponent.accessToken != myAccessTokenField.text
  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    myTunaComponent.accessToken = myAccessTokenField.text
  }
}
