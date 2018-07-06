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
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


data class TunaNotification(val title: String, val text: String)

class TunaNotificationManager(private val project: Project) {
    @Volatile
    var prevIndexingStart: Long = -1L
    @Volatile
    var prevIndexingFinish: Long = -1L
    val indexingPause: Long = 2000
    var executorService: ExecutorService = ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>())


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
                val currentTime = System.currentTimeMillis()
                if (prevIndexingStart < 0L) {
                    sendFirstStart(TunaNotification("Indexing Started", ""))
                }
                prevIndexingStart = currentTime
                if (!(prevIndexingStart > prevIndexingFinish && (prevIndexingStart - prevIndexingFinish) < indexingPause)) {
                    addNotification(TunaNotification("Indexing Started", ""))
                }
            }

            override fun exitDumbMode() {
                val currentTime = System.currentTimeMillis()
                if (prevIndexingStart < 0L) return
                prevIndexingFinish = currentTime
                executorService.execute({
                    try {
                        TimeUnit.MILLISECONDS.sleep(indexingPause)
                        if (!(prevIndexingStart > prevIndexingFinish && (prevIndexingStart - prevIndexingFinish) < indexingPause)) {
                            if (currentTime >= prevIndexingFinish) {
                                addNotification(TunaNotification("Indexing Finished :tada:", ""))
                                prevIndexingStart = -1L
                            }
                        }
                    } catch (e: InterruptedException) {
                    }
                })

            }
        })

        // Compilation and rebuild
        CompilerManager.getInstance(project).addAfterTask(CompileTask { context ->
            addNotification(TunaNotification("Compilation Finished", ""))
            return@CompileTask true
        })
    }

    private fun sendFirstStart(notification: TunaNotification) {
        executorService.execute({
            try {
                val start = System.currentTimeMillis()
                var current = System.currentTimeMillis()
                val tunaProjectComponent = TunaProjectComponent.getInstance(project)
                while (tunaProjectComponent.slackMessages == null && (current - start) < indexingPause) {
                    TimeUnit.MILLISECONDS.sleep(100)
                    current = System.currentTimeMillis()
                }
                if (tunaProjectComponent.slackMessages != null) {
                    addNotification(notification)
                }
            } catch (e: InterruptedException) {
            }
        })
    }

    private fun addNotification(notification: TunaNotification) {
        val isActive = ApplicationManager.getApplication().isActive
        if (!isActive) {
            TunaProjectComponent.getInstance(project).slackMessages?.
                    sendMessageToCurrentUser("Project '${project.name}': ${notification.title}", asBot = true)
        }
    }
}