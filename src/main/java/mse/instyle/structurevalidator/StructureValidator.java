package mse.instyle.structurevalidator;

import com.fasterxml.jackson.databind.JsonNode;
import mse.instyle.Configuration;
import mse.instyle.FileExplorer;
import mse.instyle.InStyleException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureValidator {
    private static class Section {
        String title;
        Boolean newPage;
        Boolean mandatory;

        public Section(String title, Boolean newPage, Boolean mandatory) {
            this.title = title;
            this.newPage = newPage;
            this.mandatory = mandatory;
        }

        public String getTitle() {
            return title;
        }

        public Boolean getNewPage() {
            return newPage;
        }

        public Boolean getMandatory() {
            return mandatory;
        }
    }
    private ArrayList<Section> structure;

    public void parseStructure() throws InStyleException {
        Configuration conf = Configuration.getInstance();
        JsonNode structureNode = conf.getStructure();
        if (structureNode == null) {
            throw new InStyleException("Configuration not loaded");
        }
        structure = new ArrayList<>();

        for (final JsonNode objNode : structureNode) {
            String title = objNode.get("title").asText();
            Boolean start_new_page = objNode.has("start_new_page") && objNode.get("start_new_page").asBoolean();
            Boolean mandatory = objNode.has("mandatory") && objNode.get("mandatory").asBoolean();
            Section section = new Section(title, start_new_page, mandatory);
            structure.add(section);
        }
    }

    public ArrayList<ValidationStatus> checkStructure() throws InStyleException {
        parseStructure();
        ArrayList<String> headers = FileExplorer.getInstance().getAllHeaders();
        ArrayList<String> newPageHeaders = FileExplorer.getInstance().getNewPageHeaders();
        ArrayList<ValidationStatus> result = new ArrayList<>();

        for (Section section : structure) {
            if (!headers.contains(section.getTitle())) {
                if (section.getMandatory()) {
                    result.add(new ValidationStatus(2, "Отсутствует обязательный раздел " + section.getTitle()));
                }
                else {
                    result.add(new ValidationStatus(1, "Отсутствует опциональный раздел " + section.getTitle()));
                }
            }
            else if (section.getNewPage() != newPageHeaders.contains(section.getTitle())) {
                if (section.getNewPage()) {
                    result.add(new ValidationStatus(2, "Раздел " + section.getTitle() + " должен начинаться с новой страницы"));
                }
                else {
                    result.add(new ValidationStatus(1, "Раздел " + section.getTitle() + " не обязан начинаться с новой страницы"));
                }
            }
        }
        if (result.isEmpty()) {
            result.add(new ValidationStatus(0, "Структура соответствует заданной"));
        }
        return result;
    }
}
