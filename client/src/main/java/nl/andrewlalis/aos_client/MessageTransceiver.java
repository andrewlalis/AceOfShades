package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.net.*;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;

/**
 * This thread is responsible for handling TCP message communication with the
 * server.
 */
public class MessageTransceiver extends Thread {
	private final Client client;

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	private volatile boolean running = true;

	public MessageTransceiver(Client client) {
		this.client = client;
	}

	public void connectToServer(String serverHost, int serverPort, String username) throws IOException {
		this.socket = new Socket(serverHost, serverPort);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());
		this.send(new IdentMessage(username));
		System.out.println("Sent identification packet.");
	}

	public void shutdown() {
		this.running = false;
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void send(Message message) throws IOException {
		this.out.reset();
		this.out.writeObject(message);
	}

	@Override
	public void run() {
		while (this.running) {
			try {
				Message msg = (Message) this.in.readObject();
				if (msg.getType() == Type.PLAYER_REGISTERED) {
					PlayerRegisteredMessage prm = (PlayerRegisteredMessage) msg;
					this.client.initPlayerData(prm.getPlayerId());
				} else if (msg.getType() == Type.CHAT) {
					this.client.addChatMessage((ChatMessage) msg);
				} else if (msg.getType() == Type.WORLD_UPDATE) {
					World world = ((WorldUpdateMessage) msg).getWorld();
					this.client.setWorld(world);
				}
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
				this.running = false;
			} catch (SocketException e) {
				if (!e.getMessage().equalsIgnoreCase("Socket closed")) {
					e.printStackTrace();
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
