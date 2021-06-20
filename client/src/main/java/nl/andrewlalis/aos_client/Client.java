package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_client.view.ConnectDialog;
import nl.andrewlalis.aos_client.view.GameFrame;
import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_core.model.PlayerControlState;
import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.net.PlayerControlStateMessage;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;
import nl.andrewlalis.aos_core.net.chat.PlayerChatMessage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The main class for the client, which connects to a server to join and play.
 */
public class Client {
	public static final int MAX_CHAT_MESSAGES = 10;

	private MessageTransceiver messageTransceiver;

	private int playerId;
	private PlayerControlState playerControlState;
	private World world;

	private final List<ChatMessage> chatMessages;
	private boolean chatting = false;
	private final StringBuilder chatBuffer;

	private final GameRenderer renderer;
	private final GamePanel gamePanel;
	private final SoundManager soundManager;

	public Client() {
		this.chatMessages = new LinkedList<>();
		this.chatBuffer = new StringBuilder();
		this.soundManager = new SoundManager();
		this.gamePanel = new GamePanel(this);
		this.renderer = new GameRenderer(this, gamePanel);
	}

	public void connect(String serverHost, int serverPort, String username) throws IOException, ClassNotFoundException {
		this.messageTransceiver = new MessageTransceiver(this);
		this.messageTransceiver.connectToServer(serverHost, serverPort, username);
		this.messageTransceiver.start();

		while (this.playerControlState == null || this.world == null) {
			try {
				System.out.println("Waiting for server response and player registration...");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Player and world data initialized.");
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
			this.messageTransceiver.send(new PlayerControlStateMessage(this.playerControlState));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void addChatMessage(ChatMessage message) {
		this.chatMessages.add(message);
		if (message.getClass() == PlayerChatMessage.class) {
			this.soundManager.play("chat.wav");
		}
		while (this.chatMessages.size() > MAX_CHAT_MESSAGES) {
			this.chatMessages.remove(0);
		}
	}

	public ChatMessage[] getLatestChatMessages() {
		return this.chatMessages.toArray(new ChatMessage[0]);
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
		String message = this.chatBuffer.toString().trim();
		if (!message.isBlank() && !message.equals("/")) {
			try {
				this.messageTransceiver.send(new PlayerChatMessage(this.playerId, message));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.setChatting(false);
	}

	public String getCurrentChatBuffer() {
		return this.chatBuffer.toString();
	}

	public void shutdown() {
		this.messageTransceiver.shutdown();
		this.renderer.shutdown();
	}



	public static void main(String[] args) {
		ConnectDialog dialog = new ConnectDialog();
		dialog.setVisible(true);
	}
}
