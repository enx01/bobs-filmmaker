package xyz.bobindustries.film.projects;

import xyz.bobindustries.film.projects.elements.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigProvider {
    private static final String CONFIG_FILE_NAME = ".config.properties";

    /**
     * Charge le fichier properties du dossier du projet.
     * 
     * @param project le projet dont on veut lire la config
     * @return les propriétés chargées, ou un objet vide si le fichier n'existe pas
     */
    public static Properties loadProperties(Project project) {
        Properties properties = new Properties();
        Path projectDir = project.getProjectDir();
        if (projectDir == null)
            return properties;
        File configFile = projectDir.resolve(CONFIG_FILE_NAME).toFile();
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture du fichier de configuration : " + e.getMessage());
            }
        }
        return properties;
    }

    /**
     * Sauvegarde les propriétés dans le fichier properties du dossier du projet.
     * 
     * @param project    le projet dont on veut sauvegarder la config
     * @param properties les propriétés à sauvegarder
     */
    public static void saveProperties(Project project, Properties properties) throws IOException {
        Path projectDir = project.getProjectDir();
        if (projectDir == null)
            return;

        System.out.println("file : " + projectDir + "/" + CONFIG_FILE_NAME);
        File configFile = new File(projectDir + "/" + CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            if (!configFile.createNewFile()) {
                return;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "Configuration du projet");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier de configuration : " + e.getMessage());
        }
    }

    /**
     * Définit la résolution (largeur et hauteur) dans les propriétés du projet.
     * 
     * @param properties les propriétés à modifier
     * @param width      la largeur de la résolution
     * @param height     la hauteur de la résolution
     */
    public static void setResolution(Properties properties, int width, int height) {
        properties.setProperty("resolution.width", String.valueOf(width));
        properties.setProperty("resolution.height", String.valueOf(height));
    }

    /**
     * Récupère la largeur de la résolution depuis les propriétés du projet.
     * 
     * @param properties les propriétés à lire
     * @return la largeur, ou -1 si non définie ou invalide
     */
    public static int getResolutionWidth(Properties properties) {
        String width = properties.getProperty("resolution.width");
        if (width == null)
            return -1;
        try {
            return Integer.parseInt(width);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Récupère la hauteur de la résolution depuis les propriétés du projet.
     * 
     * @param properties les propriétés à lire
     * @return la hauteur, ou -1 si non définie ou invalide
     */
    public static int getResolutionHeight(Properties properties) {
        String height = properties.getProperty("resolution.height");
        if (height == null)
            return -1;
        try {
            return Integer.parseInt(height);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
