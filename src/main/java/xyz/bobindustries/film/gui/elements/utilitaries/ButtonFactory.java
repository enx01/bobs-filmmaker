package xyz.bobindustries.film.gui.elements.utilitaries;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ButtonFactory {

    public static JButton createButton(String text, String imageName, int width, int height) {
        JButton button = new JButton() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Rectangle2D rect = g2d.getFontMetrics().getStringBounds(text, g2d);

                float fs = g2d.getFont().getSize();
                double rectWidth = rect.getWidth();

                while (rectWidth >= width && fs > 2) {
                    rectWidth = g2d.getFont().deriveFont((float) (fs--))
                            .getStringBounds(text, g2d.getFontRenderContext())
                            .getWidth();
                }

                // if (rect.getWidth() >= width)
                g2d.setFont(g2d.getFont().deriveFont((float) fs));

                g2d.setColor(new Color(235, 219, 178));
                g2d.drawString(text, (int) ((width / 2) - (rectWidth / 2)), height - 10);
            }
        };
        button.setLayout(new BorderLayout());

        try (InputStream is = ButtonFactory.class.getResourceAsStream(imageName)) {
            if (is == null) {
                SimpleErrorDialog.show("InputStream returned null! :(");
            } else {
                BufferedImage img = ImageIO.read(is);
                ImageIcon icon = new ImageIcon(img);

                Image resized = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

                button.setIcon(new ImageIcon(resized));
            }
        } catch (IOException e) {
            SimpleErrorDialog.show("Image Not Read :(");
        }

        // JLabel label = new JLabel(text, JLabel.CENTER);
        // label.setVerticalAlignment(JLabel.BOTTOM);
        // label.setHorizontalAlignment(JLabel.CENTER);

        // label.setToolTipText(text);
        button.setToolTipText(text);

        // button.add(label, BorderLayout.SOUTH);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        button.setPreferredSize(new Dimension(width, height));
        button.setMaximumSize(new Dimension(width, height));

        return button;
    }
}
