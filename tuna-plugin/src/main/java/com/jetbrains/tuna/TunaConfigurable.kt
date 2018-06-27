package com.jetbrains.tuna

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.intellij.util.io.HttpRequests
import com.jetbrains.tuna.oauth.Server
import org.jetbrains.annotations.Nls
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.JComponent


class TunaConfigurable(val myProject: Project) : Configurable {
  val myAccessTokenField = JBTextField()

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
                           "scope=incoming-webhook&" +
                           "redirect_uri=${urlEncode(TunaAppInfo.REDIRECT_URI)}")

          myAccessTokenField.text = interceptCodeAndRequestToken()
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
      "client_id" to "387915614917.388681237990",
      "client_secret" to "3dbdbd7210ef6d86f76a155c6bba8cb6",
      "code" to code,
      "redirect_uri" to "http://localhost:8000/oauth/callback"
    )
    val formEncoded = payload.entries
      .map { "${urlEncode(it.key)}=${urlEncode(it.value)}" }
      .joinToString(separator = "&")
    return HttpRequests.request("https://slack.com/api/oauth.access")
      .tuner {
        if (it is HttpURLConnection) {
          it.doOutput = true
          it.requestMethod = "POST"
        }
      }
      .connect { request ->
        val writer = (request.connection as HttpURLConnection).outputStream.bufferedWriter()
        writer.write(formEncoded)
        writer.flush()

        //      println(request.readString(null))
        val json = gson.fromJson(request.reader, JsonElement::class.java).asJsonObject
        return@connect json["access_token"].asString
      }
  }


  private fun urlEncode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())

  override fun reset() {
    val component = TunaProjectComponent.getInstance(myProject)
    myAccessTokenField.text = component.state?.myAccessToken
  }

  override fun isModified(): Boolean {
    val component = TunaProjectComponent.getInstance(myProject)
    return component.state?.myAccessToken != myAccessTokenField.text
  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    val component = TunaProjectComponent.getInstance(myProject)
    component.state?.myAccessToken = myAccessTokenField.text
  }
}
