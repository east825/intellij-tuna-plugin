package com.jetbrains.tuna

import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsListener

data class TunaNotification(val title: String, val text: String)

class TunaNotificationManager(private val project: Project) {
    var notificationsShown: MutableList<TunaNotification> = mutableListOf()

    fun initProjectListeners() {
        val connection = project.messageBus.connect()

        connection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, VcsListener {
            addNotification(TunaNotification("Something changed", "in VCS"))
        })

        // use for tests, but doesn't work for Rebuild
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler,
                                           exitCode: Int) {
                addNotification(TunaNotification(executorId, "The process is finished with exit code {}"
                        .format(exitCode)))
            }
        })
    }

    private fun addNotification(notification: TunaNotification) {
        notificationsShown.add(notification)
    }
}