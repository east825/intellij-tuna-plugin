package com.jetbrains.tuna

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import com.intellij.util.io.HttpRequests
import com.jetbrains.tuna.oauth.Server
import org.jetbrains.annotations.Nls
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.JComponent


class TunaConfigurable(val myProject: Project) : Configurable {
  var myAccessToken: String? = null

  @Nls(capitalization = Nls.Capitalization.Title)
  override fun getDisplayName(): String {
    return "Tuna Settings"
  }

  override fun createComponent(): JComponent? {
    return panel {
      row {
        link("Authorize in Slack") {
          val s = TunaAppInfo.CLIENT_ID
          BrowserUtil.open("${TunaAppInfo.OAUTH_AUTHORIZE_URL}?" +
                           "client_id=${urlEncode(s)}&" +
                           "scope=incoming-webhook&" +
                           "redirect_uri=${urlEncode(TunaAppInfo.REDIRECT_URI)}")

          myAccessToken = interceptCodeAndRequestToken()
        }
      }
    }
  }

  private fun interceptCodeAndRequestToken(): String? {
    val server = Server.start(8000)
    try {
      val code = server.getCode()
      ApplicationManager.getApplication().executeOnPooledThread {
        requestToken(code)
      }
      return "foo"
    }
    finally {
      server.shutdown()
    }
  }

  private fun requestToken(code: String): String? {
//    val code = "387915614917.388797643460.9b444b2b0a5205627e727a6e5fcda634478e19c39de3ddeede29bdfdc9665bc2"
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


  override fun reset() {
    val component = TunaProjectComponent.getInstance(myProject)
    myAccessToken = component.state?.myAccessToken
  }

  private fun urlEncode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())

  override fun isModified(): Boolean {
    val component = TunaProjectComponent.getInstance(myProject)
    return component.state?.myAccessToken != myAccessToken
  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    val component = TunaProjectComponent.getInstance(myProject)
    component.state?.myAccessToken = myAccessToken
  }
}
