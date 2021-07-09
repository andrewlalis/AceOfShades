package nl.andrewlalis.aos_client.sound;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.data.SoundData;
import nl.andrewlalis.aos_core.util.TimedCompletableFuture;

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
import java.util.concurrent.ThreadLocalRandom;

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
	private final Map<Player, TimedCompletableFuture<Void>> footstepAudioFutures = new HashMap<>();

	/**
	 * Plays the given list of sounds from the player's perspective.
	 * @param sounds The list of sounds to play.
	 * @param player The player that's hearing the sounds.
	 */
	public void play(List<SoundData> sounds, Player player) {
		for (SoundData sound : sounds) {
			this.play(sound.getType().getSoundName(), sound.getPosition(), sound.getVolume(), player);
		}
	}

	/**
	 * Plays a sound without any perspective, i.e. constant volume no matter
	 * where the player is.
	 * @param sound The sound to play.
	 */
	public void play(SoundData sound) {
		this.play(sound.getType().getSoundName(), sound.getPosition(), sound.getVolume(), null);
	}

	/**
	 * Plays the given sound from the player's perspective.
	 * @param soundName The name of the sound.
	 * @param soundOrigin The origin point of the sound.
	 * @param originalVolume The original volume of the sound.
	 * @param player The player that's hearing the sounds.
	 * @return A future that completes when the sound is done playing.
	 */
	public TimedCompletableFuture<Void> play(String soundName, Vec2 soundOrigin, float originalVolume, Player player) {
		TimedCompletableFuture<Void> cf = new TimedCompletableFuture<>();
		final float volume = this.computeVolume(originalVolume, soundOrigin, player);
		if (volume <= 0.0f) {
			cf.complete(null); // Don't play the sound at all, if its volume is nothing.
			return cf;
		}
		final float pan = this.computePan(soundOrigin, player);
		this.soundPlayerThreadPool.submit(() -> {
			this.play(soundName, pan, volume);
			cf.complete(null);
		});
		return cf;
	}

	/**
	 * Plays the audio clip for a sound, using the given pan and volume settings.
	 * <p>
	 *     This method is blocking, and should ideally be called in a separate
	 *     thread or submitted as a lambda expression to a thread pool.
	 * </p>
	 * @param soundName The sound to play.
	 * @param pan The pan setting, from -1.0 (left) to 1.0 (right).
	 * @param volume The volume, from 0.0 to 1.0.
	 */
	private void play(String soundName, float pan, float volume) {
		try {
			AudioClip clip = this.getAudioClip(soundName);
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

	public void playWalking(Player emitter, Player listener) {
		var f = this.footstepAudioFutures.get(emitter);
		long delay = 500;
		if (emitter.isSprinting()) {
			delay -= 150;
		} else if (emitter.isSneaking()) {
			delay += 150;
		}
		if (f == null || f.getElapsedMillis() > delay) {
			int choice = ThreadLocalRandom.current().nextInt(1, 5);
			var cf = this.play("footsteps" + choice + ".wav", emitter.getPosition(), 0.1f, listener);
			this.footstepAudioFutures.put(emitter, cf);
		}
	}

	/**
	 * Computes the volume that a sound should be played at, from the given
	 * player's perspective.
	 * @param originalVolume The original volume of the sound.
	 * @param soundOrigin The origin point of the sound.
	 * @param player The player that will be hearing the sound.
	 * @return The volume the sound should be played at, from 0.0 to 1.0.
	 */
	private float computeVolume(float originalVolume, Vec2 soundOrigin, Player player) {
		if (player != null && soundOrigin != null) {
			float dist = player.getPosition().dist(soundOrigin);
			originalVolume *= (Math.max(HEARING_RANGE - dist, 0) / HEARING_RANGE);
		}
		return originalVolume;
	}

	private float computePan(Vec2 soundOrigin, Player player) {
		float pan = 0.0f;
		if (player != null && player.getTeam() != null && soundOrigin != null) {
			Vec2 soundDir = soundOrigin
					.sub(player.getPosition())
					.rotate(player.getTeam().getOrientation().perp().angle())
					.unit();
			pan = Math.max(Math.min(soundDir.dot(Vec2.RIGHT), 1.0f), -1.0f);
			if (Float.isNaN(pan)) pan = 0f;
		}
		return pan;
	}

	private AudioClip getAudioClip(String soundName) throws IOException {
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
