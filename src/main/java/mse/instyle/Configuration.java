package mse.instyle;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Configuration {

    private JsonNode style;
    private JsonNode structure;

    public JsonNode getStyle() {
        return style;
    }

    public JsonNode getStructure() {
        return structure;
    }

    public static class ConfigurationHolder {
        public static final Configuration HOLDER_INSTANCE = new Configuration();
    }

    public static Configuration getInstance() {
        return ConfigurationHolder.HOLDER_INSTANCE;
    }

    public void createConfiguration() {
        String path = FileExplorer.getInstance().getConfigPath();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        style = jsonNode.get("style");
        structure = jsonNode.get("structure");
    }
}