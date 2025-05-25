package xyz.bobindustries.film.projects.elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.api.awt.AWTSequenceEncoder;


import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.projects.elements.exceptions.CouldntDeleteImageException;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

public class Project {
    private final Path projectDir;
    private final String projectName;

    private String scenarioContent;
    private final List<ImageFile> images;

    public Project(String projectName, String projectDirPath, boolean create)
            throws IOException, InvalidProjectDirectoryException {
        this.projectName = projectName;
        this.projectDir = Paths.get(projectDirPath);
        this.images = new ArrayList<>();
        loadProject(create);
    }

    public Project(String projectName, File projectDirFile, boolean create)
            throws IOException, InvalidProjectDirectoryException {
        this.projectName = projectName;
        this.projectDir = projectDirFile.toPath();
        this.images = new ArrayList<>();
        loadProject(create);
    }

    public Project() {
        this.projectDir = null;
        this.projectName = "null_project";
        this.images = new ArrayList<>();
    }

    private void loadProject(boolean create) throws IOException, InvalidProjectDirectoryException {
        if (create) {
            File scenarioFile = new File(projectDir.toString(), "scenario.txt");
            if (!scenarioFile.createNewFile()) {
                SimpleErrorDialog.show("Couldn't create scenario.txt file.");
            }

            File imagesDir = new File(projectDir.toString(), "images");
            if (!imagesDir.mkdir()) {
                SimpleErrorDialog.show("Couldn't create images directory");
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

    public String getProjectName() {
        return projectName;
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

    @SuppressWarnings("unused")
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
    
    public void exportAsVideo(List<Pair<ImageFile, Double>> data) throws IOException {
        if (data.isEmpty()) {
            throw new IOException("Data is empty!");
        }

        Path outputDir = Paths.get(projectDir + "/output/");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        System.out.println("[?] rendering video (20fps) at " + projectDir + "/output/" + projectName + ".mp4 \n data size: " + data.size());

        AWTSequenceEncoder encoder;

        try {
            encoder = AWTSequenceEncoder.createSequenceEncoder(new File(projectDir + "/output/" + projectName + ".mp4"), 20);

            for (int i = 0; i < data.size(); i++) {
                Pair<ImageFile, Double> frame = data.get(i);
                if (frame.key() == null) {
                    System.err.println("Warning: Frame " + i + " has a null image. Skipping.");
                    continue;
                }
                if (frame.value() <= 0) {
                    System.err.println("Warning: Frame " + i + " has a non-positive duration. Skipping.");
                    continue;
                }

                int numberOfVideoFrames = (int) Math.max(1, Math.round(frame.value() * 20));

                System.out.println("[?] encoding frame " + i + " (" + numberOfVideoFrames + " frames)");
                for (int j = 0; j < numberOfVideoFrames; j++) {
                    encoder.encodeImage(frame.key().getBufferedImage());
                }
            }

            encoder.finish();

        } catch (IOException e) {
            System.err.println("IOException during video encoding: " + e.getMessage());
            throw e;
        }
    }

    public String getToolTipImageText(ImageFile imageFile) {
        File file = new File(projectDir + "/images/" + imageFile.getFileName());

        try {
            if (file.exists()) {
                URL imageURL = file.toURI().toURL();

                System.out.println(imageURL);
                return "<html><img src='" + imageURL + "' width='120' height='80'></html>";
            }
            else {
                return "Image not found!";
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteImage(ImageFile imf) throws CouldntDeleteImageException {
        if (!images.contains(imf)) {
            throw new CouldntDeleteImageException("image not found in project memory.");
        }

        images.remove(imf);

        Path imagePath = projectDir.resolve("images/" + imf.getFileName());
        if (!Files.exists(imagePath)) {
            throw new CouldntDeleteImageException("image file not found.");
        }


        try {
            Path garbageDir = projectDir.resolve("images/.garbage");
            if (!Files.exists(garbageDir)) {
                Files.createDirectories(garbageDir);
            }

            Path garbageFilePath = garbageDir.resolve(imf.getFileName());
            Files.copy(imagePath, garbageFilePath);

            Files.delete(imagePath);
        } catch (IOException e) {
            throw new CouldntDeleteImageException("error deleting image file: " + e.getMessage());
        }

        if (scenarioContent != null) {
            String[] lines = scenarioContent.split(System.lineSeparator());
            StringBuilder updatedContent = new StringBuilder();
            for (String line : lines) {
                if (!line.split(",")[0].equals(imf.getFileName())) {
                    updatedContent.append(line).append(System.lineSeparator());
                }
            }
            scenarioContent = updatedContent.toString().trim();
        }

        System.out.println("[+] deleted image " + imf.getFileName());
    }
}
