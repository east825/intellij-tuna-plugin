package com.jetbrains.tuna.ui

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaCodeFragmentFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.*
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import javax.swing.border.CompoundBorder

fun createCodeSnippetTextField(project: Project, codeSnippet: CodeSnippet): EditorTextField {
  val multiline = true

  return object :
          EditorTextField(createDocument(project, codeSnippet.editor.document.text, null), project, JavaFileType.INSTANCE, true, !multiline) {
    override fun createEditor(): EditorEx {
      val editor = super.createEditor()
      editor.setHorizontalScrollbarVisible(multiline)
      editor.setVerticalScrollbarVisible(multiline)
      editor.settings.isUseSoftWraps = true
      editor.settings.lineCursorWidth = EditorUtil.getDefaultCaretWidth()
      editor.colorsScheme.editorFontName = font.fontName
      editor.colorsScheme.editorFontSize = font.size
      editor.contentComponent.border = CompoundBorder(editor.contentComponent.border, JBUI.Borders.emptyLeft(2))
      return editor
    }

    override fun getPreferredSize(): Dimension {
      return Dimension(600, 400)
    }

    override fun getData(dataId: String?): Any? {
      if (LangDataKeys.CONTEXT_LANGUAGES.`is`(dataId)) {
        return arrayOf(JavaFileType.INSTANCE.language)
      } else if (CommonDataKeys.PSI_FILE.`is`(dataId)) {
        return PsiDocumentManager.getInstance(getProject()).getPsiFile(document)
      }
      return super.getData(dataId)
    }
  }
}

fun createExpressionCodeFragment(project: Project, expression: String, context: PsiElement?, isPhysical: Boolean): PsiFile {
  return JavaCodeFragmentFactory.getInstance(project).createExpressionCodeFragment(expression, context, null, isPhysical)
}

fun createDocument(project: Project, expression: String, context: PsiElement?): Document {
  val codeFragment = createExpressionCodeFragment(project, expression, context, true)
  val document = PsiDocumentManager.getInstance(project).getDocument(codeFragment)!!
  return document
}

fun createMessageTextField(project: Project): EditorTextField {
  val features = hashSetOf<EditorCustomization>()
  features.add(SoftWrapsEditorCustomization.ENABLED)
  val textField = EditorTextFieldProvider.getInstance().getEditorField(FileTypes.PLAIN_TEXT.language, project, features)
  TextFieldWithAutoCompletion.installCompletion(textField.document, project, SlackRecipientProvider(project), false)
  return textField
}
