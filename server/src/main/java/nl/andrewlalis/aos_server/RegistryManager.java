package nl.andrewlalis.aos_server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
	private final ScheduledExecutorService executorService;
	private final Server server;

	private final ObjectMapper mapper;
	private final HttpClient httpClient;

	public RegistryManager(Server server) {
		this.server = server;
		this.mapper = new ObjectMapper();
		this.executorService = Executors.newScheduledThreadPool(3);
		this.httpClient = HttpClient.newBuilder()
			.executor(this.executorService)
			.connectTimeout(Duration.ofSeconds(3))
			.build();
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
			data.put("maxPlayers", this.server.getSettings().getMaxPlayers());
			data.put("currentPlayers", 0);
			HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(this.server.getSettings().getRegistrySettings().getRegistryUri() + "/serverInfo"))
				.POST(HttpRequest.BodyPublishers.ofByteArray(this.mapper.writeValueAsBytes(data)))
				.header("Content-Type", "application/json")
				.build();
			this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
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
			this.httpClient.sendAsync(request, responseInfo -> {
				if (responseInfo.statusCode() != 200) {
					System.out.println("Received non-OK status when sending registry update. Re-sending registry info...");
					this.sendInfo();
				}
				return null;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
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
