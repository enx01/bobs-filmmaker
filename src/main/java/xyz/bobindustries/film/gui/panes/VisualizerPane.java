package xyz.bobindustries.film.gui.panes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.*;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.utilitaries.ConstantsProvider;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.projects.elements.ImageFile;

/**
 * VisualizerPane class to visualize current project film state.
 */
public class VisualizerPane extends JPanel {
    /* Config */
    private static final int REFRESH_RATE = 50;

    /* State */
    private List<Pair<ImageFile, Double>> currentState;
    private double totalDuration;

    /* UI */
    private final JLabel imageLabel;
    private final JSlider timeSlider;
    private final JButton ppButton;
    private final JButton rewindButton;
    private final JButton forwardButton;
    private final JButton replayButton;
    private final JButton toEndButton;
    private final JLabel timeLabel;

    // private final Timer playbackTimer;

    private volatile double totalElapsedTime = 0.0;
    private volatile int currentImageIndex = -1;
    private volatile double currentFrameElapsedTime = 0.0;
    private volatile boolean isPlaying = false;
    private volatile boolean isSeeking = false;
    private volatile boolean running = false;

    private Thread visualizerThread;
    private final VisualizerRunnable visualizerRunnable;

    // private static final int TIMER_DELAY = 100;
    // private static final double TIMER_DELAY_SEC = TIMER_DELAY / 1000.0;

    private ScenarioEditorPane editorPane;

