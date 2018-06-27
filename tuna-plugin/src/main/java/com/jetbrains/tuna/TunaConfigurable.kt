package com.jetbrains.tuna

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import com.jetbrains.tuna.oauth.Server
import org.jetbrains.annotations.Nls
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
                           "redirect_uri=${urlEncode("http://localhost:8000/oauth/callback")}")

          myAccessToken = interceptCodeAndRequestToken()
        }
      }
    }
  }

  private fun interceptCodeAndRequestToken(): String? {
    val server = Server.start(8000)
    try {
      val code = server.getCode()

      // todo request token
      return null
    } finally {
      server.shutdown()
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
