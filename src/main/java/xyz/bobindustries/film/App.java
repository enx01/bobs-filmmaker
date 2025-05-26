package xyz.bobindustries.film;

import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import xyz.bobindustries.film.gui.elements.dialogs.YesNoDialog;
import xyz.bobindustries.film.gui.elements.popups.HelperBobPopUp;
import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.ConstantsProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.LoadingWindow;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.panes.WelcomePane;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.Project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import javax.swing.KeyStroke;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.AbstractAction;

/**
 * Bob's filmmaker Application main class.
 */
public class App {
    public static JFrame frame;

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
            System.out.println("[-] failed to load synth look and feel : " + e.getMessage());
        }
    }

    private static void run() {
        LoadingWindow loadingWindow = new LoadingWindow("", 200, 200);
        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

        SwingWorker<Void, Void> worker = new SwingWorker<>() { // Classe anonyme d'initialisation de la frame.
            @Override
            protected Void doInBackground() throws Exception {
                /* Creation de la fenetre */
                frame = new JFrame("bob's filmmaker");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                frame.setIconImage(Toolkit.getDefaultToolkit().getImage(App.class.getResource("bob_filmmaker.png")));

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Recuperation de la taille de
                                                                                    // l'ecran de
                                                                                    // l'utilisateur.

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        Project curProject = ProjectManager.getCurrent();
                        if (curProject != null) {
                            int userResponse = YesNoDialog.show(frame,
                                    "would you like to save project \"" +
                                            curProject.getProjectName() +
                                            "\" before exiting ?");
                            if (userResponse == YesNoDialog.YES) {
                                try {
                                    curProject.save();
                                } catch (Exception ex) {
                                    SimpleErrorDialog.show("failed to save project :(" + "\n" + ex.getMessage());
                                }
                            }
                        }
                    }
                });

                // Add InputMap and ActionMap handling for CTRL+S
                InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                ActionMap actionMap = frame.getRootPane().getActionMap();

                inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "saveProject");
                actionMap.put("saveProject", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Project curProject = ProjectManager.getCurrent();
                        if (curProject != null) {
                            try {
                                ActionListenerProvider.saveCurrentProjectWithoutSuccessFeedback(null);
                            } catch (Exception ex) {
                                SimpleErrorDialog.show("failed to save project :( " + "\n" + ex.getMessage());
                            }
                        }
                    }
                });

                Thread.sleep(3000);

                frame.setSize(screenSize.width, screenSize.height);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                frame.setMinimumSize(ConstantsProvider.WINDOW_MIN_SIZE);

                WelcomePane welcomePane = new WelcomePane();

                frame.add(welcomePane);
                frame.setVisible(true);

                frame.requestFocus();

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