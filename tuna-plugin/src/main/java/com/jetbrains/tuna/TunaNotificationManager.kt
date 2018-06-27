package com.jetbrains.tuna


import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
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
            override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler,
                                           exitCode: Int) {
                addNotification(TunaNotification("Process $env finished with exit code $exitCode", ""))
            }
        })

//        connection.subscribe(GitRepository.GIT_REPO_CHANGE, object: GitRepositoryChangeListener {
//            override fun repositoryChanged(rep: GitRepository) {
//                addNotification(TunaNotification(rep.info.toString(), "in git repository"))
//            }
//        })

        // Successful branch check out
        connection.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, object : BranchChangeListener {
            override fun branchWillChange(branchName: String) {
            }

            override fun branchHasChanged(branchName: String) {
                addNotification(TunaNotification("Branch %s checked out".format(branchName), ""))
            }
        })

        // Indexing
        connection.subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun enteredDumbMode() {
                addNotification(TunaNotification("Indexing started", ""))
            }

            override fun exitDumbMode() {
                addNotification(TunaNotification("Indexing finished", ""))
            }
        })
    }

    private fun addNotification(notification: TunaNotification) {
        notificationsShown.add(notification)
    }
}