package xyz.bobindustries.film.gui.elements;

import java.awt.*;
import javax.swing.*;

public class CoordinateBar extends JToolBar {
    private JLabel coordinates;
    private JLabel frameSize;
    private JLabel fileName;
    private Separator separator;

    public CoordinateBar() {
        setLayout(new BorderLayout());
        setFloatable(false);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        //setBackground(new Color(245, 245, 245));

        // Conteneur gauche
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.X_AXIS));
        leftContainer.setOpaque(false);
        leftContainer.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        coordinates = new JLabel("  0 x 0  ");

        fileName = new JLabel("  Aucun fichier  ");

        separator = new Separator();
        separator.setPreferredSize(new Dimension(10, 20));

        frameSize = new JLabel("  0 x 0  ");

        leftContainer.add(coordinates);
        leftContainer.add(Box.createHorizontalStrut(10));
        leftContainer.add(fileName);
        leftContainer.add(Box.createHorizontalStrut(10));
        leftContainer.add(separator);
        leftContainer.add(Box.createHorizontalStrut(10));
        leftContainer.add(frameSize);

        this.add(leftContainer, BorderLayout.WEST);
    }

    public JLabel getCoordinates()
    {
        return coordinates;
    }

    public JLabel getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.setText(fileName);
    }

    public JLabel getFrameSize()
    {
        return frameSize;
    }

    public JToolBar getCoordinateBar()
    {
        return this;
    }
}
