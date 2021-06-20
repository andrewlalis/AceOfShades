package nl.andrewlalis.aos_client;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
	private final Map<String, byte[]> soundData = new HashMap<>();

	public void play(String sound) {
		var clip = this.getClip(sound);
		if (clip != null) {
			clip.start();
		}
	}

	private Clip getClip(String sound) {
		var soundBytes = this.soundData.get(sound);
		if (soundBytes == null) {
			InputStream is = Client.class.getResourceAsStream("/nl/andrewlalis/aos_client/sound/" + sound);
			if (is == null) {
				System.err.println("Could not load sound: " + sound);
				return null;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				is.transferTo(bos);
				soundBytes = bos.toByteArray();
				this.soundData.put(sound, soundBytes);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			var ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(soundBytes));
			var clip = AudioSystem.getClip();
			clip.addLineListener(event -> {
				if (event.getType() == LineEvent.Type.STOP) {
					clip.close();
				}
			});
			clip.open(ais);
			return clip;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
}
