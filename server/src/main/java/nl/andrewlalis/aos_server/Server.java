package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.*;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.net.Message;
import nl.andrewlalis.aos_core.net.PlayerRegisteredMessage;
import nl.andrewlalis.aos_core.net.WorldUpdateMessage;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;
import nl.andrewlalis.aos_core.net.chat.PlayerChatMessage;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
	public static final int DEFAULT_PORT = 8035;

	private final List<ClientHandler> clientHandlers;
	private final ServerSocket serverSocket;
	private final World world;
	private final WorldUpdater worldUpdater;

	public Server(int port) throws IOException {
		this.clientHandlers = new CopyOnWriteArrayList<>();
		this.serverSocket = new ServerSocket(port);

		this.world = new World(new Vec2(50, 70));
		world.getBarricades().add(new Barricade(10, 10, 30, 5));
		world.getBarricades().add(new Barricade(10, 55, 30, 5));
		world.getBarricades().add(new Barricade(20, 30, 10, 10));
		world.getBarricades().add(new Barricade(0, 30, 10, 10));
		world.getBarricades().add(new Barricade(40, 30, 10, 10));

		world.getTeams().add(new Team(
			"Red",
			Color.RED,
			new Vec2(3, 3),
			new Vec2(15, 3),
			new Vec2(0, 1)
		));
		world.getTeams().add(new Team(
			"Blue",
			Color.BLUE,
			new Vec2(world.getSize().x() - 3, world.getSize().y() - 3),
			new Vec2(world.getSize().x() - 15, world.getSize().y() - 3),
			new Vec2(0, -1)
		));

		this.worldUpdater = new WorldUpdater(this, this.world);
		System.out.println("Started AOS-Server TCP on port " + port);
	}

	public void acceptClientConnection() throws IOException {
		Socket socket = this.serverSocket.accept();
		var t = new ClientHandler(this, socket);
		t.start();
		this.clientHandlers.add(t);
	}

	public int registerNewPlayer(String name, ClientHandler handler) {
		int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
		Team team = null;
		for (Team t : this.world.getTeams()) {
			if (team == null) {
				team = t;
			} else if (t.getPlayers().size() < team.getPlayers().size()) {
				team = t;
			}
		}
		Player p = new Player(id, name, team);
		this.world.getPlayers().put(p.getId(), p);
		handler.send(new PlayerRegisteredMessage(id));
		String message = p.getName() + " connected.";
		this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, message));
		System.out.println(message);
		p.setPosition(new Vec2(this.world.getSize().x() / 2.0, this.world.getSize().y() / 2.0));
		if (team != null) {
			team.getPlayers().add(p);
			p.setPosition(team.getSpawnPoint());
			p.setOrientation(team.getOrientation());
			message = name + " joined team " + team.getName() + ".";
			this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, message));
		}
		return id;
	}

	public void clientDisconnected(ClientHandler clientHandler) {
		Player player = this.world.getPlayers().get(clientHandler.getPlayerId());
		this.clientHandlers.remove(clientHandler);
		clientHandler.shutdown();
		this.world.getPlayers().remove(player.getId());
		if (player.getTeam() != null) {
			player.getTeam().getPlayers().remove(player);
		}
		String message = player.getName() + " disconnected.";
		this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, message));
		System.out.println(message);
	}

	public void sendWorldToClients() {
		for (ClientHandler handler : this.clientHandlers) {
			handler.send(new WorldUpdateMessage(this.world));
		}
	}

	public void updatePlayerState(PlayerControlState state) {
		Player p = this.world.getPlayers().get(state.getPlayerId());
		if (p != null) {
			p.setState(state);
		}
	}

	public void resetGame() {
		for (Team t : this.world.getTeams()) {
			t.resetScore();
			for (Player p : t.getPlayers()) {
				p.respawn();
			}
		}
	}

	public void broadcastMessage(Message message) {
		for (ClientHandler handler : this.clientHandlers) {
			handler.send(message);
		}
	}

	public void handlePlayerChat(ClientHandler handler, int playerId, ChatMessage msg) {
		Player p = this.world.getPlayers().get(playerId);
		if (p == null) return;
		if (msg.getText().startsWith("/")) {
			String[] words = msg.getText().substring(1).split("\\s+");
			if (words.length == 0) return;
			String command = words[0];
			String[] args = Arrays.copyOfRange(words, 1, words.length);
			this.handleCommand(handler, p, command, args);
		} else {
			this.broadcastMessage(new PlayerChatMessage(p.getId(), msg.getText()));
		}
	}

	public void handleCommand(ClientHandler handler, Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("gun")) {
			if (args.length < 1) {
				return;
			}
			String gunName = args[0];
			if (gunName.equalsIgnoreCase("smg")) {
				player.setGun(Gun.ak47());
			} else if (gunName.equalsIgnoreCase("rifle")) {
				player.setGun(Gun.m1Garand());
			} else if (gunName.equalsIgnoreCase("shotgun")) {
				player.setGun(Gun.winchester());
			}
			handler.send(new SystemChatMessage(SystemChatMessage.Level.INFO, "Changed gun to " + player.getGun().getType().name() + "."));
		} else if (command.equalsIgnoreCase("reset")) {
			this.resetGame();
			this.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.INFO, "Game has been reset."));
		}
	}



	public static void main(String[] args) throws IOException {
		System.out.println("Enter the port number to start the server on, or blank for default (" + DEFAULT_PORT + "):");
		Scanner sc = new Scanner(System.in);
		String input = sc.nextLine();
		int port = DEFAULT_PORT;
		if (input != null && !input.isBlank()) {
			try {
				port = Integer.parseInt(input.trim());
			} catch (NumberFormatException e) {
				System.err.println("Invalid port.");
				return;
			}
		}

		Server server = new Server(port);
		server.worldUpdater.start();
		while (true) {
			server.acceptClientConnection();
		}
	}
}
