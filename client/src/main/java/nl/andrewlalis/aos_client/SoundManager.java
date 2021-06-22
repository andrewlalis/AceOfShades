package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.net.data.Sound;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundManager {
	private static final int CLIP_COUNT = 10;
	private final Map<String, List<Clip>> soundData = new HashMap<>();
	private final Map<String, Integer> clipIndexes = new HashMap<>();

	public void play(List<Sound> sounds) {
		for (Sound sound : sounds) {
			this.play(sound);
		}
	}

	public void play(Sound sound) {
		var clip = this.getClip(sound.getType().getSoundName());
		if (clip == null) {
			return;
		}
		clip.setFramePosition(0);
		setVolume(clip, sound.getVolume());
		clip.start();
	}

	private void setVolume(Clip clip, float volume) {
		volume = Math.max(Math.min(volume, 1.0f), 0.0f);
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20f * (float) Math.log10(volume));
	}

	private Clip getClip(String sound) {
		var clips = this.soundData.get(sound);
		if (clips == null) {
			InputStream is = Client.class.getResourceAsStream("/nl/andrewlalis/aos_client/sound/" + sound);
			if (is == null) {
				System.err.println("Could not load sound: " + sound);
				return null;
			}
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				is.transferTo(bos);
				byte[] data = bos.toByteArray();
				clips = new ArrayList<>(CLIP_COUNT);
				for (int i = 0; i < CLIP_COUNT; i++) {
					var ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));
					var clip = AudioSystem.getClip();
					clip.open(ais);
					ais.close();
					clips.add(clip);
				}
				this.soundData.put(sound, clips);
				this.clipIndexes.put(sound, 0);
			} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		int index = this.clipIndexes.get(sound);
		if (index >= CLIP_COUNT) {
			index = 0;
		}
		Clip clip = clips.get(index);
		this.clipIndexes.put(sound, index + 1);
		return clip;
	}

	public void close() {
		for (var c : this.soundData.values()) {
			for (var clip : c) {
				clip.close();
			}
		}
	}
}
