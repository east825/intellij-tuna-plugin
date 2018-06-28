package com.jetbrains.tuna


import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.BranchChangeListener


data class TunaNotification(val title: String, val text: String)

class TunaNotificationManager(private val project: Project) {
    var notificationsShown: MutableList<TunaNotification> = mutableListOf()

    fun initProjectListeners() {
        val connection = project.messageBus.connect()

        // Running Run configurations
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
                addNotification(TunaNotification("Process '$env' finished with exit code $exitCode", ""))
            }
        })

        // Successful branch check out
        connection.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, object : BranchChangeListener {
            override fun branchWillChange(branchName: String) {
            }

            override fun branchHasChanged(branchName: String) {
                addNotification(TunaNotification("Branch '$branchName' checked out", ""))
            }
        })

        // Indexing
        connection.subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun enteredDumbMode() {
                addNotification(TunaNotification("Indexing Started", ""))
            }

            override fun exitDumbMode() {
                addNotification(TunaNotification("Indexing Finished", ""))
            }
        })

        // Compilation and rebuild
        CompilerManager.getInstance(project).addAfterTask(CompileTask { context ->
            addNotification(TunaNotification("Compilation Finished", ""))
            return@CompileTask true
        })

    }

    private fun addNotification(notification: TunaNotification) {
        val isActive = ApplicationManager.getApplication().isActive
        if (!isActive) {
            TunaProjectComponent.getInstance(project).slackMessages?.sendMessageToCurrentUser(notification.title, asBot = true)
        }
    }
}