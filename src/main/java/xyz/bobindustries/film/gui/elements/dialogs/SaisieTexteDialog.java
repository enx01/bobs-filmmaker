package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SaisieTexteDialog extends JDialog {
    private JTextField champTexte;
    private JButton boutonValider;
    private String texteSaisi = null;
    private boolean isSuccess = false;

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    public SaisieTexteDialog(Frame parent) {
        super(parent, "Choisir un nom", true); // true = modal
        setSize(300, 150);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        champTexte = new JTextField(20);
        boutonValider = new JButton("Valider");

        setLayout(new FlowLayout());
        add(new JLabel("Entrez un nom :"));
        add(champTexte);
        add(boutonValider);

        boutonValider.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                texteSaisi = champTexte.getText();
                isSuccess = true;
                dispose();
            }
        });
    }

    public String getTexteSaisi() {
        return texteSaisi;
    }

    public static String show(Frame parent) {
        SaisieTexteDialog dialog = new SaisieTexteDialog(parent);
        dialog.setVisible(true); // modal
        if (dialog.isSuccess) {
            return dialog.getTexteSaisi();
        } else {
            return null;
        }
    }
} 