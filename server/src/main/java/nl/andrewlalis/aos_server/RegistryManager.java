package nl.andrewlalis.aos_server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The registry manager is responsible for keeping the server registry up to
 * date with this server's information, by sending periodic update HTTP messages.
 */
public class RegistryManager {
	/**
	 * The list of retry timings that will be used if the registry server cannot
	 * be reached. Using the retryTimingIndex, we'll start at 5, and increment
	 * each time the connection fails.
	 */
	public static final long[] RETRY_TIMINGS = new long[]{5, 10, 30, 60, 120, 300};
	private int retryTimingIndex = 0;

	private final ScheduledExecutorService executorService;
	private final Server server;

	private final ObjectMapper mapper;
	private final HttpClient httpClient;

	public RegistryManager(Server server) {
		this.server = server;
		this.mapper = new ObjectMapper();
		this.executorService = Executors.newScheduledThreadPool(3);
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
		this.executorService.submit(this::sendInfo);
		this.executorService.scheduleAtFixedRate(
			this::sendUpdate,
			server.getSettings().getRegistrySettings().getUpdateInterval(),
			server.getSettings().getRegistrySettings().getUpdateInterval(),
			TimeUnit.SECONDS
		);
	}

	public void sendInfo() {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("name", this.server.getSettings().getRegistrySettings().getName());
			data.put("address", this.server.getSettings().getRegistrySettings().getAddress());
			data.put("description", this.server.getSettings().getRegistrySettings().getDescription());
			data.put("location", this.server.getSettings().getRegistrySettings().getLocation());
			data.put("icon", this.getIconData());
			data.put("maxPlayers", this.server.getSettings().getMaxPlayers());
			data.put("currentPlayers", 0);
			HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(this.server.getSettings().getRegistrySettings().getRegistryUri() + "/serverInfo"))
				.POST(HttpRequest.BodyPublishers.ofByteArray(this.mapper.writeValueAsBytes(data)))
				.header("Content-Type", "application/json")
				.build();
			try {
				System.out.println("Sending server information to registry...");
				var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() != 200) {
					System.err.println("Non-OK status when sending registry info:\n" + response.body() + "\nAttempting to send again in 10 seconds...");
					this.executorService.schedule(this::sendInfo, 10, TimeUnit.SECONDS);
				} else if (this.retryTimingIndex > 0) {
					this.retryTimingIndex = 0; // Reset the retry timing index if we successfully sent our server info.
				}
			} catch (IOException e) {
				long retryTiming = RETRY_TIMINGS[this.retryTimingIndex];
				System.err.println("Could not send info to registry server. Registry may be offline, or this server may not have internet access. Attempting to resend info in " + retryTiming + " seconds...");
				this.executorService.schedule(this::sendInfo, retryTiming, TimeUnit.SECONDS);
				if (this.retryTimingIndex < RETRY_TIMINGS.length - 1) {
					this.retryTimingIndex++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendUpdate() {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("name", this.server.getSettings().getRegistrySettings().getName());
			data.put("address", this.server.getSettings().getRegistrySettings().getAddress());
			data.put("currentPlayers", server.getPlayerCount());
			HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(this.server.getSettings().getRegistrySettings().getRegistryUri() + "/serverInfo"))
				.PUT(HttpRequest.BodyPublishers.ofByteArray(this.mapper.writeValueAsBytes(data)))
				.header("Content-Type", "application/json")
				.build();
			try {
				var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() != 200) {
					System.err.println("Received non-OK status when sending registry update:\n" + response.body());
				}
			} catch (IOException e) {
				System.err.println("Error sending update to registry server: " + e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getIconData() throws IOException {
		Path iconFile = Path.of("icon.png");
		if (Files.exists(iconFile)) {
			byte[] imageBytes = Files.readAllBytes(iconFile);
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			if (image.getWidth() == 64 && image.getHeight() == 64) {
				return Base64.getUrlEncoder().encodeToString(imageBytes);
			} else {
				System.err.println("icon.png must be 64 x 64.");
			}
		}
		return null;
	}

	public void shutdown() {
		this.executorService.shutdown();
		try {
			while (!this.executorService.awaitTermination(3, TimeUnit.SECONDS)) {
				System.out.println("Waiting for scheduler to terminate.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
