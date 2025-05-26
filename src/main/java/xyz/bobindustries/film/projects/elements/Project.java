package xyz.bobindustries.film.projects.elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jcodec.api.awt.AWTSequenceEncoder;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ConfigProvider;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

public class Project {
    private final Path projectDir;
    private final String projectName;

    private static Properties config;
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

    public static Properties getConfig() {
        return config;
    }

    public Properties loadProperties() {
        config = ConfigProvider.loadProperties(this);
        return config;
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

            File properties = new File(projectDir.toString(), ".config.properties");
            if (!properties.createNewFile()) {
                SimpleErrorDialog.show("Couldn't create .config.properties file.");
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
                        images.add(new ImageFile(imagePath.getFileName().toString(), imagePath.toString()));
                        System.out.println("Added image " + imagePath.getFileName() + " to project " + projectName);
                    }
                }
            }
        }

        loadProperties();
    }

    public Path getProjectDir() {
        return projectDir;
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

    /*
     * public void addOrUpdateImage(String fileName, byte[] content) {
     * =======
     * 
     * @SuppressWarnings("unused")
     * public void addOrUpdateImage(String fileName, byte[] content) {
     * >>>>>>> eac57ec07a5f1f6336eaf54755edf452859c05ba
     * boolean found = false;
     * for (ImageFile img : images) {
     * if (img.getFileName().equals(fileName)) {
     * img.setContent(content);
     * found = true;
     * break;
     * }
     * }
     * 
     * if (!found) {
     * images.add(new ImageFile(fileName, content));
     * }
     * }
     */

    public void addImage(ImageFile imageFileToAdd) {
        System.out.println("image added");
        System.out.println("past img add;" + imageFileToAdd.getPath());
        images.add(imageFileToAdd);
        System.out.println("size:" + images.size());
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

    public void exportAsVideo(List<FrameData> data) throws IOException {
        if (data.isEmpty()) {
            throw new IOException("Data is empty!");
        }

        Path outputDir = Paths.get(projectDir + "/output/");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        AWTSequenceEncoder encoder;

        try {
            encoder = AWTSequenceEncoder.createSequenceEncoder(new File(projectDir + "/output/" + projectName + ".mp4"),
                    20);

            for (FrameData frame : data) {
                if (frame.getImageFile() == null) {
                    System.err.println("Warning: Frame " + data.indexOf(frame) + " has a null image. Skipping.");
                    continue;
                }
                if (frame.getDuration() <= 0) {
                    System.err.println(
                            "Warning: Frame " + data.indexOf(frame) + " has a non-positive duration. Skipping.");
                    continue;
                }

                int numberOfVideoFrames = (int) Math.max(1, Math.round(frame.getDuration() * 20));

                for (int i = 0; i < numberOfVideoFrames; i++) {
                    encoder.encodeImage(frame.getImageFile().getBufferedImage());
                }
            }

            encoder.finish();
        } catch (IOException e) {
            System.err.println("IOException during video encoding: " + e.getMessage());
            throw e;
        }
    }

}
