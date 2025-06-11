package xyz.bobindustries.film.gui.elements.popups;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;

final class Value<T> {
    T v;

    Value(T v) {
        this.v = v;
    }

    public T getV() {
        return v;
    }
}

public class SimpleValueChangerPopUp extends JDialog {
    Value<String> value;
    JTextField txtField;

    public Value<String> getValue() {
        return value;
    }

    private SimpleValueChangerPopUp(int fieldLength, Value<String> initialValue, Frame owner, boolean center) {
        super(owner, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());
        value = initialValue;

        txtField = new JTextField(fieldLength);
        txtField.setText(initialValue.getV().toString());
        add(txtField);

        txtField.addActionListener(e -> {
            try {
                value = new Value<String>(txtField.getText());
            } catch (NumberFormatException ex) {
                value = initialValue;
            }
            dispose();
        });

        setUndecorated(true);

        if (center) {
            setLocationRelativeTo(null);
        } else {
            Point mousePos = MouseInfo.getPointerInfo().getLocation();

            int x = mousePos.x + 10;
            int y = mousePos.y - 50;

            setLocation(x, y);
        }
        pack();

        /* Bind ESC key to exit dialog. */
        getRootPane().registerKeyboardAction(e -> {
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Shows the double value changer pop up.
     * 
     * @return new value
     */
    public static double show(double initialValue, Frame owner) {
        SimpleValueChangerPopUp svcpu = new SimpleValueChangerPopUp(10, new Value<String>(String.valueOf(initialValue)),
                owner, false);

        svcpu.setVisible(true);
        return Double.parseDouble(((String) svcpu.getValue().getV()));
    }

    public static double show(double initialValue, Frame owner, boolean center) {
        SimpleValueChangerPopUp svcpu = new SimpleValueChangerPopUp(10, new Value<String>(String.valueOf(initialValue)),
                owner, center);

        svcpu.setVisible(true);
        return Double.parseDouble(((String) svcpu.getValue().getV()));
    }

    /**
     * Shows the String value changer pop up.
     * 
     * @return new value
     */
    public static String show(String initialValue, Frame owner) {
        SimpleValueChangerPopUp svcpu = new SimpleValueChangerPopUp(initialValue.length(),
                new Value<String>(initialValue),
                owner, false);

        svcpu.setVisible(true);
        return (String) svcpu.getValue().getV();
    }

    public static String show(String initialValue, Frame owner, boolean center) {
        SimpleValueChangerPopUp svcpu = new SimpleValueChangerPopUp(initialValue.length(),
                new Value<String>(initialValue),
                owner, center);

        svcpu.setVisible(true);
        return (String) svcpu.getValue().getV();
    }

}
