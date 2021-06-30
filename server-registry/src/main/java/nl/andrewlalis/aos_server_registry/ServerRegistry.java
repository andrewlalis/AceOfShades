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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerRegistry {
	public static final int PORT = 8567;
	public static final ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws ServletException {
		startServer();
		// Every few minutes, prune all stale servers from the registry.
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
		scheduler.scheduleAtFixedRate(new ServerDataPruner(), 1, 1, TimeUnit.MINUTES);
	}

	private static void startServer() throws ServletException {
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
			.addHttpListener(PORT, "localhost")
			.setHandler(servletHandler)
			.build();
		server.start();
	}
}
