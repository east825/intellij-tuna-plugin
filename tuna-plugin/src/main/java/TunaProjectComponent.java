import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TunaProjectComponent implements ProjectComponent {
    @NotNull
    private Project myProject;

    TunaProjectComponent(@NotNull Project project) {
        myProject = project;
    }

    @Override
    public void projectOpened() {
        initProjectListeners();
    }

    private void initProjectListeners() {

    }
}
