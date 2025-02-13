package mse.instyle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileExplorer {
    private String path;
    public String getConfigPath() {
        return path + "/instyle-config.json";
    }

    public String getBuildprofilesPath() {
        return  path + "/Writerside/cfg/buildprofiles.xml";
    }

    public String getCssFilename() {
        return "custom.css";
    }

    public String getCssPath() {
        return path + "/Writerside/cfg/static";
    }

    public ArrayList<File> getAllMdFiles() {
        File directory = new File(path + "/Writerside/topics");
        File[] files = directory.listFiles();
        ArrayList<File> mdFiles = new ArrayList<>();
        for (File file : files) {
            if (FilenameUtils.getExtension(file.getName()).equals("md")) {
                mdFiles.add(file);
            }
        }
        return mdFiles;
    }

    public File[] getAllTempFiles() {
        File directory = new File(getTempDirectory());
        return directory.listFiles();
    }

    public ArrayList<String> getNewPageHeaders() throws InStyleException {
        Dictionary<String, ArrayList<String>> headersByFiles = getHeadersByFiles();
        ArrayList<File> files = getAllMdFiles();
        ArrayList<String> firstHeaders = new ArrayList<>();

        for (File file : files) {
            firstHeaders.add(headersByFiles.get(file.getName()).get(0));
        }

        return firstHeaders;
    }

    public ArrayList<String> getAllHeaders() throws InStyleException {
        Dictionary<String, ArrayList<String>> headersByFiles = getHeadersByFiles();
        ArrayList<File> files = getAllMdFiles();
        ArrayList<String> allHeaders = new ArrayList<>();

        for (File file : files) {
            allHeaders.addAll(headersByFiles.get(file.getName()));
        }

        return allHeaders;
    }

    public Dictionary<String, ArrayList<String>> getHeadersByFiles() throws InStyleException {
        ArrayList<File> files = FileExplorer.getInstance().getAllMdFiles();
        Dictionary<String, ArrayList<String>> headers = new Hashtable<>();

        if (files != null) {
            for (File file : files) {
                try {
                    String contents = new String(Files.readAllBytes(file.toPath()));
                    Pattern pattern = Pattern.compile("#.+(?=\\n|$)");
                    Matcher matcher = pattern.matcher(contents);
                    while (matcher.find()) {
                        String title = contents.substring(matcher.start(), matcher.end());
                        title = StringUtils.stripStart(title, "# ");
                        if (headers.get(file.getName()) != null) {
                            headers.get(file.getName()).add(title);
                        }
                        else {
                            headers.put(file.getName(), new ArrayList<>(Arrays.asList(title)));
                        }
                    }
                } catch (IOException e) {
                    throw new InStyleException("Cannot read md file: " + e);
                }
            }
        }
        return headers;
    }

    public String getTempDirectory() {
        return path + "/temp";
    }

    public String getResultPdfPath() {
        return path + "/result.pdf";
    }

    public static class FileExplorerHolder {
        public static final FileExplorer HOLDER_INSTANCE = new FileExplorer();
    }

    public static FileExplorer getInstance() {
        return FileExplorer.FileExplorerHolder.HOLDER_INSTANCE;
    }

    public void createFileExplorer(String projectPath) {
        path = projectPath;
    }
}