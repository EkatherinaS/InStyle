package mse.instyle.structurevalidator;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mse.instyle.*;
import mse.instyle.csseditor.CssFileEditor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

public class PopupStructureNotifications extends AnAction {

    private Project project;

    @Override
    public void update(AnActionEvent e) {
        project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public void actionPerformed(@NotNull AnActionEvent event) {
        FileExplorer.getInstance().createFileExplorer(project.getBasePath());
        StructureValidator validator = new StructureValidator();
        Configuration.getInstance().createConfiguration();

        try {
            ArrayList<ValidationStatus> result = validator.checkStructure();
            DialogValidationResult dialog = new DialogValidationResult(result);
            dialog.showAndGet();
        } catch (InStyleException e) {
            Messages.showErrorDialog("Error validating file: " + e.getMessage(), "Error");
        }
    }
}