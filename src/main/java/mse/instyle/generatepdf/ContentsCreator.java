package mse.instyle.generatepdf;

import com.fasterxml.jackson.databind.JsonNode;
import mse.instyle.Configuration;
import mse.instyle.FileExplorer;
import mse.instyle.InStyleException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            String header = filesWithHeaders.get(file.getName().replace("pdf", "md")).get(0);
            if (toInclude.contains(header)) {
                html.append(String.format(oneLine, header, currentPage));
            }
            doc = Loader.loadPDF(file);
            currentPage += doc.getNumberOfPages();
        }

        return html.toString();
    }
}
