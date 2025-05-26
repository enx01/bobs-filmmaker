package xyz.bobindustries.film.gui.elements.menubars;

import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;

import javax.swing.*;

public class WorkspaceMenuBar extends JMenuBar {

    private boolean toolboxOpened = false;

    public WorkspaceMenuBar() {
        displayDefaultOptions();
    }

    public void displayDefaultOptions() {
        removeAll();
        // Project Menu :
        JMenu projectMenu = new JMenu("project");
        projectMenu.setMnemonic('P');

        JMenuItem newProjectItem = new JMenuItem("new");
        newProjectItem.setMnemonic('N');
        newProjectItem.addActionListener(ActionListenerProvider::getNewProjectDialogAction);

        JMenuItem openProjectItem = new JMenuItem("open");
        openProjectItem.setMnemonic('O');
        openProjectItem.addActionListener(ActionListenerProvider::getOpenProjectDialogAction);

        JMenuItem exportProjectItem = new JMenuItem("export");
        exportProjectItem.setMnemonic('E');
        exportProjectItem.addActionListener(ActionListenerProvider::exportProjectAsVideo);

        JMenuItem saveProjectItem = new JMenuItem("save");
        saveProjectItem.setMnemonic('S');
        saveProjectItem.addActionListener(ActionListenerProvider::saveCurrentProject);

        JMenuItem closeProjectItem = new JMenuItem("close");
        closeProjectItem.setMnemonic('C');
        closeProjectItem.addActionListener(ActionListenerProvider::closeCurrentProject);

        projectMenu.add(newProjectItem);
        projectMenu.add(openProjectItem);
        projectMenu.add(new JSeparator());
        projectMenu.add(exportProjectItem);
        projectMenu.add(saveProjectItem);
        projectMenu.add(new JSeparator());
        projectMenu.add(closeProjectItem);

        // Window Menu :
        JMenu windowMenu = new JMenu("window");
        windowMenu.setMnemonic('W');

        JMenuItem welcomeItem = new JMenuItem("welcome");
        welcomeItem.setMnemonic('W');
        welcomeItem.addActionListener(ActionListenerProvider::getShowWelcomeFrameAction);

        JMenuItem imageEditorItem = new JMenuItem("image editor");
        imageEditorItem.setMnemonic('I');
        imageEditorItem.addActionListener(ActionListenerProvider::getShowImageEditorFrameAction);

        JMenuItem scenarioEditorItem = new JMenuItem("scenario editor");
        scenarioEditorItem.setMnemonic('S');
        scenarioEditorItem.addActionListener(ActionListenerProvider::getShowScenarioEditorFrameAction);

        JMenuItem filmVisualizerItem = new JMenuItem("film visualizer");
        filmVisualizerItem.setMnemonic('V');
        filmVisualizerItem.addActionListener(ActionListenerProvider::getShowFilmVisualizerFrameAction);

        JMenuItem aboutItem = new JMenuItem("about");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(ActionListenerProvider::getShowAboutFrameAction);

        windowMenu.add(welcomeItem);
        windowMenu.add(imageEditorItem);
        windowMenu.add(scenarioEditorItem);
        windowMenu.add(filmVisualizerItem);
        windowMenu.add(aboutItem);

        JMenu helpMenu = new JMenu("help");
        helpMenu.setMnemonic('H');

        JMenuItem bobFilmmakerTutorial = new JMenuItem("bob filmmaker tutorial");
        bobFilmmakerTutorial.setMnemonic('B');
        bobFilmmakerTutorial.addActionListener(ActionListenerProvider::getShowBobTutorialPopup);

        JMenuItem imageEditorTutorial = new JMenuItem("image editor tutorial");
        imageEditorTutorial.setMnemonic('I');
        imageEditorTutorial.addActionListener(ActionListenerProvider::getShowImageEditorTutorialPopup);

        JMenuItem scenarioEditorTutorial = new JMenuItem("scenario editor tutorial");
        scenarioEditorTutorial.setMnemonic('S');
        scenarioEditorTutorial.addActionListener(ActionListenerProvider::getShowScenarioEditorTutorialPopup);

        JMenuItem filmVisualizerTutorial = new JMenuItem("film visualizer tutorial");
        filmVisualizerTutorial.setMnemonic('V');
        filmVisualizerTutorial.addActionListener(ActionListenerProvider::getShowVisualizerTutorialPopup);

        helpMenu.add(bobFilmmakerTutorial);
        helpMenu.add(imageEditorTutorial);
        helpMenu.add(scenarioEditorTutorial);
        helpMenu.add(filmVisualizerTutorial);

        this.add(projectMenu);
        this.add(windowMenu);
        this.add(helpMenu);
    }

    public void displayEditorOptions() {
        displayDefaultOptions();
        JMenu windowMenu = new JMenu("editor");

        JMenuItem showToolsItem = new JMenuItem("Show tools");
        JMenuItem showColorsItem = new JMenuItem("Show colors");
        JMenuItem showToolsSettings = new JMenuItem("Show tools settings");
        JMenuItem saveItem = new JMenuItem("Save image");

        windowMenu.add(showToolsItem);
        windowMenu.add(showColorsItem);
        windowMenu.add(showToolsSettings);
        windowMenu.add(saveItem);

        showToolsItem.addActionListener(ActionListenerProvider::openEditorToolbox);
        showColorsItem.addActionListener(ActionListenerProvider::openEditorColorBox);
        showToolsSettings.addActionListener(ActionListenerProvider::openEditorToolsSettings);
        saveItem.addActionListener(ActionListenerProvider::saveImageAction);
        add(windowMenu);
    }

}
