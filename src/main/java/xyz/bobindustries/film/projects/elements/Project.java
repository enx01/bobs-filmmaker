package xyz.bobindustries.film.projects.elements;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jcodec.api.awt.AWTSequenceEncoder;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.projects.ConfigProvider;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.projects.elements.exceptions.CouldntDeleteImageException;
import xyz.bobindustries.film.projects.elements.exceptions.ImageNotFoundInDirectoryException;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;
import xyz.bobindustries.film.utils.ImageUtils;

public class Project {
    private final Path projectDir;
    private final String projectName;

    private Properties config;
    private String scenarioContent;
    private final List<ImageFile> images;

    public final static double MAX_TIME = 1,
            MIN_TIME = 0.1;

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

    public Properties getConfig() {
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

    public void addImage(ImageFile imageFileToAdd) {
        for (ImageFile img : images) {
            if (img.getFileName().equals(imageFileToAdd.getFileName())) {
                images.remove(img);
                break;
            }
        }

        images.add(imageFileToAdd);
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yy_HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        Path outputDir = Paths.get(projectDir + "/output/");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        System.out.println("[?] rendering video (20fps) at " + projectDir + "/output/" + projectName + timestamp
                + ".mp4 \n data size: " + data.size());

        AWTSequenceEncoder encoder;

        try {
            encoder = AWTSequenceEncoder.createSequenceEncoder(
                    new File(projectDir + "/output/" + projectName + timestamp + ".mp4"),
                    20);

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

    /**
     * Get a formatted html String which can be set as a tooltip to serve as an
     * image preview.
     * 
     * @param imageFile the image to preview
     * @return the formatted html String
     */
    public String getToolTipImageText(ImageFile imageFile) {
        File file = new File(projectDir + "/images/" + imageFile.getFileName());

        try {
            if (file.exists()) {
                URL imageURL = file.toURI().toURL();

                return "<html><img src='" + imageURL + "' width='120' height='80'></html>";
            } else {
                return "Image not found!";
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes an image from the project.
     * This moves the image file to a images/.garbage directory
     * and deletes all its occurences from the scenario content.
     * 
     * @param imf ImageFile to delete.
     */
    public void deleteImage(ImageFile imf) throws CouldntDeleteImageException, IOException {
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
            if (Files.exists(garbageFilePath)) {
                Files.delete(garbageFilePath);
            }
            Files.copy(imagePath, garbageFilePath);

            Files.delete(imagePath);
        } catch (IOException e) {
            throw new CouldntDeleteImageException("error deleting image file: " + e.getMessage());
        }

        if (!images.contains(imf)) {
            throw new CouldntDeleteImageException("image not found in project memory.");
        }

        images.remove(imf);

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

        save();

        System.out.println("[+] deleted image " + imf.getFileName());
    }

    /**
     * Creates a new empty image in the images/ directory.
     * 
     * @param imageName name of the new blank image.
     */
    public void createNewEmptyFile(String imageName) {
        int height = ConfigProvider.getResolutionHeight(getConfig());
        int width = ConfigProvider.getResolutionWidth(getConfig());
        Color[][] defaultCanvas = new Color[height][width];
        for (int i = 0; i < defaultCanvas.length; i += 1) {
            for (int j = 0; j < defaultCanvas[0].length; j += 1) {
                defaultCanvas[i][j] = new Color(255, 255, 255);
            }
        }

        String projectPath = getProjectDir().toString();
        ImageUtils.exportImage(defaultCanvas, projectPath + "/images/" + imageName);
    }

    /**
     * Deletes all lines where the image name corresponds to the given name.
     * 
     * @param imageName name of the image to remove from the scenario content.
     */
    public void deleteOccurrencesFromScenario(String imageName) {
        if (scenarioContent != null) {
            String[] lines = scenarioContent.split(System.lineSeparator());
            StringBuilder updatedContent = new StringBuilder();
            for (String line : lines) {
                if (!line.split(",")[0].equals(imageName)) {
                    updatedContent.append(line).append(System.lineSeparator());
                }
            }
            scenarioContent = updatedContent.toString().trim();

        }
    }

    /**
     * Deletes a given line from the scenario content.
     * 
     * @param line index of the line to delete.
     */
    public void deleteLineFromScenario(int line) {
        if (scenarioContent != null) {
            String[] lines = scenarioContent.split(System.lineSeparator());
            StringBuilder updatedContent = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i == line)
                    continue;
                else
                    updatedContent.append(lines[i]).append(System.lineSeparator());
            }

            scenarioContent = updatedContent.toString().trim();

        }
    }

    /**
     * Changes the given line's content with the provided content.
     * 
     * @param line    index of the line to change.
     * @param newLine new content of the line.
     */
    public void changeLineInScenario(int line, String newLine) {
        if (scenarioContent != null) {
            String[] lines = scenarioContent.split(System.lineSeparator());
            StringBuilder updatedContent = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i == line)
                    updatedContent.append(newLine).append(System.lineSeparator());
                else
                    updatedContent.append(lines[i]).append(System.lineSeparator());
            }

            scenarioContent = updatedContent.toString().trim();

        }
    }

    /**
     * Verifies the current scenario content and throws the appropriate exceptions.
     * 
     * @return true if the content is valid.
     * @throws InvalidScenarioContentException   with the correct exceptions
     *                                           attributes set.
     * @throws ImageNotFoundInDirectoryException if an image is in the scenario
     *                                           content but no corresponding file
     *                                           exists.
     */
    public boolean verifyScenarioContent() throws InvalidScenarioContentException, ImageNotFoundInDirectoryException {
        String[] lines = scenarioContent.split("\\r?\n");

        if (lines.length >= 1 && !lines[0].isEmpty()) {
            int i = 0;
            for (String line : lines) {
                String[] lineData = line.split(",");

                if (lineData.length != 2) {
                    throw new InvalidScenarioContentException("Error line " + i + ". : Wrong file format.",
                            InvalidScenarioContentException.INVALID_LINE_FORMAT, i, line);
                } else {
                    String fileName = lineData[0].trim();
                    double time;

                    try {
                        time = Double.parseDouble(lineData[1].trim());
                    } catch (NumberFormatException nfe) {
                        throw new InvalidScenarioContentException(
                                "Error line " + i + ". : Invalid specified time.",
                                InvalidScenarioContentException.INVALID_TIME, i, line);
                    }

                    if (fileName.isEmpty())
                        throw new InvalidScenarioContentException("Error line " + i + ". : Empty file name.",
                                InvalidScenarioContentException.EMPTY_FILENAME, i, line);

                    if (time > MAX_TIME || time < MIN_TIME)
                        throw new InvalidScenarioContentException(
                                "Error line " + i + ". : Invalid time. Must be < 1 && > .2",
                                InvalidScenarioContentException.INVALID_TIME, i, line);

                    boolean foundFile = false;
                    for (ImageFile imf : images) {
                        if (imf.getFileName().equals(lineData[0].trim())) {
                            foundFile = true;
                            break;
                        }
                    }
                    if (!foundFile) {
                        throw new ImageNotFoundInDirectoryException(
                                "Error line " + i + ". : File : " + lineData[0].trim() + " not found.",
                                lineData[0].trim());
                    }
                }
                i++;
            }
        }

        return true;
    }

    /**
     * Verifies if given line could fit in the scenario content.
     * 
     * @return true if line is correct, else false.
     */
    public boolean verifyLine(String line) {
        String[] lineData = line.split(",");

        if (lineData.length != 2) {
            return false;
        }

        if (lineData[0].isEmpty() || lineData[0].isBlank())
            return false;

        double time;
        try {
            time = Double.parseDouble(lineData[1].trim());
        } catch (Exception e) {
            return false;
        }

        if (time > MAX_TIME || time < MIN_TIME)
            return false;

        boolean foundFile = false;
        for (ImageFile imf : images) {
            if (imf.getFileName().equals(lineData[0].trim())) {
                foundFile = true;
                break;
            }
        }
        if (!foundFile)
            return false;

        return true;
    }

    /**
     * Iterates through the scenario content to return the corresponding images and
     * times.
     * 
     * @return An ordered List of Pairs of the images and their corresponding time
     *         in the
     *         scenario content.
     */
    public List<Pair<ImageFile, Double>> getOrderedImagesWithTime()
            throws InvalidScenarioContentException, ImageNotFoundInDirectoryException {
        List<Pair<ImageFile, Double>> result = new ArrayList<>();

        if (!verifyScenarioContent())
            return null;

        String[] lines = scenarioContent.split("\\r?\n");

        if (lines.length >= 1 && !lines[0].isEmpty()) {
            int i = 0;
            for (String line : lines) {
                String[] lineData = line.split(",");

                for (ImageFile imf : images) {
                    if (imf.getFileName().equals(lineData[0].trim())) {
                        result.add(new Pair<ImageFile, Double>(
                                imf,
                                Double.parseDouble(lineData[1].trim())));
                        break;
                    }
                }

            }
        }

        return result;
    }
}
