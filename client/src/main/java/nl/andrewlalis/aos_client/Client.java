package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_client.view.GameFrame;
import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_core.model.PlayerControlState;
import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.net.ChatMessage;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The main class for the client, which connects to a server to join and play.
 */
public class Client {
	public static final int MAX_CHAT_MESSAGES = 10;

	private final int udpPort;
	private DatagramReceiver datagramReceiver;
	private MessageTransceiver messageTransceiver;

	private int playerId;
	private PlayerControlState playerControlState;
	private World world;

	private final List<String> chatMessages;
	private boolean chatting = false;
	private final StringBuilder chatBuffer;

	private final GameRenderer renderer;
	private final GamePanel gamePanel;
	private final SoundManager soundManager;

	public Client(int udpPort) {
		this.udpPort = udpPort;
		this.chatMessages = new LinkedList<>();
		this.chatBuffer = new StringBuilder();
		this.soundManager = new SoundManager();
		this.gamePanel = new GamePanel(this);
		this.renderer = new GameRenderer(this, gamePanel);
	}

	public void connect(String serverHost, int serverPort, String username) throws IOException, ClassNotFoundException {
		this.datagramReceiver = new DatagramReceiver(this, this.udpPort);
		this.datagramReceiver.start();
		this.messageTransceiver = new MessageTransceiver(this);
		this.messageTransceiver.connectToServer(serverHost, serverPort, username, this.udpPort);
		this.messageTransceiver.start();

		while (this.playerControlState == null) {
			try {
				System.out.println("Waiting for server response and player registration...");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		GameFrame g = new GameFrame("Ace of Shades - " + serverHost + ":" + serverPort, this, this.gamePanel);
		g.setVisible(true);
		this.renderer.start();
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
		for (String sound : this.world.getSoundsToPlay()) {
			this.soundManager.play(sound);
		}
	}

	public void initPlayerData(int playerId) {
		this.playerId = playerId;
		this.playerControlState = new PlayerControlState();
		this.playerControlState.setPlayerId(playerId);
	}

	public int getPlayerId() {
		return playerId;
	}

	public PlayerControlState getPlayerState() {
		return playerControlState;
	}

	public void sendPlayerState() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this.playerControlState);
			byte[] buffer = bos.toByteArray();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.messageTransceiver.getRemoteAddress(), this.messageTransceiver.getPort());
			this.datagramReceiver.getDatagramSocket().send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void addChatMessage(String text) {
		this.chatMessages.add(text);
		this.soundManager.play("chat.wav");
		while (this.chatMessages.size() > MAX_CHAT_MESSAGES) {
			this.chatMessages.remove(0);
		}
	}

	public String[] getLatestChatMessages() {
		return this.chatMessages.toArray(new String[0]);
	}

	public boolean isChatting() {
		return this.chatting;
	}

	public void setChatting(boolean chatting) {
		this.chatting = chatting;
		if (this.chatting) {
			this.chatBuffer.setLength(0);
		}
	}

	public void appendToChat(char c) {
		this.chatBuffer.append(c);
	}

	public void backspaceChat() {
		if (this.chatBuffer.length() > 0) {
			this.chatBuffer.setLength(this.chatBuffer.length() - 1);
		}
	}

	public void sendChat() {
		try {
			this.messageTransceiver.send(new ChatMessage(this.chatBuffer.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setChatting(false);
	}

	public String getCurrentChatBuffer() {
		return this.chatBuffer.toString();
	}

	public void shutdown() {
		this.datagramReceiver.shutdown();
		this.messageTransceiver.shutdown();
		this.renderer.shutdown();
	}



	public static void main(String[] args) {
		// Randomly choose a high-level UDP port that's probably open.
		int udpPort = 20000 + ThreadLocalRandom.current().nextInt(0, 10000);

		String hostAndPort = JOptionPane.showInputDialog("Enter server host and port (host:port):");
		if (hostAndPort == null) throw new IllegalArgumentException("A host and port is required.");
		String[] parts = hostAndPort.split(":");
		if (parts.length != 2) throw new IllegalArgumentException("Invalid host:port.");
		String host = parts[0].trim();
		int port = Integer.parseInt(parts[1]);
		String username = JOptionPane.showInputDialog("Enter a username:");
		if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required.");

		Client client = new Client(udpPort);
		try {
			client.connect(host, port, username);
		} catch (IOException | ClassNotFoundException e) {
			client.shutdown();
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not connect:\n" + e.getMessage(), "Connection Error", JOptionPane.WARNING_MESSAGE);
		}
	}
}
