package xyz.bobindustries.film;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import xyz.bobindustries.film.gui.elements.utilitaries.LoadingWindow;
import xyz.bobindustries.film.gui.panes.WelcomePane;

import java.awt.*;

/**
 * Hello world!
 */
public class App {
    private static JFrame frame;

    public static void main(String[] args) {
        System.out.println("[+] started filmmaker app.");
        SwingUtilities.invokeLater(App::run);
    }

    private static void run() {
        LoadingWindow loadingWindow = new LoadingWindow();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() { // Classe anonyme d'initialisation de la frame.
            @Override
            protected Void doInBackground() throws Exception {
                /** Creation de la fenetre */
                frame = new JFrame("bob's filmmaker");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Recuperation de la taille de
                                                                                    // l'ecran de
                                                                                    // l'utilisateur.
                frame.setSize(screenSize.width, screenSize.height);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                frame.setMinimumSize(new Dimension(600, 400));

                WelcomePane welcomePane = new WelcomePane();

                frame.add(welcomePane);

                frame.setVisible(true);
                return null;
            }

            protected void done() {
                loadingWindow.dispose();
            }
        };

        worker.execute();
    }

}
