package nl.andrewlalis.aos_server_registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import nl.andrewlalis.aos_server_registry.data.ServerDataPruner;
import nl.andrewlalis.aos_server_registry.servlet.ServerInfoServlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerRegistry {
	public static final String SETTINGS_FILE = "settings.properties";
	public static final ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws ServletException, IOException {
		Properties props = new Properties();
		props.load(ServerRegistry.class.getResourceAsStream("/nl/andrewlalis/aos_server_registry/defaults.properties"));
		Path settingsPath = Path.of(SETTINGS_FILE);
		if (Files.exists(settingsPath)) {
			props.load(Files.newBufferedReader(settingsPath));
		} else {
			System.out.println("Using built-in default settings. Create a settings.properties file to configure.");
		}

		startServer(Integer.parseInt(props.getProperty("port")));

		// Every few minutes, prune all stale servers from the registry.
		long pruneDelaySeconds = Long.parseLong(props.getProperty("prune-delay"));
		long pruneThresholdMinutes = Long.parseLong(props.getProperty("prune-threshold-minutes"));
		System.out.printf("Will prune servers inactive for more than %d minutes, checking every %d seconds.\n", pruneThresholdMinutes, pruneDelaySeconds);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
		scheduler.scheduleAtFixedRate(new ServerDataPruner(pruneThresholdMinutes), pruneDelaySeconds, pruneDelaySeconds, TimeUnit.SECONDS);
	}

	/**
	 * Starts the Undertow HTTP servlet container.
	 * @param port The port to bind to.
	 * @throws ServletException If the server could not be started.
	 */
	private static void startServer(int port) throws ServletException {
		DeploymentInfo servletBuilder = Servlets.deployment()
			.setClassLoader(ServerRegistry.class.getClassLoader())
			.setContextPath("/")
			.setDeploymentName("AOS Server Registry")
			.addServlets(
				Servlets.servlet("ServersInfoServlet", ServerInfoServlet.class)
					.addMapping("/serverInfo")
			);
		DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
		manager.deploy();
		HttpHandler servletHandler = manager.start();
		Undertow server = Undertow.builder()
			.addHttpListener(port, "localhost")
			.setHandler(servletHandler)
			.build();
		server.start();
	}
}
