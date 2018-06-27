package com.jetbrains.tuna

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.roots.ui.configuration.ProjectConfigurable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.layout.*
import org.jetbrains.annotations.Nls
import java.net.URLEncoder

import javax.swing.*

class TunaConfigurable : Configurable {

  @Nls(capitalization = Nls.Capitalization.Title)
  override fun getDisplayName(): String {
    return "Tuna Settings"
  }

  override fun createComponent(): JComponent? {
    return panel {
      row {
        link("Authorize in Slack") {
          BrowserUtil.open("${TunaAppInfo.OAUTH_AUTHORIZE_URL}?" +
                           "client_id=${URLEncoder.encode(TunaAppInfo.CLIENT_ID, "utf-8")}&" +
                           "scope=incoming-webhook&" +
                           "redirect_uri=${URLEncoder.encode("http://localhost:8000/oauth/callback", "utf-8")}")
        }
      }
    }
  }

  override fun isModified(): Boolean {
    return false
  }

  @Throws(ConfigurationException::class)
  override fun apply() {

  }
}
