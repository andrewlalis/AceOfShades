package nl.andrewlalis.aos_server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.*;
import nl.andrewlalis.aos_core.model.tools.GunCategory;
import nl.andrewlalis.aos_core.model.tools.GunType;
import nl.andrewlalis.aos_core.net.Message;
import nl.andrewlalis.aos_core.net.PlayerUpdateMessage;
import nl.andrewlalis.aos_core.net.Type;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_core.net.data.DataTypes;
import nl.andrewlalis.aos_core.net.data.PlayerDetailUpdate;
import nl.andrewlalis.aos_core.net.data.WorldUpdate;
import nl.andrewlalis.aos_core.util.ByteUtils;
import nl.andrewlalis.aos_server.settings.ServerSettings;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Server {
	private final ServerSettings settings;

	private final List<ClientHandler> clientHandlers;
	private final ServerSocket serverSocket;
	private final DataTransceiver dataTransceiver;
	private final World world;
	private final WorldUpdater worldUpdater;
	private final ServerCli cli;
	private final ChatManager chatManager;
	private RegistryManager registryManager;

	private volatile boolean running;

	public Server(ServerSettings settings) throws IOException {
		this.settings = settings;
		this.clientHandlers = new CopyOnWriteArrayList<>();
		this.serverSocket = new ServerSocket(settings.getPort());
		this.dataTransceiver = new DataTransceiver(this, settings.getPort());
		this.cli = new ServerCli(this);
		this.world = new World(new Vec2(50, 70));
		this.initWorld();
		this.worldUpdater = new WorldUpdater(this, this.world);
		this.chatManager = new ChatManager(this);
		if (settings.getRegistrySettings().isDiscoverable()) {
			this.registryManager = new RegistryManager(this);
		}
	}

	public ServerSettings getSettings() {
		return settings;
	}

	private void initWorld() {
		for (var gs : this.settings.getGunSettings()) {
			this.world.getGunTypes().put(gs.getName(), new GunType(
				gs.getName(),
				GunCategory.valueOf(gs.getCategory().toUpperCase()),
				gs.getColor(),
				gs.getMaxClipCount(),
				gs.getClipSize(),
				gs.getBulletsPerRound(),
				gs.getAccuracy(),
				gs.getShotCooldownTime(),
				gs.getReloadTime(),
				gs.getBulletSpeed(),
				gs.getBaseDamage()
			));
		}

		world.getBarricades().add(new Barricade(10, 10, 30, 5));
		world.getBarricades().add(new Barricade(10, 55, 30, 5));
		world.getBarricades().add(new Barricade(20, 30, 10, 10));
		world.getBarricades().add(new Barricade(0, 30, 10, 10));
		world.getBarricades().add(new Barricade(40, 30, 10, 10));

		world.getTeams().put((byte) 1, new Team(
			(byte) 1,
			"Red",
			Color.RED,
			new Vec2(3, 3),
			new Vec2(15, 3),
			new Vec2(0, 1)
		));
		world.getTeams().put((byte) 2, new Team(
			(byte) 2,
			"Blue",
			Color.BLUE,
			new Vec2(world.getSize().x() - 3, world.getSize().y() - 3),
			new Vec2(world.getSize().x() - 15, world.getSize().y() - 3),
			new Vec2(0, -1)
		));
	}

	public World getWorld() {
		return world;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public int getPlayerCount() {
		return this.clientHandlers.size();
	}

	public void acceptClientConnection() {
		try {
			Socket socket = this.serverSocket.accept();
			var t = new ClientHandler(this, socket);
			t.start();
			this.clientHandlers.add(t);
		} catch (IOException e) {
			if (e instanceof SocketException && !this.running && e.getMessage().equalsIgnoreCase("Socket closed")) {
				return; // Ignore this exception, since it is expected on shutdown.
			}
			e.printStackTrace();
		}
	}

	public Player registerNewPlayer(String name) {
		int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
		Team team = null;
		for (Team t : this.world.getTeams().values()) {
			if (team == null) {
				team = t;
			} else if (t.getPlayers().size() < team.getPlayers().size()) {
				team = t;
			}
		}
		Player p = new Player(id, name, team, this.world.getGunTypes().get(this.settings.getPlayerSettings().getDefaultGun()), settings.getPlayerSettings().getMaxHealth());
		this.world.getPlayers().put(p.getId(), p);
		String message = p.getName() + " connected.";
		this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, message));
		System.out.println(message);
		p.setPosition(new Vec2(this.world.getSize().x() / 2.0f, this.world.getSize().y() / 2.0f));
		if (team != null) {
			team.getPlayers().add(p);
			p.setPosition(team.getSpawnPoint());
			p.setOrientation(team.getOrientation());
			message = name + " joined team " + team.getName() + ".";
			this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, message));
		}
		this.broadcastMessage(new PlayerUpdateMessage(Type.PLAYER_JOINED, p));
		return p;
	}

	public void clientDisconnected(ClientHandler clientHandler) {
		Player player = clientHandler.getPlayer();
		this.clientHandlers.remove(clientHandler);
		clientHandler.shutdown();
		this.world.getPlayers().remove(player.getId());
		if (player.getTeam() != null) {
			player.getTeam().getPlayers().remove(player);
		}
		String message = player.getName() + " disconnected.";
		this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, message));
		System.out.println(message);
		this.broadcastMessage(new PlayerUpdateMessage(Type.PLAYER_LEFT, player));
	}

	public void kickPlayer(Player player) {
		for (ClientHandler handler : this.clientHandlers) {
			if (handler.getPlayer().getId() == player.getId()) {
				handler.shutdown();
				return;
			}
		}
	}

	public void sendWorldUpdate(WorldUpdate update) {
		try {
			byte[] data = update.toBytes();
			byte[] finalData = new byte[data.length + 1];
			finalData[0] = DataTypes.WORLD_DATA;
			System.arraycopy(data, 0, finalData, 1, data.length);
			for (ClientHandler handler : this.clientHandlers) {
				if (handler.getClientUdpPort() == -1) continue;
				this.dataTransceiver.send(finalData, handler.getClientAddress(), handler.getClientUdpPort());
				byte[] detailData = ByteUtils.prefix(DataTypes.PLAYER_DETAIL, new PlayerDetailUpdate(handler.getPlayer()).toBytes());
				this.dataTransceiver.send(detailData, handler.getClientAddress(), handler.getClientUdpPort());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updatePlayerState(int playerId, PlayerControlState state) {
		Player p = this.world.getPlayers().get(playerId);
		if (p != null) {
			p.setState(state);
		}
	}

	public void resetGame() {
		for (Team t : this.world.getTeams().values()) {
			t.resetScore();
			for (Player p : t.getPlayers()) {
				p.resetStats();
				p.respawn(settings.getPlayerSettings().getMaxHealth());
			}
		}
		broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, "Game has been reset."));
	}

	public void broadcastMessage(Message message) {
		for (ClientHandler handler : this.clientHandlers) {
			handler.send(message);
		}
	}

	public void sendTeamMessage(Team team, Message message) {
		for (ClientHandler handler : this.clientHandlers) {
			if (team.equals(handler.getPlayer().getTeam())) {
				handler.send(message);
			}
		}
	}

	public void shutdown() {
		this.running = false;
		try {
			this.serverSocket.close();
			for (ClientHandler handler : this.clientHandlers) {
				handler.send(new Message(Type.SERVER_SHUTDOWN));
				handler.shutdown();
			}
		} catch (Exception e) {
			System.err.println("Could not close server socket on shutdown: " + e.getMessage());
		}
	}

	public void run() {
		this.running = true;
		this.dataTransceiver.start();
		this.worldUpdater.start();
		this.cli.start();
		System.out.println("Started AOS-Server TCP/UDP on port " + this.serverSocket.getLocalPort() + "; now accepting connections.");
		while (this.running) {
			this.acceptClientConnection();
		}
		this.shutdown();
		System.out.println("Stopped accepting new client connections.");
		this.worldUpdater.shutdown();
		System.out.println("Stopped world updater.");
		this.cli.shutdown();
		System.out.println("Stopped CLI interface.");
		this.dataTransceiver.shutdown();
		System.out.println("Stopped data transceiver.");
		if (this.registryManager != null) {
			this.registryManager.shutdown();
			System.out.println("Stopped registry communications.");
		}
	}



	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var settings = mapper.readValue(Server.class.getClassLoader().getResourceAsStream("default_settings.yaml"), ServerSettings.class);
		Server server = new Server(settings);
		server.run();
	}
}
