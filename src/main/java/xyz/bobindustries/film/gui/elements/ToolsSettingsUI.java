package xyz.bobindustries.film.gui.elements;

import xyz.bobindustries.film.image_editor.ToolsSettings;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.panes.EditorPane;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ToolsSettingsUI extends JPanel {

    private JSlider slider;
    private JLabel currentValue;

    public ToolsSettingsUI(JInternalFrame parent, int min, int max, int def) {
        setLayout(new BorderLayout());
        slider = new JSlider(min, max, def);
        slider.setPaintLabels(true);

        currentValue = new JLabel(""+def);

        // Ajoute un écouteur pour détecter les changements de valeur
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = slider.getValue();
                currentValue.setText(""+value);
                EditorPane editor = (EditorPane) Workspace.getInstance().getImageEditorFrame().getContentPane();
                ToolsSettings ts = editor.getSelectedToolsSettings();
                ts.updateCurrentThickness(value);
            }
        });

        add(slider, BorderLayout.CENTER);
        add(currentValue, BorderLayout.SOUTH);
    }

    public void setSlider(int min, int max, int def) {
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(def);
    }
}
