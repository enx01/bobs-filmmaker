package xyz.bobindustries.film.ImageEditor;

public interface ToolsSettings {
    int[] getSliderBounds();
    int getCurrentThickness();
    void updateCurrentThickness(int thickness);
}
