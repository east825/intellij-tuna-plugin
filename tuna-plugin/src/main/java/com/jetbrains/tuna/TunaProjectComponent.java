package com.jetbrains.tuna;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.twelvemonkeys.lang.StringUtil;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@State(name = "TunaConfig", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class TunaProjectComponent implements ProjectComponent, PersistentStateComponent<TunaProjectComponent.Config> {
  public static final Logger LOG = Logger.getInstance(TunaProjectComponent.class);

  public static TunaProjectComponent getInstance(@NotNull Project project) {
    return project.getComponent(TunaProjectComponent.class);
  }

  @NotNull
  private Project myProject;

  private SlackSession mySlackSession;
  private SlackMessages mySlackMessages;

  @NotNull
  private TunaNotificationManager myNotificationManager;

  private Config myConfig = new Config();

  TunaProjectComponent(@NotNull Project project) {
    myProject = project;
    myNotificationManager = new TunaNotificationManager(myProject);
  }

  @Override
  public void projectOpened() {
    myNotificationManager.initProjectListeners();
    if (myConfig.myAccessToken != null && !myConfig.myAccessToken.isEmpty()) {
      restartSession();
    }
    else {
      showBalloon(myProject);
    }
  }

  private void showBalloon(Project project) {
    Notification notification = new Notification("Tuna plugin", "Tuna plugin",
            "You are not authorised", NotificationType.WARNING);
    notification.addAction(new DumbAwareAction("Authorise in Slack") {
      @Override
      public void actionPerformed(AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, TunaConfigurable.class);
        notification.expire();
      }
    });

    notification.notify(project);
  }

  @Nullable
  @Override
  public Config getState() {
    return myConfig;
  }

  @Override
  public void loadState(@NotNull Config state) {
    XmlSerializerUtil.copyBean(state, myConfig);
  }

  static class Config {
    public String myAccessToken;
  }

  @Nullable
  public SlackSession getSlackSession() {
    return mySlackSession;
  }

  @Nullable
  public SlackMessages getSlackMessages() {
    if (mySlackMessages == null && mySlackSession != null && mySlackSession.isConnected()) {
      mySlackMessages = new SlackMessages(mySlackSession);
    }
    return mySlackMessages;
  }

  @Nullable
  public String getAccessToken() {
    return myConfig.myAccessToken;
  }

  public void setAccessToken(@Nullable String token) {
    myConfig.myAccessToken = token;
    if (mySlackSession != null && StringUtil.isEmpty(token)) {
      destroySession();
    }
    else if (mySlackSession == null && !StringUtil.isEmpty(token)) {
      restartSession();
    }
  }

  public void restartSession() {
    if (mySlackSession != null) {
      destroySession();
    }
    mySlackSession = SlackSessionFactory.getSlackSessionBuilder(myConfig.myAccessToken).build();
    new Task.Backgroundable(myProject, "Initializing Connection with Slack", false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          indicator.setText2("Initializing connection");
          mySlackSession.connect();
          mySlackMessages = new SlackMessages(mySlackSession);
          indicator.setText("Pre-fetching User Icons");
          for (SlackUser user: mySlackSession.getUsers()) {
            indicator.setText2("Fetching icon for " + user.getUserName());
            mySlackMessages.getUserIcon(user.getId());
          }
        }
        catch (IOException e) {
          LOG.error(e);
          mySlackSession = null;
          mySlackMessages = null;
        }
      }
    }.queue();
  }

  private void destroySession() {
    SlackSession existing = mySlackSession;
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        existing.disconnect();
      }
      catch (IOException e) {
        LOG.error(e);
      }
    });
    mySlackSession = null;
    mySlackMessages = null;
  }
}
