package com.jetbrains.tuna.ui

import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import javax.swing.JTextArea

class ChooseSlackUserToSendMessageBalloon(private val project: Project, private val logicalPosition: LogicalPosition) {
  fun showWindow() {
    val slackRecipientTextField = JBTextField()

    val panel = FormBuilder.createFormBuilder()
      .addLabeledComponent("To:", slackRecipientTextField)
      .addLabeledComponent("Message:", JTextArea())
      .panel

    val myBalloon = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, slackRecipientTextField)
      //      .setProject(myProject)
      .setResizable(false)
      .setModalContext(false)
      .setCancelOnClickOutside(true)
      .setRequestFocus(true)
      .setCancelKeyEnabled(false)
      /*
            .setCancelCallback {
              saveSearchText()
              saveLocation()
              true
            }
      */
      .addUserData("SIMPLE_WINDOW")
      .setResizable(true)
      .setMovable(true)
      .createPopup()

    myBalloon.setMinimumSize(Dimension(300, 50))
    myBalloon.showInFocusCenter()
  }
}