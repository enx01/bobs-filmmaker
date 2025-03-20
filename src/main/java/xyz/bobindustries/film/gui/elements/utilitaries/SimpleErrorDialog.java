package xyz.bobindustries.film.gui.elements.utilitaries;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SimpleErrorDialog extends JDialog {

    public static void showErrorDialog(String message) {
        // Create a JDialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Error");
        dialog.setModal(true); // Make the dialog modal
        dialog.setSize(300, 150); // Set the size of the dialog
        dialog.setLocationRelativeTo(null); // Center the dialog on the screen

        // Create a panel to hold the message and button
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Create a label to display the error message
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        // Create an OK button to close the dialog
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose()); // Close the dialog when clicked
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the panel to the dialog
        dialog.add(panel);
        dialog.setVisible(true); // Show the dialog
    }
}
