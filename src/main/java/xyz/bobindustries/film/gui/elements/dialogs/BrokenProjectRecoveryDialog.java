package xyz.bobindustries.film.gui.elements.dialogs;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.popups.SimpleValueChangerPopUp;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.ImageNotFoundInDirectoryException;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;

public class BrokenProjectRecoveryDialog extends JDialog {

    public BrokenProjectRecoveryDialog(Exception e) {
        super();
        setModal(true);
        setSize(600, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setTitle("broken project recovery");

        System.out.println("[+] BrokenProjectRecoveryDialog");

        if (e instanceof ImageNotFoundInDirectoryException) {
            showImageNotFoundInDirectoryPrompt(((ImageNotFoundInDirectoryException) e).imageName);
        }

        if (e instanceof InvalidScenarioContentException) {
            showInvalidScenarioContentPrompt((InvalidScenarioContentException) e);
        }
    }

    private void showImageNotFoundInDirectoryPrompt(String imageName) {
        setLayout(new BorderLayout());

        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 20, 20));

        JButton createEmptyImageButton = new JButton("create an empty image \"" + imageName + "\".");
        JButton deleteImageFromScenario = new JButton("delete all occurences of \"" + imageName + "\".");
        JButton abort = new JButton("abort");

        createEmptyImageButton.addActionListener(ev -> {
            ProjectManager.getCurrent().createNewEmptyFile(imageName);

            setVisible(false);
            dispose();
        });

        deleteImageFromScenario.addActionListener(ev -> {
            ProjectManager.getCurrent().deleteOccurrencesFromScenario(imageName);

            setVisible(false);
            dispose();
        });

        abort.addActionListener(ev -> {
            setVisible(false);
            dispose();
        });

        optionsPanel.add(createEmptyImageButton);
        optionsPanel.add(deleteImageFromScenario);
        optionsPanel.add(abort);
        add(new JLabel("image \"" + imageName + "\" not found in images directory."), BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);

    }

    private void showInvalidScenarioContentPrompt(InvalidScenarioContentException e) {
        setLayout(new BorderLayout());
        JLabel excMessage = new JLabel(e.getMessage());

        JButton deleteLine = new JButton("delete line from scenario");

        deleteLine.addActionListener(ev -> {
            ProjectManager.getCurrent().deleteLineFromScenario(e.line);
            setVisible(false);
            dispose();
        });

        JButton editLine = new JButton("edit line content");
        editLine.addActionListener(ev -> {

            boolean ok = false;
            String newLine = "";
            Project current = ProjectManager.getCurrent();

            while (!ok) {
                newLine = SimpleValueChangerPopUp.show(e.lineData,
                        App.getFrame(), true);

                if (current.verifyLine(newLine)) {
                    ok = true;
                }
            }

            current.changeLineInScenario(e.line, newLine);

            setVisible(false);
            dispose();
        });

        JButton editTime = new JButton("edit line time");
        editTime.addActionListener(ev -> {

            Project current = ProjectManager.getCurrent();
            boolean ok = false;
            double newTime = 0.0;
            String[] parts = e.lineData.split(",");
            double initialValue = Double.parseDouble(parts[1]);
            String newLine = "";

            while (!ok) {
                newTime = SimpleValueChangerPopUp.show(initialValue, App.getFrame(), true);

                newLine = parts[0] + "," + newTime;
                if (current.verifyLine(newLine))
                    ok = true;
            }

            current.changeLineInScenario(e.line, newLine);

            setVisible(false);
            dispose();
        });

        JPanel optionsPanel = new JPanel(new GridLayout(3, 0, 20, 20));

        switch (e.type) {
            case InvalidScenarioContentException.INVALID_LINE_FORMAT:
                optionsPanel.add(editLine);
                optionsPanel.add(deleteLine);
                optionsPanel.add(new JPanel());
                break;
            case InvalidScenarioContentException.EMPTY_FILENAME:
                optionsPanel.add(editLine);
                optionsPanel.add(deleteLine);
                optionsPanel.add(new JPanel());
                break;
            case InvalidScenarioContentException.INVALID_TIME:
                optionsPanel.add(editTime);
                optionsPanel.add(deleteLine);
                optionsPanel.add(new JPanel());
                break;

        }

        JButton abort = new JButton("abort");

        abort.addActionListener(ev -> {
            setVisible(false);
            dispose();
        });

        add(excMessage, BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);
        add(abort, BorderLayout.SOUTH);
    }

    public static void show(Exception e) {
        BrokenProjectRecoveryDialog bprd = new BrokenProjectRecoveryDialog(e);
        bprd.setVisible(true);
    }
}
