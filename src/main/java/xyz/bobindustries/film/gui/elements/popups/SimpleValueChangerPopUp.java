package xyz.bobindustries.film.gui.elements.popups;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;

public class SimpleValueChangerPopUp extends JDialog {
    double value;

    public double getValue() {
        return value;
    }

    private SimpleValueChangerPopUp(double initialValue, Frame owner) {
        super(owner, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        JTextField txtField = new JTextField(10);
        add(txtField);

        setUndecorated(true);

        txtField.addActionListener(e -> {
            try {
                value = Double.parseDouble(txtField.getText());
            } catch (NumberFormatException ex) {
                value = initialValue;
            }
            dispose();
        });

        Point mousePos = MouseInfo.getPointerInfo().getLocation();

        int x = mousePos.x + 10;
        int y = mousePos.y - 50;

        setLocation(x, y);
        pack();

        /* Bind ESC key to exit dialog. */
        getRootPane().registerKeyboardAction(e -> {
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Shows the value changer pop up.
     * 
     * @return new value
     */
    public static double show(double initialValue, Frame owner) {
        SimpleValueChangerPopUp svcpu = new SimpleValueChangerPopUp(initialValue, owner);
        svcpu.setVisible(true);
        return svcpu.getValue();
    }

}
