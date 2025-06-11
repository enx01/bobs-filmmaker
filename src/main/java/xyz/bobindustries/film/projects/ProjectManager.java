package xyz.bobindustries.film.projects;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidProjectDirectoryException;

public class ProjectManager {
    private static Project current = null;

    public ProjectManager() {
    }

    public static Project createProject(String projectName, String projectLocation)
            throws IOException, InvalidProjectDirectoryException {
        File projectDir = new File(projectLocation, projectName);

        if (!projectDir.mkdir()) {
            throw new InvalidProjectDirectoryException();
        }

        return new Project(projectName, projectDir, true);
    }

    public static Project openProject(String projectLocation)
            throws IOException, InvalidProjectDirectoryException {
        File projectDir = new File(projectLocation);

        if (!projectDir.exists()) {
            throw new IOException();
        }

        verifyLocationContent(projectDir);

        String projectName = projectDir.getName();

        return new Project(projectName, projectDir, false);
    }

    private static boolean verifyLocationContent(File file)
            throws InvalidProjectDirectoryException {
        if (!file.isDirectory()) {
            return false;
        }

        File[] contents = file.listFiles();

        if (contents == null) {
            return false;
        }

        boolean hasImagesDir = false;
        boolean hasTxtFile = false;
        boolean hasOutputDir = false;
        boolean hasConfigFile = false;

        // Iterate through the contents of the directory
        for (File content : contents) {
            if (content.isDirectory()) {
                if (content.getName().equals("images"))
                    hasImagesDir = true;
                else if (content.getName().equals("output"))
                    hasOutputDir = true;
            } else if (content.isFile() && content.getName().equals("scenario.txt")) {
                hasTxtFile = true;
            } else if (content.isFile() && content.getName().equals(".config.properties")) {
                hasConfigFile = true;
            }
        }

        if (!hasImagesDir) {
            throw new InvalidProjectDirectoryException("No images directory found in project directory.");
        }
        if (!hasTxtFile) {
            throw new InvalidProjectDirectoryException("No scenario.txt file found in project directory.");
        }
        if (!hasConfigFile) {
            throw new InvalidProjectDirectoryException("No .config.properties file found in project directory.");
        }

        if (contents.length == 3) {
            return hasImagesDir && hasTxtFile && hasConfigFile;
        } else if (contents.length == 4) {
            return hasImagesDir && hasTxtFile && hasOutputDir && hasConfigFile;
        }

        return false;
    }

    public static Project getCurrent() {
        return current;
    }

    public static Color[][] getLastImage() {
        return getCurrent().getImages().getLast().getColorMatrix();
    }

    public static Color[][] getNImage(int n) {
        return getCurrent().getImages().get(n).getColorMatrix();
    }

    public static String getNImageName(int n) {
        return getCurrent().getImages().get(n).getFileName();
    }

    public static Color[][] getImageMatrix(String name) {
        for (ImageFile file : getCurrent().getImages()) {
            if (file.getFileName().equals(name)) {
                return file.getColorMatrix();
            }
        }
        return null;
    }

    public static void saveCurrent() throws IOException {
        if (current == null) {
            return;
        }

        current.save();
    }

    public static void setCurrent(Project newCurrent) {
        current = newCurrent;
    }

}