    public VisualizerPane(ScenarioEditorPane editorPane) {
        this.editorPane = editorPane;
        updateState();
        setSize(ConstantsProvider.IFRAME_MIN_SIZE);
        setPreferredSize(ConstantsProvider.IFRAME_MIN_SIZE);

        this.visualizerRunnable = new VisualizerRunnable();

        totalDuration = computeTotalDuration();

        setLayout(new BorderLayout(5, 5));

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        imageLabel.putClientProperty("JComponent.setBackground.respectUI", Boolean.FALSE);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setOpaque(true);
        imageLabel.repaint();

        imageLabel.setPreferredSize(
                new Dimension((int) (this.getPreferredSize().width * .3), (int) (this.getPreferredSize().height * .7)));
        imageLabel.setBorder(BorderFactory.createEtchedBorder());
        add(imageLabel, BorderLayout.CENTER);

        /* Controls */

        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        int sliderMax = (int) Math.round(totalDuration * 10);
        timeSlider = new JSlider(0, sliderMax, 0);
        timeSlider.setFocusable(false);
        timeSlider.setPaintTicks(true);
        timeSlider.setPreferredSize(new Dimension(this.getPreferredSize().width, 30));
        timeSlider.setMajorTickSpacing(sliderMax / 5 > 0 ? sliderMax / 5 : 1);
        timeSlider.setMinorTickSpacing(sliderMax / 10 > 0 ? sliderMax / 10 : 1);

        sliderPanel.add(timeSlider);

        JPanel timeHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        timeLabel = new JLabel(formatTime(0.0) + " / " + formatTime(totalDuration));
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 10f));
        timeHolder.add(timeLabel);

        controlPanel.add(timeHolder, BorderLayout.NORTH);

        controlPanel.add(sliderPanel, BorderLayout.CENTER);

        /* Buttons */
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        replayButton = new JButton("⏮︎");
        rewindButton = new JButton("⏴︎");
        ppButton = new JButton("⏯︎");
        forwardButton = new JButton("⏵︎");
        toEndButton = new JButton("⏭︎");

        buttonsPanel.add(replayButton);
        buttonsPanel.add(rewindButton);
        buttonsPanel.add(ppButton);
        buttonsPanel.add(forwardButton);
        buttonsPanel.add(toEndButton);

        controlPanel.add(buttonsPanel, BorderLayout.SOUTH);
        // controlPanel.setPreferredSize(new Dimension(this.getPreferredSize().width,
        // (int) (this.getPreferredSize().height * .4)));

        add(controlPanel, BorderLayout.SOUTH);

        // playbackTimer = new Timer(TIMER_DELAY, new PlaybackActionListener());
        // playbackTimer.setInitialDelay(0);

        setPlaybackStateAndUISync(0.0);

        ppButton.addActionListener(e -> togglePlayPause());
        rewindButton.addActionListener(e -> seek(-10.0));
        forwardButton.addActionListener(e -> seek(10.0));
        replayButton.addActionListener(e -> replayFilm());
        toEndButton.addActionListener(e -> goToEnd());
        timeSlider.addChangeListener(new SliderChangeListener());

        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }

            @Override
            public void componentMoved(ComponentEvent e) {

                refresh();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                refresh();
            }
        });

        App.getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopPlaybackThread();
            }
        });
    }

    /* Public methods */

    public void stopPlaybackThread() {
        running = false; // Signal loop to stop
        isPlaying = false;
        if (visualizerThread != null) {
            visualizerThread.interrupt(); // Interrupt sleep/wait
            try {
                visualizerThread.join(500); // Wait briefly for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Playback thread signaled to stop.");
    }

    public void updateState() {
        currentState = editorPane.getCurrentState(3, TimeUnit.SECONDS);
        totalDuration = computeTotalDuration();
        if (timeSlider != null && !isSeeking) {

            int sliderMax = (int) Math.round(totalDuration * 10);
            timeSlider.setMaximum(sliderMax);
        }
    }

    public void refresh() {
        displayImage(currentImageIndex);

        imageLabel.revalidate();
        imageLabel.repaint();
    }

    /* Private methods */

    private double computeTotalDuration() {
        if (currentState == null)
            return 0.0;
        double totalDuration = 0.0;

        for (Pair<ImageFile, Double> p : currentState) {
            if (p.getValue() != null && p.getValue() > 0)
                totalDuration += p.getValue();
        }

        return totalDuration;
    }

    private String formatTime(double timeInSeconds) {
        int totalSeconds = (int) Math.floor(timeInSeconds);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        int millis = (int) Math.round((timeInSeconds - totalSeconds) * 10);
        return String.format("%02d:%02d.%d", minutes, seconds, millis);
    }

    private void displayImage(int index) {
        if (index >= 0 && index < currentState.size()) {
            ImageFile file = currentState.get(index).getKey();
            BufferedImage img = file.getBufferedImage();
            if (img != null) {
                int panelW = imageLabel.getWidth() > 0 ? imageLabel.getWidth() : 400;
                int panelH = imageLabel.getHeight() > 0 ? imageLabel.getHeight() : 300;
                Image scaledImg = img.getScaledInstance(-1, panelH, Image.SCALE_SMOOTH);
                if (scaledImg.getWidth(null) > panelW) {
                    scaledImg = img.getScaledInstance(panelW, -1, Image.SCALE_SMOOTH);
                }
                imageLabel.setIcon(new ImageIcon(scaledImg));
                imageLabel.setText(null);
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("<html><center>Invalid Image Data<br>(" + file.getFileName() + ")</center></html>");
            }
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("No image to display");
        }
        imageLabel.revalidate();
        imageLabel.repaint();
    }

    private void updateSliderPosition() {
        if (timeSlider != null && !isSeeking) {
            int sliderValue = (int) Math.round(totalElapsedTime * 10);
            timeSlider.setValue(sliderValue);
        }
        if (timeLabel != null) {
            timeLabel.setText(formatTime(totalElapsedTime) + " / " + formatTime(totalDuration));
        }
    }

    private void goToEnd() {
        setPlaybackStateAndUISync(totalDuration);
    }

    private void togglePlayPause() {
        updateState();
        if (isPlaying) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        if (totalElapsedTime >= totalDuration) {
            replayFilm();
        }
        if (!isPlaying) {
            isPlaying = true;

            if (visualizerThread == null || !visualizerThread.isAlive()) {
                visualizerThread = new Thread(visualizerRunnable, "visualizerThread");
                visualizerThread.setDaemon(true);
                visualizerThread.start();
            }
        }
    }

    private void pausePlayback() {
        if (isPlaying) {
            isPlaying = false;
        }
    }

    private void seek(double timeDeltaSeconds) {
        boolean wasPlaying = isPlaying;

        if (wasPlaying) {
            pausePlayback();
        }

        isSeeking = true;
        setPlaybackStateAndUISync(totalElapsedTime + timeDeltaSeconds);
        isSeeking = false;
        if (wasPlaying) {
            startPlayback();
        }
    }

    private void replayFilm() {
        pausePlayback();
        setPlaybackStateAndUISync(0.0);
        timeLabel.setText(formatTime(totalElapsedTime) + " / " + formatTime(totalDuration));
    }

    private void setPlaybackStateAndUISync(double newTime) {
        newTime = Math.max(0.0, Math.min(newTime, totalDuration));

        int targetIndex = -1;
        double calculatedFrameElapsedTime = 0.0;

        if (currentState != null && !currentState.isEmpty() && totalDuration > 0) {
            double timeAccountedFor = 0;
            for (int i = 0; i < currentState.size(); i++) {
                // Similar logic as before to find targetIndex & calculatedFrameElapsedTime...
                // Handle zero duration frames correctly...
                double frameDuration = currentState.get(i).getValue() != null ? currentState.get(i).getValue() : 0.0;
                if (frameDuration <= 0) {
                    if (newTime == 0.0 && i == 0) {
                        targetIndex = 0;
                        calculatedFrameElapsedTime = 0.0;
                        break;
                    }
                    continue;
                }
                double frameEndTime = timeAccountedFor + frameDuration;
                // Use tolerance for comparison
                if (newTime < frameEndTime || Math.abs(newTime - frameEndTime) < (REFRESH_RATE / 1000.0 / 2.0)) {
                    targetIndex = i;
                    calculatedFrameElapsedTime = Math.max(0.0, Math.min(newTime - timeAccountedFor, frameDuration));
                    break;
                }
                timeAccountedFor += frameDuration;
            }
            // Post-loop adjustments if index not found (e.g., newTime is totalDuration)
            if (targetIndex == -1) {
                // Find last valid frame...
                targetIndex = currentState.size() - 1; // Default to last
                for (int i = currentState.size() - 1; i >= 0; i--) {
                    double fd = currentState.get(i).getValue() != null ? currentState.get(i).getValue() : 0.0;
                    if (fd > 0) {
                        targetIndex = i;
                        calculatedFrameElapsedTime = fd;
                        break;
                    }
                }
                if (targetIndex == -1 && !currentState.isEmpty())
                    targetIndex = 0; // Fallback
                newTime = totalDuration; // Snap time to end
                calculatedFrameElapsedTime = currentState.get(targetIndex).getValue();
            }
        } else {
            newTime = 0.0;
            targetIndex = -1;
            calculatedFrameElapsedTime = 0.0;
        }

        this.totalElapsedTime = newTime;
        this.currentImageIndex = targetIndex;
        this.currentFrameElapsedTime = calculatedFrameElapsedTime;

        updateSliderComponent(newTime);
        updateTimeLabelComponent(newTime);
        displayImage(targetIndex);

    }

    private void updateSliderComponent(double time) {
        if (timeSlider != null && !timeSlider.getValueIsAdjusting()) {
            int sliderValue = (int) Math.round(time * 10);
            timeSlider.setValue(sliderValue);
        }
    }

    private void updateTimeLabelComponent(double time) {
        if (timeLabel != null) {
            timeLabel.setText(formatTime(time) + " / " + formatTime(totalDuration));
        }
    }

    /*
     * Custom SliderChangeListener to implement time changing using the slider.
     */
    private class SliderChangeListener implements ChangeListener {
        private boolean wasPlayingBeforeDrag = false;

        @Override
        public void stateChanged(ChangeEvent e) {
            if (timeSlider == null)
                return;

            if (timeSlider.getValueIsAdjusting()) {
                if (!isSeeking) { // Start of drag
                    wasPlayingBeforeDrag = isPlaying;
                    if (wasPlayingBeforeDrag) {
                        pausePlayback(); // Stop background thread updates
                    }
                    isSeeking = true; // Signal seeking state
                }
                // Update time label continuously during drag for feedback
                double sliderTime = timeSlider.getValue() / 10.0;
                updateTimeLabelComponent(sliderTime);

            } else { // End of drag
                if (isSeeking) { // Only process if we initiated the seek
                    double newTime = timeSlider.getValue() / 10.0;
                    // Set state and update UI (synchronously on EDT)
                    setPlaybackStateAndUISync(newTime);
                    // Crucially, allow background thread to resume processing
                    isSeeking = false;

                    // Resume playback if needed
                    if (wasPlayingBeforeDrag && totalElapsedTime < totalDuration) {
                        startPlayback();
                    } else {
                        // Ensure button is Play if drag ended at the very end
                        if (totalElapsedTime >= totalDuration) {
                            isPlaying = false; // Sync state var
                        }
                    }
                }
            }
        }
    }

    /*
     * Custom VisualizerRunnable class to implement film visualizer running logic.
     */
    private class VisualizerRunnable implements Runnable {
        @Override
        public void run() {
            running = true;
            long lastUpdateTimeNs = System.nanoTime();
            long nextRefreshTimeNs = lastUpdateTimeNs;

            while (running) {
                try {
                    long loopStartTimeNs = System.nanoTime();

                    /* Main Logic */
                    if (isPlaying && !isSeeking) {
                        double deltaTimeSec = (loopStartTimeNs - lastUpdateTimeNs) / 1_000_000_000.0;
                        lastUpdateTimeNs = loopStartTimeNs;

                        /* Advance Time */
                        double newTotalElapsedTime = totalElapsedTime + deltaTimeSec;
                        double newFrameElapsedTime = currentFrameElapsedTime + deltaTimeSec;
                        int frameIndex = currentImageIndex; // Use local copy for safety

                        /* Check State Validity */
                        if (frameIndex < 0 || frameIndex >= currentState.size()) {
                            System.err.println("Invalid frame index in background thread: " + frameIndex);
                            SwingUtilities.invokeLater(() -> pausePlayback());
                            continue;
                        }

                        /* End of Film Check */
                        if (newTotalElapsedTime >= totalDuration) {
                            // Update state variables (volatile writes)
                            totalElapsedTime = totalDuration;
                            currentFrameElapsedTime = currentState.get(frameIndex).getValue();

                            final double finalTime = totalDuration;
                            // Schedule UI update and pause on EDT
                            SwingUtilities.invokeLater(() -> {
                                updateSliderComponent(finalTime);
                                updateTimeLabelComponent(finalTime);
                                // Optionally display last frame if needed displayImageComponent(finalIndex);
                                pausePlayback(); // Update button and isPlaying flag
                            });
                            continue; // Skip rest of loop iteration, wait for user action
                        }

                        /* Frame Advancement Check */
                        double currentFrameDuration = currentState.get(frameIndex).getValue();
                        boolean frameChanged = false;

                        // Loop to handle skipping frames with zero/short duration
                        while ((currentFrameDuration <= 0 || newFrameElapsedTime >= currentFrameDuration)
                                && frameIndex < currentState.size() - 1) {
                            double overshoot = (currentFrameDuration <= 0) ? newFrameElapsedTime
                                    : newFrameElapsedTime - currentFrameDuration;
                            frameIndex++;
                            newFrameElapsedTime = overshoot;
                            currentFrameDuration = currentState.get(frameIndex).getValue();
                            frameChanged = true;
                        }

                        /* Update Volatile State Variables */
                        totalElapsedTime = newTotalElapsedTime;
                        currentFrameElapsedTime = newFrameElapsedTime;

                        /* Schedule UI Update on EDT */
                        final double finalTime = newTotalElapsedTime; // Final for lambda
                        final int finalIndex = frameIndex;

                        if (frameChanged) {
                            currentImageIndex = frameIndex; // Update shared index *after* calculation
                            // Load image and update all UI components
                            SwingUtilities.invokeLater(() -> {
                                // Check if still relevant (e.g., user didn't seek in the meantime)
                                if (currentImageIndex == finalIndex) { // Check against volatile state
                                    displayImage(finalIndex); // Loads image on EDT
                                    updateSliderComponent(finalTime);
                                    updateTimeLabelComponent(finalTime);
                                }
                            });
                        } else {
                            // Frame didn't change, but update slider/label periodically if enough time
                            // passed
                            if (loopStartTimeNs >= nextRefreshTimeNs) {
                                SwingUtilities.invokeLater(() -> {
                                    // Check if still relevant
                                    if (!isSeeking) { // Only update if not actively seeking
                                        updateSliderComponent(finalTime);
                                        updateTimeLabelComponent(finalTime);
                                    }
                                });
                                nextRefreshTimeNs = loopStartTimeNs + (long) (REFRESH_RATE * 1_000_000);
                            }
                        }
                    } else {
                        // If paused or seeking, update lastUpdateTime to avoid large jump when resuming
                        lastUpdateTimeNs = loopStartTimeNs;
                    }

                    /* Sleep */
                    // Calculate time taken and sleep for the remainder of the interval
                    long timeTakenNs = System.nanoTime() - loopStartTimeNs;
                    long sleepTimeNs = (long) (REFRESH_RATE * 1_000_000) - timeTakenNs;
                    if (sleepTimeNs > 0) {
                        Thread.sleep(sleepTimeNs / 1_000_000, (int) (sleepTimeNs % 1_000_000));
                    } else {
                        // Yield if loop took too long, prevent busy-waiting
                        Thread.yield();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Re-set interrupt flag
                    running = false; // Signal loop to exit
                    System.out.println("Visualizer thread interrupted.");
                } catch (Exception e) {
                    System.err.println("Error in visualizer thread: " + e);
                    e.printStackTrace();
                    running = false; // Stop on unexpected error
                    SwingUtilities.invokeLater(() -> pausePlayback()); // Attempt to update UI state
                }
            } // end while(running)

            running = false; // Ensure flag is false on exit
        } // end run()
    } // end VisualizerRunnable

}
