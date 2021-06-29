package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.net.*;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This thread is responsible for handling TCP message communication with the
 * server.
 */
public class MessageTransceiver extends Thread {
	private final Client client;

	private final Socket socket;
	private final DataTransceiver dataTransceiver;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;
	private final ExecutorService writeService = Executors.newFixedThreadPool(1);

	private volatile boolean running = true;

	public MessageTransceiver(Client client, String serverHost, int serverPort, String username) throws IOException {
		this.client = client;
		this.socket = new Socket(serverHost, serverPort);
		this.dataTransceiver = new DataTransceiver(client, this.socket.getInetAddress(), this.socket.getPort());
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());
		this.initializeConnection(username);
	}

	/**
	 * Initializes the TCP connection to the server. This involves sending an
	 * IDENT packet containing some data the server needs about this client, and
	 * waiting for a {@link PlayerRegisteredMessage} response from the server,
	 * which contains the basic data we need to start the game.
	 * @param username The username for this client.
	 * @throws IOException If the connection could not be initialized.
	 */
	private void initializeConnection(String username) throws IOException {
		boolean established = false;
		int attempts = 0;
		while (!established && attempts < 100) {
			this.send(new IdentMessage(username, this.dataTransceiver.getLocalPort()));
			try {
				Object obj = this.in.readObject();
				if (obj instanceof PlayerRegisteredMessage msg) {
					this.client.setPlayer(msg.getPlayer());
					this.client.setWorld(msg.getWorld());
					established = true;
				} else if (obj instanceof ConnectionRejectedMessage msg) {
					throw new IOException(msg.getMessage());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			attempts++;
		}
		if (!established) {
			throw new IOException("Could not initialize connection to server in " + attempts + " attempts.");
		}
	}

	public void shutdown() {
		this.running = false;
		this.dataTransceiver.shutdown();
		this.writeService.shutdown();
		try {
			this.out.close();
			this.in.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(Message message) {
		if (this.socket.isClosed()) return;
		this.writeService.submit(() -> {
			try {
				this.out.reset();
				this.out.writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void sendData(byte type, int playerId, byte[] data) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1 + Integer.BYTES + data.length);
		buffer.put(type);
		buffer.putInt(playerId);
		buffer.put(data);
		this.dataTransceiver.send(buffer.array());
	}

	@Override
	public void run() {
		this.dataTransceiver.start();
		while (this.running) {
			try {
				Message msg = (Message) this.in.readObject();
				if (msg.getType() == Type.CHAT) {
					this.client.getChatManager().addChatMessage((ChatMessage) msg);
				} else if (msg.getType() == Type.PLAYER_JOINED && this.client.getWorld() != null) {
					PlayerUpdateMessage pum = (PlayerUpdateMessage) msg;
					this.client.getWorld().getPlayers().put(pum.getPlayer().getId(), pum.getPlayer());
				} else if (msg.getType() == Type.PLAYER_LEFT && this.client.getWorld() != null) {
					PlayerUpdateMessage pum = (PlayerUpdateMessage) msg;
					this.client.getWorld().getPlayers().remove(pum.getPlayer().getId());
				} else if (msg.getType() == Type.SERVER_SHUTDOWN) {
					this.client.shutdown();
					JOptionPane.showMessageDialog(null, "Server has been shut down.", "Server Shutdown", JOptionPane.WARNING_MESSAGE);
				}
			} catch (StreamCorruptedException | EOFException e) {
				this.shutdown();
			} catch (SocketException e) {
				if (!e.getMessage().equalsIgnoreCase("Socket closed")) {
					e.printStackTrace();
				}
				this.shutdown();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
