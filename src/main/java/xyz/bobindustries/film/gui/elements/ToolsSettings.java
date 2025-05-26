package xyz.bobindustries.film.gui.elements;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ToolsSettings extends JPanel {

    private JSlider slider;
    private JLabel currentValue;

    public ToolsSettings(JInternalFrame parent, int min, int max, int def) {
        setLayout(new BorderLayout());
        slider = new JSlider(min, max, def);
        slider.setPaintLabels(true);

        currentValue = new JLabel(""+def);

        // Ajoute un écouteur pour détecter les changements de valeur
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = slider.getValue();
                System.out.println(value);
                currentValue.setText(""+value);
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
