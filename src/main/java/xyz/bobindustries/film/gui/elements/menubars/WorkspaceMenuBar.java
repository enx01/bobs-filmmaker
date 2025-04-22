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

        JMenuItem newProjectItem = new JMenuItem("new");
        newProjectItem.addActionListener(ActionListenerProvider::getNewProjectDialogAction);

        JMenuItem openProjectItem = new JMenuItem("open");
        openProjectItem.addActionListener(ActionListenerProvider::getOpenProjectDialogAction);

        JMenuItem saveProjectItem = new JMenuItem("save");
        saveProjectItem.addActionListener(ActionListenerProvider::saveCurrentProject);

        projectMenu.add(newProjectItem);
        projectMenu.add(openProjectItem);
        projectMenu.add(saveProjectItem);

        // Window Menu :
        JMenu windowMenu = new JMenu("window");

        JMenuItem welcomeItem = new JMenuItem("welcome");
        JMenuItem imageEditorItem = new JMenuItem("image editor");
        JMenuItem scenarioEditorItem = new JMenuItem("scenario editor");
        JMenuItem filmVisualizerItem = new JMenuItem("film visualizer");
        JMenuItem aboutItem = new JMenuItem("about");

        welcomeItem.addActionListener(ActionListenerProvider::getShowWelcomeFrameAction);
        imageEditorItem.addActionListener(ActionListenerProvider::getShowImageEditorFrameAction);
        scenarioEditorItem.addActionListener(ActionListenerProvider::getShowScenarioEditorFrameAction);
        filmVisualizerItem.addActionListener(ActionListenerProvider::getShowFilmVisualizerFrameAction);
        aboutItem.addActionListener(ActionListenerProvider::getShowAboutFrameAction);

        windowMenu.add(welcomeItem);
        windowMenu.add(imageEditorItem);
        windowMenu.add(scenarioEditorItem);
        windowMenu.add(filmVisualizerItem);
        windowMenu.add(aboutItem);

        this.add(projectMenu);
        this.add(windowMenu);
    }

    public void displayEditorOptions() {
        displayDefaultOptions();
        JMenu windowMenu = new JMenu("editor");

        JMenuItem showToolsItem = new JMenuItem("Show tools");
        JMenuItem showColorsItem = new JMenuItem("Show colors");
        JMenuItem resizeItem = new JMenuItem("Resize image");
        JMenuItem canvasSizeItem = new JMenuItem("Resize drawing area");
        JMenuItem saveItem = new JMenuItem("Save image");

        windowMenu.add(showToolsItem);
        windowMenu.add(showColorsItem);
        windowMenu.add(resizeItem);
        windowMenu.add(canvasSizeItem);
        windowMenu.add(saveItem);

        showToolsItem.addActionListener(ActionListenerProvider::openEditorToolbox);
        add(windowMenu);
    }

}
