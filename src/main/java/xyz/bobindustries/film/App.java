package xyz.bobindustries.film;

import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.utilitaries.LoadingWindow;
import xyz.bobindustries.film.gui.panes.WelcomePane;

import java.awt.*;
import java.io.InputStream;

/**
 * Bob's filmmaker Application main class.
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
            System.out.println("[+] failed to load synth look and feel : " + e.getMessage());
        }
    }

    private static void run() {
        LoadingWindow loadingWindow = new LoadingWindow("bob's filmmaker", 200, 200);
        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

        SwingWorker<Void, Void> worker = new SwingWorker<>() { // Classe anonyme d'initialisation de la frame.
            @Override
            protected Void doInBackground() {
                /* Creation de la fenetre */
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

    public static JFrame getFrame() {
        return frame;
    }

}
