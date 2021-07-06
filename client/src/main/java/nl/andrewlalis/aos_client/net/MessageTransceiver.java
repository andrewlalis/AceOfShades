package nl.andrewlalis.aos_client.net;

import nl.andrewlalis.aos_client.Client;
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
 * server. During its {@link MessageTransceiver#run()} method, it will try to
 * receive objects from the server, and process them.
 * <p>
 *     It also manages an internal UDP transceiver for sending and receiving
 *     high volume, lightweight data packets about things like world updates and
 *     player input events.
 * </p>
 */
public class MessageTransceiver extends Thread {
	/**
	 * A reference to the client that this transceiver thread is working for.
	 */
	private final Client client;

	/**
	 * The TCP socket that's used for communication.
	 */
	private final Socket socket;

	/**
	 * An internal datagram transceiver that is used for UDP communication.
	 */
	private final DataTransceiver dataTransceiver;

	/**
	 * Output stream that is used for sending objects to the server.
	 */
	private final ObjectOutputStream out;

	/**
	 * Input stream that is used for receiving objects from the server.
	 */
	private final ObjectInputStream in;

	/**
	 * A single-threaded executor that is used to queue and send messages to the
	 * server sequentially without blocking the main transceiver thread.
	 */
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

	/**
	 * Sends a message to the server, by submitting it to the write service's
	 * queue.
	 * @param message The message to send.
	 */
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

	/**
	 * Sends a packet via UDP to the server.
	 * @param type The type of data to send.
	 * @param playerId The id of the player.
	 * @param data The data to send.
	 * @throws IOException If the data could not be sent.
	 */
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
