package nl.andrewlalis.aos_client.launcher.servers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PublicServerListModel extends AbstractListModel<PublicServerInfo> {
	public static final String REGISTRY_URL = "http://37.97.207.39:25566/serverInfo";

	private final List<PublicServerInfo> currentPageItems;
	private boolean firstPage;
	private boolean lastPage;
	private int currentPage;
	private String currentQuery;
	private String currentOrder;
	private String currentOrderDir;
	private int pageSize;

	private final HttpClient client;
	private final ObjectMapper mapper;
	private final ScheduledExecutorService executorService;
	private ScheduledFuture<?> pageFetchFuture;
	private final List<Consumer<PublicServerListModel>> modelUpdateListeners;

	public PublicServerListModel() {
		this.currentPageItems = new ArrayList<>();
		this.modelUpdateListeners = new ArrayList<>();
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		this.client = HttpClient.newBuilder()
			.executor(this.executorService)
			.connectTimeout(Duration.ofSeconds(3))
			.build();
		this.mapper = new ObjectMapper();
		this.fetchPage(0, null, null, null);
	}

	public void scheduleAutoPageFetch() {
		this.pageFetchFuture = this.executorService.scheduleAtFixedRate(
				() -> this.fetchPage(this.currentPage, this.currentQuery, this.currentOrder, this.currentOrderDir),
				5,
				5,
				TimeUnit.SECONDS
		);
	}

	public void addListener(Consumer<PublicServerListModel> listener) {
		this.modelUpdateListeners.add(listener);
	}

	public void fetchPage(int page) {
		this.fetchPage(page, this.currentQuery);
	}

	public void fetchPage(int page, String query) {
		this.fetchPage(page, query, this.currentOrder, this.currentOrderDir);
	}

	public void fetchPage(int page, String query, String order, String orderDir) {
		if (this.pageFetchFuture != null && !this.pageFetchFuture.isDone()) {
			this.pageFetchFuture.cancel(false);
		}
		String uri = REGISTRY_URL + "?page=" + page + "&size=" + this.pageSize;
		if (query != null && !query.isBlank()) {
			uri += "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
		}
		if (order != null && !order.isBlank()) {
			uri += "&order=" + URLEncoder.encode(order, StandardCharsets.UTF_8);
		}
		if (orderDir != null && !orderDir.isBlank()) {
			uri += "&dir=" + URLEncoder.encode(orderDir, StandardCharsets.UTF_8);
		}
		System.out.println("Fetching from " + uri);
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder().GET().uri(new URI(uri)).header("Accept", "application/json").build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		var requestFuture = this.client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
		requestFuture.whenCompleteAsync((response, throwable) -> {
			this.currentPageItems.clear();
			this.firstPage = true;
			this.lastPage = true;
			if (throwable != null) {
				System.err.println("Could not request data from registry: " + throwable);
			} else if (response.statusCode() != 200) {
				System.err.println("Non-OK status code: " + response.statusCode());
			} else {
				try {
					JsonNode json = this.mapper.readValue(response.body(), JsonNode.class);
					this.firstPage = json.get("firstPage").asBoolean();
					this.lastPage = json.get("lastPage").asBoolean();
					this.currentPage = json.get("currentPage").asInt();
					this.pageSize = json.get("pageSize").asInt();
					this.currentQuery = query;
					this.currentOrder = json.get("order").asText();
					this.currentOrderDir = json.get("orderDirection").asText();
					for (Iterator<JsonNode> it = json.get("contents").elements(); it.hasNext(); ) {
						this.addServerInfoFromJson(it.next());
					}
					this.scheduleAutoPageFetch();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.fireContentsChanged(this, 0, this.getSize());
			this.modelUpdateListeners.forEach(l -> l.accept(this));
		});
	}

	public boolean isFirstPage() {
		return firstPage;
	}

	public boolean isLastPage() {
		return lastPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	@Override
	public int getSize() {
		return this.currentPageItems.size();
	}

	@Override
	public PublicServerInfo getElementAt(int index) {
		return this.currentPageItems.get(index);
	}

	public void addServerInfoFromJson(JsonNode node) throws IOException {
		Image icon = null;
		JsonNode iconNode = node.get("icon");
		if (iconNode != null && iconNode.getNodeType() == JsonNodeType.STRING) {
			icon = ImageIO.read(new ByteArrayInputStream(Base64.getUrlDecoder().decode(iconNode.textValue())));
		}
		PublicServerInfo info = new PublicServerInfo(
				node.get("name").asText(),
				node.get("address").asText(),
				node.get("version").asText(),
				node.get("description").asText(),
				node.get("location").asText(),
				icon,
				node.get("maxPlayers").asInt(),
				node.get("currentPlayers").asInt()
		);
		this.currentPageItems.add(info);
	}

	public void dispose() {
		this.modelUpdateListeners.clear();
		this.executorService.shutdown();
	}
}
