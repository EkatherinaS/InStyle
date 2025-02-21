package mse.instyle.generatepdf;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.codeInsight.daemon.impl.DaemonTooltipRendererProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mse.instyle.*;
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
    private String fullHtml = "<html><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><head>%s</head><body>%s</body></html>";


    @Override
    public void update(AnActionEvent e) {
        project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private File createHtmlFile(String name, String contents, String css) throws IOException {
        //Write to a temporary HTML file
        File tempHtmlFile = File.createTempFile(name, ".html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempHtmlFile))) {
            writer.write(String.format(fullHtml, contents, css));
        }
        return tempHtmlFile;
    }

    private File generatePdfFromHtml(String name, String htmlPath) throws InterruptedException, IOException {
        String tempDir = FileExplorer.getInstance().getTempDirectory();
        File outputPdf = new File(tempDir + "/" + name + ".pdf");

        Configuration conf = Configuration.getInstance();
        JsonNode styleNode = conf.getStyle();

        String left = "20mm";
        String right = "20mm";
        String top = "30mm";
        String bottom = "15mm";

        if (styleNode != null && styleNode.get("default") != null) {
            left = styleNode.get("default").get("margin-left").asText();
            right = styleNode.get("default").get("margin-right").asText();
            top = styleNode.get("default").get("margin-top").asText();
            bottom = styleNode.get("default").get("margin-bottom").asText();
        }

        //Run wkhtmltopdf
        ProcessBuilder pb = new ProcessBuilder("wkhtmltopdf", "--disable-smart-shrinking", "--dpi", "300",
                "-L", left, "-R", right, "-T", top, "-B", bottom,
                htmlPath, outputPdf.getPath());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();

        return outputPdf;
    }

    private String generateSection(File file) throws InStyleException {

        try {
            String markdownText = new String(Files.readAllBytes(file.toPath()));
            String cssPath = FileExplorer.getInstance().getCssPath() + "/" + FileExplorer.getInstance().getCssFilename();
            String cssText = new String(Files.readAllBytes(Path.of(cssPath)));

            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdownText);
            HtmlRenderer renderer = HtmlRenderer.builder().build();

            String htmlContent = renderer.render(document);
            String css = "<style>" + cssText + "</style>";
            String header = file.getName().replace(".md", "");

            File htmlFile = createHtmlFile(header, htmlContent, css);
            File pdfFile = generatePdfFromHtml(header, htmlFile.getPath());

            htmlFile.delete();
            return pdfFile.getPath();
        }
        catch (Exception e) {
            throw new InStyleException("Error generating pdf: " + e);
        }
    }

    private String generatePdf() throws InStyleException, IOException, InterruptedException {
        ArrayList<File> files = FileExplorer.getInstance().getAllMdFiles();
        String tempDir = FileExplorer.getInstance().getTempDirectory();
        String resultPath = FileExplorer.getInstance().getResultPdfPath();
        try {
            Files.deleteIfExists(Path.of(resultPath));
        } catch (IOException e) {
            throw new InStyleException("Error deleting file: " + e);
        }

        PDFMergerUtility ut = new PDFMergerUtility();
        ArrayList<String> filePaths = new ArrayList<>();
        for (File file : files) {
            filePaths.add(generateSection(file));
        }

        try {
            String htmlContents = ContentsCreator.createHtmlListOfContents();
            String contentsCss = ContentsCreator.getContentsCss();
            File htmlFile = createHtmlFile("contents", htmlContents, contentsCss);
            File pdfFile = generatePdfFromHtml("contents", htmlFile.getPath());
            ut.addSource(pdfFile.getPath());
        } catch (FileNotFoundException e) {
            throw new InStyleException("Error adding section pdf to result: " + e);
        }

        for (String path : filePaths) {
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
            String tempDir = FileExplorer.getInstance().getTempDirectory();
            new File(tempDir).mkdirs();
            String path = generatePdf();
            Messages.showInfoMessage("Pdf created: " + path, "Pdf Created");
        } catch (InStyleException | IOException | InterruptedException e) {
            Messages.showErrorDialog("Error validating file: " + e.getMessage(), "Error");
        }
    }
}