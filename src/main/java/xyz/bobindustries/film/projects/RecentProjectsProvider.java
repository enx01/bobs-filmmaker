package xyz.bobindustries.film.projects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class RecentProjectsProvider {

    public static void writeConfigFile(String projToAdd) {
        String userHome = System.getProperty("user.home");
        File configDir = new File(userHome, ".config/bobsfilmmaker");

        if (!configDir.exists()) {
            configDir.mkdir();
        }

        File configFile = new File(configDir, "recentProjects.properties");

        Properties config = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
            String existingPathsString = config.getProperty("directories", "");
            ArrayList<String> existingPaths = new ArrayList<>();
            if (!existingPathsString.isEmpty()) {
                String[] existingPathsArray = existingPathsString.split(",");
                for (String path : existingPathsArray) {
                    existingPaths.add(path);
                }
            }

            boolean alreayExists = false;

            // replacement du path courant en haut de la liste
            for (int i = 0; i < existingPaths.size(); i++) {
                if (existingPaths.get(i).equals(projToAdd)) {
                    existingPaths.remove(i);
                    existingPaths.add(0, projToAdd);
                    alreayExists = true;
                    break;
                }
            }

            if (!alreayExists) {
                if (existingPaths.size() == 10) {
                    existingPaths.remove(9);
                }
                existingPaths.add(0, projToAdd);
            }

            String newPathsString = String.join(",", existingPaths);

            config.setProperty("directories", newPathsString);

        } catch (IOException e) {
            System.out.println("Fichier de configuration non trouvé ou erreur lors de la lecture : " + e.getMessage());
            ArrayList<String> paths = new ArrayList<>();
            paths.add(projToAdd);

            String pathsString = String.join(",", paths);
            config.setProperty("directories", pathsString);
        }

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            config.store(fos, "");
            System.out.println("Configuration file created successfully at: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error creating configuration file: " + e.getMessage());
        }
    }

    public static ArrayList<String> getDirectoriesFromConfig() {
        String userHome = System.getProperty("user.home");
        File configDir = new File(userHome, ".config/bobsfilmmaker");
        File configFile = new File(configDir, "recentProjects.properties");

        Properties config = new Properties();
        ArrayList<String> directories = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);

            String pathsString = config.getProperty("directories", "");
            if (!pathsString.isEmpty()) {
                String[] pathsArray = pathsString.split(",");
                for (String path : pathsArray) {
                    directories.add(path);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return new ArrayList<>();
        }

        return directories;
    }
}
