package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.data.Sound;
import nl.andrewlalis.aos_core.net.data.SoundType;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundManager {
	private static final float HEARING_RANGE = 50.0f;
	private final Map<String, List<Clip>> soundData = new HashMap<>();
	private final Map<String, Integer> clipIndexes = new HashMap<>();

	public void play(List<Sound> sounds, Player player) {
		for (Sound sound : sounds) {
			this.play(sound, player);
		}
	}
	public void play(List<Sound> sounds) {
		for (Sound sound : sounds) {
			this.play(sound, null);
		}
	}

	public void play(Sound sound) {
		this.play(sound, null);
	}

	public void play(Sound sound, Player player) {
		var clip = this.getClip(sound.getType());
		if (clip == null) {
			return;
		}
		clip.setFramePosition(0);
		float v = sound.getVolume();
		if (player != null && sound.getPosition() != null) {
			float dist = player.getPosition().dist(sound.getPosition());
			v *= (Math.max(HEARING_RANGE - dist, 0) / HEARING_RANGE);
		}
		if (v <= 0.0f) return;
		if (player != null && player.getTeam() != null && sound.getPosition() != null) {
			setPan(clip, player.getPosition(), sound.getPosition(), player.getTeam().getOrientation());
		}
		setVolume(clip, v);
		clip.start();
	}

	private void setVolume(Clip clip, float volume) {
		volume = Math.max(Math.min(volume, 1.0f), 0.0f);
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20f * (float) Math.log10(volume));
	}

	private void setPan(Clip clip, Vec2 playerPos, Vec2 soundPos, Vec2 playerOrientation) {
		Vec2 soundDir = soundPos
				.sub(playerPos)
				.rotate(playerOrientation.perp().angle())
				.unit();
		float pan = Math.max(Math.min(soundDir.dot(Vec2.RIGHT), 1.0f), -1.0f);
		if (Float.isNaN(pan)) pan = 0f;
		FloatControl panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
		panControl.setValue(pan);
	}

	private Clip getClip(SoundType soundType) {
		String sound = soundType.getSoundName();
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
				clips = new ArrayList<>(soundType.getClipBufferCount());
				for (int i = 0; i < soundType.getClipBufferCount(); i++) {
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
		if (index >= soundType.getClipBufferCount()) {
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
