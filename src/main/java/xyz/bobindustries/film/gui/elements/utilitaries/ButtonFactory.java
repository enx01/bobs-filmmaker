package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.gui.panes.WelcomePane;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ButtonFactory {

    public static JButton createButton(String text, String imageName, int width, int height) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());

        try (InputStream is = ButtonFactory.class.getResourceAsStream(imageName)) {
            if (is == null) {
                SimpleErrorDialog.showErrorDialog("InputStream returned null! :(");
            } else {
                BufferedImage img = ImageIO.read(is);
                ImageIcon icon = new ImageIcon(img);

                Image resized = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

                button.setIcon(new ImageIcon(resized));
            }
        } catch (IOException e) {
            SimpleErrorDialog.showErrorDialog("Image Not Read :(");
        }

        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setVerticalAlignment(JLabel.BOTTOM);
        label.setHorizontalAlignment(JLabel.CENTER);

        button.add(label, BorderLayout.SOUTH);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        button.setPreferredSize(new Dimension(width, height));
        button.setMaximumSize(new Dimension(width, height));

        return button;
    }
}
