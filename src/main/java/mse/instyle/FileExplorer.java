package mse.instyle;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

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
        assert files != null;
        for (File file : files) {
            if (FilenameUtils.getExtension(file.getName()).equals("md")) {
                mdFiles.add(file);
            }
        }
        return mdFiles;
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