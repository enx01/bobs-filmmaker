package xyz.bobindustries.film.gui.elements.utilitaries;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SimpleHappyDialog {

    public static void show(String message) {
        JDialog dialog = new JDialog();
        dialog.setTitle("bob is happy");
        dialog.setModal(true);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        JLabel label = new JLabel(message);
        panel.add(label, BorderLayout.CENTER);

        Bob bob = new Bob(.5);
        bob.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ignoredMouseEvent) {
                dialog.dispose();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bob);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);

        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ESCAPE || evt.getKeyCode() == KeyEvent.VK_ENTER)
                    dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}