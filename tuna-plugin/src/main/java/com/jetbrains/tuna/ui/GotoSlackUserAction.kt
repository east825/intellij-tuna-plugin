package com.jetbrains.tuna.ui

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.gotoByName.ChooseByNameBase
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.ide.util.gotoByName.SimpleChooseByNameModel
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.Processor
import com.intellij.util.text.MatcherHolder
import com.intellij.util.ui.UIUtil
import com.jetbrains.tuna.SlackMessages
import com.jetbrains.tuna.TunaProjectComponent
import com.ullink.slack.simpleslackapi.SlackUser
import javax.swing.JList
import javax.swing.ListCellRenderer

class GotoSlackUserAction : GotoActionBase() {
  override fun gotoActionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val component = project.getComponent(TunaProjectComponent::class.java) ?: return
    val slackMessages = component.slackMessages ?: return
    val model = SlackUserPopupModel(project, slackMessages)
    val provider = SlackUserItemProvider(slackMessages)

    val editor = e.getRequiredData(CommonDataKeys.EDITOR)

    val popup = ChooseByNamePopup.createPopup(project, model, provider)
    popup.setShowListForEmptyPattern(true)
    popup.isSearchInAnyPlace = true
    showNavigationPopup(object : GotoActionCallback<SlackUser>() {
      override fun elementChosen(popup: ChooseByNamePopup?, element: Any?) {
        if (element !is SlackUser) {
          // todo show error message dialog
          return
        }
        val sendMessageDialog = SendMessageDialog(project, element, CodeSnippet(editor))

        ApplicationManager.getApplication().invokeLater {
          sendMessageDialog.showAndGet()
        }
      }
    }, null, popup)
  }

  class SlackUserPopupModel(project: Project, val slackMessages: SlackMessages) : SimpleChooseByNameModel(project, "Select user:", null) {
    override fun getElementsByName(name: String?, pattern: String?): Array<Any> = emptyArray()

    override fun getNames(): Array<String> = emptyArray()

    override fun getListCellRenderer(): ListCellRenderer<*> {
      return object : ColoredListCellRenderer<SlackUser>() {
        override fun customizeCellRenderer(list: JList<out SlackUser>,
                                           value: SlackUser?,
                                           index: Int,
                                           selected: Boolean,
                                           hasFocus: Boolean) {
          if (value != null) {
            val matcher = MatcherHolder.getAssociatedMatcher(list)
            val selectedBackground = UIUtil.getListSelectionBackground()
            icon = slackMessages.getUserIcon(value.id)
            SpeedSearchUtil.appendColoredFragmentForMatcher(value.userName, this,
                                                            SimpleTextAttributes.REGULAR_ATTRIBUTES, matcher, selectedBackground, selected)
            if (!value.realName.isNullOrEmpty()) {
              append(" ")
              SpeedSearchUtil.appendColoredFragmentForMatcher("(${value.realName})",
                                                              this, SimpleTextAttributes.GRAYED_ATTRIBUTES, matcher, selectedBackground,
                                                              selected)
            }
          }
        }
      }
    }

    override fun getElementName(element: Any?): String? = (element as? SlackUser)?.userName
  }

  class SlackUserItemProvider(private var messages: SlackMessages) : ChooseByNameItemProvider {
    override fun filterElements(base: ChooseByNameBase,
                                pattern: String,
                                everywhere: Boolean,
                                cancelled: ProgressIndicator,
                                consumer: Processor<Any>): Boolean {
      if (messages.session.isConnected) {
        // For some reason this builder needs to be built twice,
        // even though it's already created in ChooseByNameBased for use in cell renderer
        val matcher = NameUtil.buildMatcher("*$pattern", NameUtil.MatchingCaseSensitivity.NONE)
        messages.session.users
          .filter { matcher.matches(it.userName.orEmpty()) || matcher.matches(it.realName.orEmpty()) }
          .forEach {
            // fetch icon on a pooled thread
            messages.getUserIcon(it.id)
            consumer.process(it)
          }
      }
      return true
    }

    override fun filterNames(base: ChooseByNameBase, names: Array<out String>, pattern: String) = emptyList<String>()
  }

}
