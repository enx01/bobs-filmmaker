package xyz.bobindustries.film.gui.elements;

import xyz.bobindustries.film.ImageEditor.ToolsList;
import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToolboxButton extends JButton {

    public ToolboxButton(ToolsList tool) {
        setActionCommand(tool.name());
        setPreferredSize(new Dimension(40, 40));
        setToolTipText("Outil " + tool);

        // Icon icon = UIManager.getIcon("OptionPane.informationIcon");
        // setIcon(icon);

        addActionListener(ActionListenerProvider::setTool);
    }
}