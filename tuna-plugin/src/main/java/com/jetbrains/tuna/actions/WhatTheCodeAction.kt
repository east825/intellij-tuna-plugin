package com.jetbrains.tuna.actions


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.jetbrains.tuna.ui.ChooseSlackUserToSendMessageBalloon

class WhatTheCodeAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: TODO("What should we do without project?")

    val editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val caretModel = editor.caretModel
    val selectedText = editor.selectionModel.selectedText
    if (selectedText.isNullOrBlank()) {
      ChooseSlackUserToSendMessageBalloon(project, caretModel.logicalPosition).showWindow()
    }
  }
}