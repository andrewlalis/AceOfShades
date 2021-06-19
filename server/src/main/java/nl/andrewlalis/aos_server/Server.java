package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.*;
import nl.andrewlalis.aos_core.net.ChatMessage;
import nl.andrewlalis.aos_core.net.Message;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
	public static final int DEFAULT_PORT = 8035;

	private final List<ClientHandler> clientHandlers;
	private final ServerSocket serverSocket;
	private final DatagramCommunicationThread datagramCommunicationThread;
	private final World world;
	private final WorldUpdater worldUpdater;

	public Server(int port) throws IOException {
		this.clientHandlers = new ArrayList<>();
		this.serverSocket = new ServerSocket(port);
		this.datagramCommunicationThread = new DatagramCommunicationThread(this, port);

		this.world = new World(new Vec2(50, 70));
		world.getBarricades().add(new Barricade(10, 10, 30, 5));
		world.getBarricades().add(new Barricade(10, 55, 30, 5));
		world.getBarricades().add(new Barricade(20, 30, 10, 10));
		world.getBarricades().add(new Barricade(0, 30, 10, 10));
		world.getBarricades().add(new Barricade(40, 30, 10, 10));

		world.getTeams().add(new Team("Red", Color.RED, new Vec2(3, 3), new Vec2(0, 1)));
		world.getTeams().add(new Team("Blue", Color.BLUE, new Vec2(world.getSize().x() - 3, world.getSize().y() - 3), new Vec2(0, -1)));

		this.worldUpdater = new WorldUpdater(this, this.world);
		System.out.println("Started AOS-Server TCP/UDP on port " + port);
	}

	public void acceptClientConnection() throws IOException {
		Socket socket = this.serverSocket.accept();
		var t = new ClientHandler(this, socket);
		t.start();
		synchronized (this.clientHandlers) {
			this.clientHandlers.add(t);
		}
	}

	public int registerNewPlayer(String name) {
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
		System.out.println("Client connected: " + p.getId() + ", " + p.getName());
		this.broadcastMessage(new ChatMessage(name + " connected."));
		this.world.getPlayers().put(p.getId(), p);
		p.setPosition(new Vec2(this.world.getSize().x() / 2.0, this.world.getSize().y() / 2.0));
		if (team != null) {
			team.getPlayers().add(p);
			p.setPosition(team.getSpawnPoint());
			p.setOrientation(team.getOrientation());
			this.broadcastMessage(new ChatMessage(name + " joined team " + team.getName()));
			System.out.println("Player joined team " + team.getName());
		}
		return id;
	}

	public void clientDisconnected(ClientHandler clientHandler) {
		Player player = this.world.getPlayers().get(clientHandler.getPlayerId());
		synchronized (this.clientHandlers) {
			this.clientHandlers.remove(clientHandler);
			clientHandler.shutdown();
		}
		this.world.getPlayers().remove(player.getId());
		if (player.getTeam() != null) {
			player.getTeam().getPlayers().remove(player);
		}
		this.broadcastMessage(new ChatMessage(player.getName() + " disconnected."));
		System.out.println("Client disconnected: " + player.getId() + ", " + player.getName());
	}

	public void sendWorldToClients() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			new ObjectOutputStream(bos).writeObject(this.world);
			byte[] data = bos.toByteArray();
			DatagramPacket packet = new DatagramPacket(data, data.length);
			for (ClientHandler handler : this.clientHandlers) {
				if (handler.getDatagramPort() == -1) continue;
				packet.setAddress(handler.getSocket().getInetAddress());
				packet.setPort(handler.getDatagramPort());
				this.datagramCommunicationThread.getSocket().send(packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePlayerState(PlayerControlState state) {
		Player p = this.world.getPlayers().get(state.getPlayerId());
		if (p != null) {
			p.setState(state);
		}
	}

	public void broadcastMessage(Message message) {
		for (ClientHandler handler : this.clientHandlers) {
			try {
				handler.send(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void broadcastPlayerChat(int playerId, ChatMessage msg) {
		Player p = this.world.getPlayers().get(playerId);
		if (p == null) return;
		this.broadcastMessage(new ChatMessage(p.getName() + ": " + msg.getText()));
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
		server.datagramCommunicationThread.start();
		server.worldUpdater.start();
		while (true) {
			server.acceptClientConnection();
		}
	}


}
