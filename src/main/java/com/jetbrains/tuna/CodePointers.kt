package com.jetbrains.tuna

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtilCore

const val DEFAULT_BUILTIN_SERVER_PORT = 63342

fun getBuiltinServerUrl(editor: Editor): String? {
  val virtualFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return null
  val baseDir = editor.project?.baseDir ?: return null
  val relativePath = VfsUtilCore.getRelativePath(virtualFile, baseDir)
  val line = editor.caretModel.logicalPosition.line
  val column = editor.caretModel.logicalPosition.column
  return "http://localhost:$DEFAULT_BUILTIN_SERVER_PORT/api/file/$relativePath:$line:$column"
}