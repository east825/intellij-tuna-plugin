package com.jetbrains.tuna

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.ui.layout.*
import com.sun.net.httpserver.HttpServer
import org.jetbrains.annotations.Nls
import java.net.InetSocketAddress
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
    val code: Ref<String> = Ref.create()
    // TODO use normal HTTP server for this purpose, e.g. Netty
    val server = HttpServer.create(InetSocketAddress(8000), 0)
    server.createContext("/oauth/callback") {
      code.set(it.requestURI.query.split("&")
                 .firstOrNull() { it.startsWith("code=") }
                 ?.removePrefix("code="))
      // TODO send response back to the browser
      server.stop(0)
    }
    server.executor = null
    server.start()
    if (!code.isNull) {
      // Request Access Token
    }
    return null
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
