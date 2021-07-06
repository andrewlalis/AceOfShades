package nl.andrewlalis.aos_client.sound;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.data.Sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The sound manager is responsible for playing game sounds, using a cached set
 * of audio clips that are loaded from sound resource files. Sounds are played
 * using a thread pool.
 */
public class SoundManager {
	/**
	 * The range in which a player can hear a sound. If it's further than this
	 * distance, the sound is not played.
	 */
	private static final float HEARING_RANGE = 50.0f;

	/**
	 * The size of the clip buffer used during audio playback, in seconds.
	 */
	private static final float CLIP_BUFFER_SIZE = 1.0f / 10.0f;

	private final ExecutorService soundPlayerThreadPool = Executors.newCachedThreadPool();
	private final Map<String, AudioClip> audioClips = new HashMap<>();

	/**
	 * Plays the given list of sounds from the player's perspective.
	 * @param sounds The list of sounds to play.
	 * @param player The player that's hearing the sounds.
	 */
	public void play(List<Sound> sounds, Player player) {
		for (Sound sound : sounds) {
			this.play(sound, player);
		}
	}

	/**
	 * Plays a sound without any perspective, i.e. constant volume no matter
	 * where the player is.
	 * @param sound The sound to play.
	 */
	public void play(Sound sound) {
		this.play(sound, null);
	}

	/**
	 * Plays the given sound from the player's perspective.
	 * @param sound The sound to play.
	 * @param player The player that's hearing the sounds.
	 */
	public void play(Sound sound, Player player) {
		final float volume = this.computeVolume(sound, player);
		if (volume <= 0.0f) return; // Don't play the sound at all, if its volume is nothing.
		final float pan = this.computePan(sound, player);
		this.soundPlayerThreadPool.submit(() -> this.play(sound, pan, volume));
	}

	/**
	 * Plays the audio clip for a sound, using the given pan and volume settings.
	 * <p>
	 *     This method is blocking, and should ideally be called in a separate
	 *     thread or submitted as a lambda expression to a thread pool.
	 * </p>
	 * @param sound The sound to play.
	 * @param pan The pan setting, from -1.0 (left) to 1.0 (right).
	 * @param volume The volume, from 0.0 to 1.0.
	 */
	private void play(Sound sound, float pan, float volume) {
		try {
			AudioClip clip = this.getAudioClip(sound);
			final int bufferSize = clip.getFormat().getFrameSize() * Math.round(clip.getFormat().getSampleRate() * CLIP_BUFFER_SIZE);
			byte[] buffer = new byte[bufferSize];
			SourceDataLine line = AudioSystem.getSourceDataLine(clip.getFormat());
			line.open(clip.getFormat(), bufferSize);
			line.start();

			// Set pan.
			FloatControl panControl = (FloatControl) line.getControl(FloatControl.Type.PAN);
			panControl.setValue(pan);
			// Set volume.
			volume = Math.max(Math.min(volume, 1.0f), 0.0f);
			FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(20f * (float) Math.log10(volume));

			InputStream source = new ByteArrayInputStream(clip.getSamples());
			int bytesRead = 0;
			while (bytesRead != -1) {
				bytesRead = source.read(buffer, 0, bufferSize);
				if (bytesRead != -1) {
					line.write(buffer, 0, bytesRead);
				}
			}
			line.drain();
			line.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Computes the volume that a sound should be played at, from the given
	 * player's perspective.
	 * @param sound The sound to play.
	 * @param player The player that will be hearing the sound.
	 * @return The volume the sound should be played at, from 0.0 to 1.0.
	 */
	private float computeVolume(Sound sound, Player player) {
		float v = sound.getVolume();
		if (player != null && sound.getPosition() != null) {
			float dist = player.getPosition().dist(sound.getPosition());
			v *= (Math.max(HEARING_RANGE - dist, 0) / HEARING_RANGE);
		}
		return v;
	}

	private float computePan(Sound sound, Player player) {
		float pan = 0.0f;
		if (player != null && player.getTeam() != null && sound.getPosition() != null) {
			Vec2 soundDir = sound.getPosition()
					.sub(player.getPosition())
					.rotate(player.getTeam().getOrientation().perp().angle())
					.unit();
			pan = Math.max(Math.min(soundDir.dot(Vec2.RIGHT), 1.0f), -1.0f);
			if (Float.isNaN(pan)) pan = 0f;
		}
		return pan;
	}

	private AudioClip getAudioClip(Sound sound) throws IOException {
		String soundName = sound.getType().getSoundName();
		AudioClip clip = this.audioClips.get(soundName);
		if (clip == null) {
			clip = new AudioClip("/nl/andrewlalis/aos_client/sound/" + soundName);
			this.audioClips.put(soundName, clip);
		}
		return clip;
	}

	public void close() {
		this.soundPlayerThreadPool.shutdown();
	}
}
