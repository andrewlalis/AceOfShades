package nl.andrewlalis.aos_server.settings;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class that's responsible for loading the settings from their usual
 * location,
 */
public class SettingsLoader {
	public static ServerSettings load() throws IOException {
		Path settingsFile = Path.of("settings.yaml");
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		if (Files.notExists(settingsFile)) {
			System.out.println(settingsFile.getFileName() + " does not exist yet. Initializing with default settings.");
			try (
				InputStream in = SettingsLoader.class.getClassLoader().getResourceAsStream("default_settings.yaml");
				OutputStream out = Files.newOutputStream(settingsFile)
			) {
				if (in == null) throw new IOException("Could not read default settings.");
				in.transferTo(out);
				System.out.println("Initialized server with default settings. Please review these and restart to apply changes.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		InputStream in = Files.newInputStream(settingsFile);
		var settings = mapper.readValue(in, ServerSettings.class);
		in.close();
		return settings;
	}
}
