package xyz.bobindustries.film.gui.elements.popups;

import xyz.bobindustries.film.gui.elements.utilitaries.Bob;

import javax.swing.*;
import java.awt.*;

public class HelperBobPopUp extends JDialog {

    public HelperBobPopUp(Component caller, Frame owner) {
        super(owner, "Helper Bob", true);
        setSize(800, 600);
        setResizable(false);

        if (caller != null)
            setLocationRelativeTo(caller);

        setLayout(new BorderLayout());


        Bob bob = new Bob(true, .75);
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.setOpaque(false);
        paddedPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 20));
        paddedPanel.add(bob, BorderLayout.CENTER);

        add(paddedPanel, BorderLayout.PAGE_END);

        JPanel tutorialHolder = new JPanel() {
            @Override
            public void paintComponents(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Set color to #fbf1c7
                g2d.setColor(Color.decode("#fbf1c7"));

                // Calculate dimensions for the centered oval
                int width = 600; // Width of the oval
                int height = 400; // Height of the oval
                @SuppressWarnings("unused")
                int x = (800 - width) / 2; // Center x-coordinate
                @SuppressWarnings("unused")
                int y = (600 - height) / 2; // Center y-coordinate

                // Draw the filled oval
                g2d.fillOval(0, 0, width, height);
            }
        };

        tutorialHolder.setPreferredSize(new Dimension(600, 400));
        tutorialHolder.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        add(tutorialHolder, BorderLayout.CENTER);
    }



    public static void show(Frame owner, Component caller) {
        HelperBobPopUp hbpu = new HelperBobPopUp(caller, owner);
        hbpu.setVisible(true);
    }
}
