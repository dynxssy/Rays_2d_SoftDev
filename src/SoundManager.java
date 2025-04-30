import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private Clip clip;

    public void playMusic(String filePath) {
        try {
            stopMusic(); // Stop any currently playing music
            File musicFile = new File(filePath);  // Load music file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);  // Get the audio stream
            clip = AudioSystem.getClip();  // Get a clip to play the music
            clip.open(audioStream);  // Open the clip with the audio stream
            clip.loop(Clip.LOOP_CONTINUOUSLY);  // Loop the music indefinitely
        } catch (Exception e) {
            e.printStackTrace();  // Handle errors
        }
    }

    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();  // Stop the music if it is playing
        }
    }

    
}
