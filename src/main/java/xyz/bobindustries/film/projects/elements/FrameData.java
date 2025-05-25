package xyz.bobindustries.film.projects.elements;

public class FrameData {
    ImageFile imageFile;
    double duration;

    public FrameData(ImageFile imageFile, double duration) {
        this.imageFile = imageFile;
        this.duration = duration;
    }

    public ImageFile getImageFile() {
        return imageFile;
    }

    public double getDuration() {
        return duration;
    }
}
