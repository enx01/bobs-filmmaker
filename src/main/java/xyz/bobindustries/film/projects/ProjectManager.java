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
            SimpleErrorDialog.showErrorDialog("Failed to create project directory");
        }

        Project res = new Project(projectDir, true);

        return res;
    }

    public Project getCurrent() {
        return current;
    }

    public static void setCurrent(Project newCurrent) {
        current = newCurrent;
    }

}
