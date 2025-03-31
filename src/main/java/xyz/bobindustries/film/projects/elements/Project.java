package xyz.bobindustries.film.projects.elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

public class Project {

    class ImageFile {
        private String fileName;
        private byte[] content;

        public ImageFile(String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

    }

    private Path projectDir;
    private String scenarioContent;

    private List<ImageFile> images;

    public Project(String projectDirPath, boolean create) throws IOException, InvalidProjectDirectoryException {
        this.projectDir = Paths.get(projectDirPath);
        this.images = new ArrayList<>();
        loadProject(create);
    }

    public Project(File projectDirFile, boolean create) throws IOException, InvalidProjectDirectoryException {
        this.projectDir = projectDirFile.toPath();
        this.images = new ArrayList<>();
        loadProject(create);
    }

    private void loadProject(boolean create) throws IOException, InvalidProjectDirectoryException {
        if (create) {
            File scenarioFile = new File(projectDir.toString(), "scenario.txt");
            if (!scenarioFile.createNewFile()) {
                SimpleErrorDialog.showErrorDialog("Couldn't create scenario.txt file.");
            }

            File imagesDir = new File(projectDir.toString(), "images");
            if (!imagesDir.mkdir()) {
                SimpleErrorDialog.showErrorDialog("Couldn't create images directory");
            }
        }

        Path scenarioPath = projectDir.resolve("scenario.txt");

        if (Files.exists(scenarioPath)) {
            scenarioContent = new String(Files.readAllBytes(scenarioPath));
        } else {
            throw new InvalidProjectDirectoryException();
        }

        Path imagesDir = projectDir.resolve("images");
        if (Files.exists(imagesDir) && Files.isDirectory(imagesDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(imagesDir)) {
                for (Path imagePath : stream) {
                    if (Files.isRegularFile(imagePath)) {
                        byte[] content = Files.readAllBytes(imagePath);
                        images.add(new ImageFile(imagePath.getFileName().toString(), content));
                    }
                }
            }
        }
    }

    public String getScenarioContent() {
        return scenarioContent;
    }

    public void setScenarioContent(String scenarioContent) {
        this.scenarioContent = scenarioContent;
    }

    public List<ImageFile> getImages() {
        return images;
    }

    public void addOrUpdateImage(String fileName, byte[] content) {
        boolean found = false;
        for (ImageFile img : images) {
            if (img.getFileName().equals(fileName)) {
                img.setContent(content);
                found = true;
                break;
            }
        }

        if (!found) {
            images.add(new ImageFile(fileName, content));
        }
    }

    public void save() throws IOException {
        Path scenarioPath = projectDir.resolve("scenario.txt");
        Files.write(scenarioPath, scenarioContent.getBytes());

        Path imagesDir = projectDir.resolve("images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }

        for (ImageFile img : images) {
            Path imagePath = imagesDir.resolve(img.getFileName());
            Files.write(imagePath, img.getContent());
        }
    }

}
