package xyz.bobindustries.film.gui.elements.dialogs;

import javax.swing.*;
import java.awt.*;

/*
    Simple Yes / No user prompting dialog.
 */
public class YesNoDialog extends JDialog {
    public static final int YES = 1;
    public static final int NO = 0;
    public static final int CANCEL = 2;

    private int choice = -1;

    public YesNoDialog(Frame owner, String query, boolean cancel) {
        super(owner, true);

        setSize(600, 150);
        setResizable(false);
        setMinimumSize(new Dimension(600, 150));
        setLocationRelativeTo(null); // Center the dialog on the screen
        setLayout(new BorderLayout());

        setUndecorated(true);

        JLabel queryLabel = new JLabel(query, JLabel.CENTER);
        queryLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(queryLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton yesButton = new JButton("yes");
        yesButton.addActionListener(e -> {
            choice = YES;
            setVisible(false);
        });

        JButton noButton = new JButton("no");
        noButton.addActionListener(e -> {
            choice = NO;
            setVisible(false);
        });

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        if (cancel) {
            JButton cancelButton = new JButton("cancel");
            cancelButton.addActionListener(e -> {
                choice = CANCEL;
                setVisible(false);
            });

            buttonPanel.add(cancelButton);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    public static int show(Frame parent, String query, boolean cancel) {
        YesNoDialog dialog = new YesNoDialog(parent, query, cancel);
        dialog.setVisible(true);
        return dialog.choice;
    }
}
