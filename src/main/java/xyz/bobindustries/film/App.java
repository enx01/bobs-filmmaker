package xyz.bobindustries.film;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthLookAndFeel;

import xyz.bobindustries.film.gui.elements.utilitaries.LoadingWindow;
import xyz.bobindustries.film.gui.panes.WelcomePane;

import java.awt.*;
import java.io.InputStream;

/**
 * Hello world!
 */
public class App {
    private static JFrame frame;

    public static void main(String[] args) {
        System.out.println("[+] started filmmaker app.");
        setCustomLAF();
        SwingUtilities.invokeLater(App::run);
    }

    private static void setCustomLAF() {
        try {
            SynthLookAndFeel synthLookAndFeel = new SynthLookAndFeel();
            InputStream is = App.class.getResourceAsStream("gruvbox-light.xml");
            synthLookAndFeel.load(is, App.class);

            UIManager.setLookAndFeel(synthLookAndFeel);

        } catch (Exception e) {
            e.printStackTrace();
        }
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

                frame.setMinimumSize(new Dimension(800, 600));

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
