package mse.instyle.generatepdf;

import com.fasterxml.jackson.databind.JsonNode;
import mse.instyle.Configuration;
import mse.instyle.FileExplorer;
import mse.instyle.InStyleException;
import mse.instyle.Templates;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;

public class ContentsCreator {

    private static ArrayList<String> parseStructure() throws InStyleException {
        Configuration conf = Configuration.getInstance();
        JsonNode structureNode = conf.getStructure();
        ArrayList<String> including = new ArrayList<>();

        if (structureNode == null) {
            throw new InStyleException("Configuration not loaded");
        }

        for (final JsonNode objNode : structureNode) {
            String title = objNode.get("title").asText();
            boolean inContents = objNode.get("included_in_contents").asBoolean();
            if (inContents) {
                including.add(title);
            }
        }

        return including;
    }

    public static String createHtmlListOfContents() throws InStyleException, IOException {
        Dictionary<String, ArrayList<String>> filesWithHeaders = FileExplorer.getInstance().getHeadersByFiles();
        File[] files = FileExplorer.getInstance().getAllTempFiles();
        ArrayList<String> toInclude = parseStructure();
        int currentPage = 1;
        PDDocument doc;

        StringBuilder html = new StringBuilder("<h1>Содержание</h1>");
        String oneLine = """
                            <div class="row">
                                <div class="left">%s</div>
                                <div class="separator"></div>
                                <div class="right">%s</div>
                            </div>
                         """;

        for (File file : files) {
            if (filesWithHeaders.get(file.getName().replace("pdf", "md")) == null) {
                continue;
            }
            String header = filesWithHeaders.get(file.getName().replace("pdf", "md")).get(0);
            if (toInclude.contains(header)) {
                html.append(String.format(oneLine, header, currentPage));
            }
            doc = Loader.loadPDF(file);
            currentPage += doc.getNumberOfPages();
        }

        return html.toString();
    }

    public static String getContentsCss() throws InStyleException, IOException {
        Configuration conf = Configuration.getInstance();
        String cssPath = FileExplorer.getInstance().getCssPath() + "/" + FileExplorer.getInstance().getCssFilename();
        String cssText = new String(Files.readAllBytes(Path.of(cssPath)));

        JsonNode styleNode = conf.getStyle();
        if (styleNode == null) {
            throw new InStyleException("Configuration not loaded");
        }
        String separator = styleNode.get("default").get("contents-separator").asText();
        String contentsCss = Templates.getContentsCss().replace("{separator}", separator.repeat(50));
        return "<style>\n" + cssText + "\n" + contentsCss + "\n</style>";
    }
}
