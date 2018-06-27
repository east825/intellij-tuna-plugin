package com.jetbrains.tuna;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "TunaConfig", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class TunaProjectComponent implements ProjectComponent, PersistentStateComponent<TunaProjectComponent.Config> {
    public static TunaProjectComponent getInstance(@NotNull Project project) {
        return project.getComponent(TunaProjectComponent.class);
    }

    @NotNull
    private Project myProject;
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
}
