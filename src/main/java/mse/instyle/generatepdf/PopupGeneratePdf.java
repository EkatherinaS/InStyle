package mse.instyle.generatepdf;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mse.instyle.Configuration;
import mse.instyle.FileExplorer;
import mse.instyle.InStyleException;
import mse.instyle.ValidationStatus;
import mse.instyle.structurevalidator.DialogValidationResult;
import mse.instyle.structurevalidator.StructureValidator;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class PopupGeneratePdf extends AnAction {

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

    private String generateSection(File file) throws InStyleException {

        try {
            String markdownText = new String(Files.readAllBytes(file.toPath()));
            String cssPath = FileExplorer.getInstance().getCssPath() + "/" + FileExplorer.getInstance().getCssFilename();
            String cssText = new String(Files.readAllBytes(Path.of(cssPath)));
            String tempDir = FileExplorer.getInstance().getTempDirectory();

            new File(tempDir).mkdirs();

            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdownText);
            HtmlRenderer renderer = HtmlRenderer.builder().build();

            String htmlContent = renderer.render(document);
            String css = "<style>" + cssText + "</style>";
            String fullHtml = "<html><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><head>" + css + "</head><body>" + htmlContent + "</body></html>";

            //Write to a temporary HTML file
            File tempHtmlFile = File.createTempFile("temp", ".html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempHtmlFile))) {
                writer.write(fullHtml);
            }

            File outputPdf = new File(tempDir + "/" + file.getName() + ".pdf");

            //Run wkhtmltopdf
            ProcessBuilder pb = new ProcessBuilder("wkhtmltopdf",
                    tempHtmlFile.getAbsolutePath(), outputPdf.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            //Cleanup and output result
            tempHtmlFile.delete();
            return outputPdf.getAbsolutePath();
        }
        catch (Exception e) {
            throw new InStyleException("Error generating pdf: " + e);
        }
    }

    private String generatePdf() throws InStyleException {
        ArrayList<File> files = FileExplorer.getInstance().getAllMdFiles();
        String tempDir = FileExplorer.getInstance().getTempDirectory();
        String resultPath = FileExplorer.getInstance().getResultPdfPath();
        try {
            Files.deleteIfExists(Path.of(resultPath));
        } catch (IOException e) {
            throw new InStyleException("Error deleting file: " + e);
        }

        PDFMergerUtility ut = new PDFMergerUtility();
        for (File file : files) {
            String path = generateSection(file);
            try {
                ut.addSource(path);
            } catch (FileNotFoundException e) {
                throw new InStyleException("Error adding section pdf to result: " + e);
            }
        }
        ut.setDestinationFileName(resultPath);
        try {
            ut.mergeDocuments(null);
        } catch (IOException e) {
            throw new InStyleException("Error merging pdf to result: " + e);
        }
        try {
            FileUtils.deleteDirectory(new File(tempDir));
        } catch (IOException e) {
            throw new InStyleException("Error deleting temp directory: " + e);
        }
        return resultPath;
    }

    public void actionPerformed(@NotNull AnActionEvent event) {
        FileExplorer.getInstance().createFileExplorer(project.getBasePath());
        try {
            String path = generatePdf();
            Messages.showInfoMessage("Pdf created: " + path, "Pdf Created");
        } catch (InStyleException e) {
            Messages.showErrorDialog("Error validating file: " + e.getMessage(), "Error");
        }
    }
}