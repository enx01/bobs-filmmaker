package xyz.bobindustries.film.image_editor;

public interface ToolsSettings {
    int[] getSliderBounds();
    int getCurrentThickness();
    void updateCurrentThickness(int thickness);
}
