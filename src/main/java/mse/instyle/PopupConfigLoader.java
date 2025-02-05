package mse.instyle;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mse.instyle.csseditor.CssFileEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class PopupConfigLoader extends AnAction {

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
        String path = project.getBasePath();
        FileExplorer.getInstance().createFileExplorer(path);
        assert path != null;
        File workingDirectory = new File(path);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл конфигурации:");
        fileChooser.setCurrentDirectory(workingDirectory);
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                new File(FileExplorer.getInstance().getConfigPath());
                FileWriter writer = new FileWriter(FileExplorer.getInstance().getConfigPath());
                writer.write(content);
                writer.close();
                Messages.showMessageDialog(content, "Файл Загружен", Messages.getInformationIcon());

                XmlFileEditor.editBuildprofiles();
                Configuration.getInstance().createConfiguration();
                CssFileEditor.createCssFile();
            }
            catch (IOException | InStyleException e) {
                Messages.showErrorDialog("Error reading file: " + e.getMessage(), "Error");
            }
        }
    }
}