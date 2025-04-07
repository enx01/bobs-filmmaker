package xyz.bobindustries.film.projects;

import java.io.File;
import java.io.IOException;

import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
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

    public static Project openProject(String projectLocation) throws IOException, InvalidProjectDirectoryException {
        File projectDir = new File(projectLocation);

        if (!projectDir.exists() || !verifyLocationContent(projectDir)) {
            System.out.println("splash");
            throw new InvalidProjectDirectoryException();
        }

        String projectName = projectDir.getName();

        return new Project(projectName, projectDir, false);
    }

    private static boolean verifyLocationContent(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        File[] contents = file.listFiles();

        if (contents == null) {
            return false;
        }

        boolean hasImagesDir = false;
        boolean hasTxtFile = false;

        // Iterate through the contents of the directory
        for (File content : contents) {
            if (content.isDirectory() && content.getName().equals("images")) {
                hasImagesDir = true;
            } else if (content.isFile() && content.getName().equals("scenario.txt")) {
                hasTxtFile = true;
            }
        }

        // Check if there is exactly one directory "images" and one .txt file
        return hasImagesDir && hasTxtFile && contents.length == 2;
    }

    public static Project getCurrent() {
        return current;
    }

    public static void setCurrent(Project newCurrent) {
        current = newCurrent;
    }

}
