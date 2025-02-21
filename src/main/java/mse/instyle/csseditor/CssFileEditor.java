package mse.instyle.csseditor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mse.instyle.Configuration;
import mse.instyle.FileExplorer;
import mse.instyle.InStyleException;
import mse.instyle.Templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CssFileEditor {

    private static Path createFile() throws InStyleException {
        try {
            String path = FileExplorer.getInstance().getCssPath();
            String file = FileExplorer.getInstance().getCssFilename();

            Files.createDirectories(Path.of(path));
            String filePath = path + "/" + file;
            Path cssPath = Path.of(filePath);

            File cssFile = new File(filePath);
            cssFile.createNewFile();

            return cssPath;
        }
        catch (IOException e) {
            throw new InStyleException("Cannot create file: " + e);
        }
    }

    private static StringBuilder createVariablesCss() throws InStyleException {
        StringBuilder css = new StringBuilder();
        String defaultNode = Configuration.getInstance().getStyle().get("default").toString();
        String headersNode = Configuration.getInstance().getStyle().get("headers").toString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        try {
            Map<String, Object> defaultValues = objectMapper.readValue(defaultNode, new TypeReference<>() {});
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                css.append(String.format("--%s-%s: %s;\n", "default", entry.getKey(), entry.getValue()));
            }
        } catch (JsonProcessingException e) {
            throw new InStyleException("Error reading default from configuration: " + e);
        }

        try {
            Map<String, Map<String, Object>> headerValues = objectMapper.readValue(headersNode, new TypeReference<>() {});
            for (Map.Entry<String, Map<String, Object>> entry : headerValues.entrySet()) {
                for (Map.Entry<String, Object> val : entry.getValue().entrySet()) {
                    css.append(String.format("--%s-%s: %s;\n", entry.getKey(), val.getKey(), val.getValue()));
                }
            }
        } catch (JsonProcessingException e) {
            throw new InStyleException("Error reading headers from configuration: " + e);
        }

        return css;
    }

    private static StringBuilder createHeadersCss() throws InStyleException {
        StringBuilder css = new StringBuilder();
        String headersNode = Configuration.getInstance().getStyle().get("headers").toString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        String templateHeader = Templates.getTemplateHeaderCss();

        try {
            Map<String, Map<String, Object>> headerValues = objectMapper.readValue(headersNode, new TypeReference<>() {});
            for (Map.Entry<String, Map<String, Object>> entry : headerValues.entrySet()) {
                css.append(templateHeader.replaceAll("\\{num}", entry.getKey()));
            }
            return css;
        }
        catch (JsonProcessingException e) {
            throw new InStyleException("Error reading headers from configuration: " + e);
        }
    }

    private static StringBuilder createSimpleCss() throws InStyleException {
        StringBuilder cssBody = new StringBuilder();
        StringBuilder cssP = new StringBuilder();
        String defaultNode = Configuration.getInstance().getStyle().get("default").toString();
        String headersNode = Configuration.getInstance().getStyle().get("headers").toString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        try {
            Map<String, Object> defaultValues = objectMapper.readValue(defaultNode, new TypeReference<>() {});
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                if (entry.getKey().startsWith("font")) {
                    cssP.append(String.format("\t%s: %s;\n", entry.getKey(), entry.getValue()));
                }
                else {
                    cssBody.append(String.format("\t%s: %s;\n", entry.getKey(), entry.getValue()));
                }
            }
        } catch (JsonProcessingException e) {
            throw new InStyleException("Error reading default from configuration: " + e);
        }

        StringBuilder css = new StringBuilder();
        css.append("body {\n");
        css.append(cssBody);
        css.append("}\np {\n");
        css.append(cssP);
        css.append("}\nol {\n");
        css.append(cssP);
        css.append("}\nul {\n");
        css.append(cssP);
        css.append("}\ndiv {\n");
        css.append(cssP);
        css.append("}\n");

        try {
            Map<String, Map<String, Object>> headerValues = objectMapper.readValue(headersNode, new TypeReference<>() {});
            for (Map.Entry<String, Map<String, Object>> entry : headerValues.entrySet()) {
                css.append(entry.getKey());
                css.append(" { \n");
                for (Map.Entry<String, Object> val : entry.getValue().entrySet()) {
                    css.append(String.format("\t%s: %s;\n", val.getKey(), val.getValue()));
                }
                css.append("}\n");
            }
        } catch (JsonProcessingException e) {
            throw new InStyleException("Error reading headers from configuration: " + e);
        }

        return css;
    }

    public static void createCssFile() throws InStyleException {
        //StringBuilder variablesCss = createVariablesCss();
        //StringBuilder headersCss = createHeadersCss();

        //String result = Templates.getTemplateCss().replaceAll("\\{variables}", String.valueOf(variablesCss)) + headersCss;

        StringBuilder result = createSimpleCss();
        Path cssPath = createFile();
        try {
            Files.write(cssPath, result.toString().getBytes());
        } catch (IOException e) {
            throw new InStyleException("Error writing to file: " + e);
        }
    }
}