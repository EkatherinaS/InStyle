package mse.instyle.structurevalidator;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DialogValidationResult extends DialogWrapper {

    private final ArrayList<JLabel> result;

    public DialogValidationResult(ArrayList<ValidationStatus> list) {
        super(true);
        setTitle("Результат Проверки");

        result = new ArrayList<>();
        for (ValidationStatus vs : list) {
            JLabel label;
            switch (vs.code()) {
                case 1: {
                    label = new JLabel("Warning: " + vs.message());
                    label.setForeground(JBColor.yellow.darker());
                    break;
                }
                case 2: {
                    label = new JLabel("Error: " + vs.message());
                    label.setForeground(JBColor.RED.darker());
                    break;
                }
                default: {
                    label = new JLabel("Success: " + vs.message());
                    label.setForeground(JBColor.green.darker());
                    break;
                }
            }
            result.add(label);
        }
        init();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));

        for (JLabel label : result) {
            dialogPanel.add(label, BorderLayout.LINE_START);
        }

        return dialogPanel;
    }
}