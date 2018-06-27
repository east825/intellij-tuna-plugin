package com.jetbrains.tuna.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class SendMessageDialog(project: Project) : DialogWrapper(project) {
    private val receiverName = JBTextField()
    private val messageField = JBTextField(20)

    init {
        init()
        title = "Send Message"
    }

    override fun createCenterPanel(): JComponent? {
        return panel {
            row("To:") { receiverName(CCFlags.pushX) }
            row("Message:") { messageField(CCFlags.pushX) }
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        // TODO
    }

}