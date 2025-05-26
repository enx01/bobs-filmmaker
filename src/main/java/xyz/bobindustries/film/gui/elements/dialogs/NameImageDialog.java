package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class NameImageDialog extends JDialog {
    private JTextField champTexte;
    private JButton boutonValider;
    private String texteSaisi = null;
    private boolean isSuccess = false;

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    public NameImageDialog(Frame parent) {
        super(parent, "Saisie de texte", true); // true = modal
        setSize(500, 100);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        champTexte = new JTextField(20);
        boutonValider = new JButton("Valider");

        setLayout(new FlowLayout());
        add(new JLabel("Entrez un texte :"));
        add(champTexte);
        add(boutonValider);

        boutonValider.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                texteSaisi = champTexte.getText();
                if (texteSaisi.trim().isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yy_HH:mm");
                    LocalDateTime now = LocalDateTime.now();
                    String timestamp = now.format(formatter);
                    texteSaisi = "image" + timestamp;
                }
                isSuccess = true;
                dispose();
            }
        });
    }

    public String getTexteSaisi() {
        return texteSaisi;
    }

    public static String show(Frame parent, String nameResult) {
        NameImageDialog dialog = new NameImageDialog(parent);
        dialog.setVisible(true); // modal
        return dialog.texteSaisi;
    }
}
