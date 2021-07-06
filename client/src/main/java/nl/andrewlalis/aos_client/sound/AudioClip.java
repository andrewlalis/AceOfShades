package nl.andrewlalis.aos_client.sound;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.data.Sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple container object for an in-memory audio clip. The contents of this
 * clip are played using a {@link javax.sound.sampled.SourceDataLine} during
 * runtime.
 * @see SoundManager#play(Sound, Player)
 */
public class AudioClip {
	private final AudioFormat format;
	private final byte[] samples;

	/**
	 * Constructs a new audio clip, using the given resource name to load audio
	 * data from a classpath resource.
	 * @param resource The name of the classpath resource to load.
	 * @throws IOException If the clip could not be loaded.
	 */
	public AudioClip(String resource) throws IOException {
		try {
			InputStream inputStream = AudioClip.class.getResourceAsStream(resource);
			if (inputStream == null) throw new IOException("Could not get resource as stream: " + resource);
			AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
			this.format = in.getFormat();
			this.samples = in.readAllBytes();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public AudioFormat getFormat() {
		return format;
	}

	public byte[] getSamples() {
		return samples;
	}
}
