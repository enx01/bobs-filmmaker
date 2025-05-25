package xyz.bobindustries.film.gui.elements.utilitaries;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SimpleErrorDialog {

    public static void show(String message) {
        // Create a JDialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Error");
        dialog.setModal(true); // Make the dialog modal
        dialog.setResizable(false);

        // Create a panel to hold the message and button
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Create a label to display the error message
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        // Create an OK button to close the dialog
        JButton okButton = new JButton("ok");
        okButton.addActionListener(e -> dialog.dispose()); // Close the dialog when clicked
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the panel to the dialog
        dialog.add(panel);

        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ESCAPE || evt.getKeyCode() == KeyEvent.VK_ENTER)
                    dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(null); // Center the dialog on the screen
        dialog.setVisible(true); // Show the dialog
    }
}
