package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.net.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

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

	public void connectToServer(String serverHost, int serverPort, String username, int udpPort) throws IOException, ClassNotFoundException {
		this.socket = new Socket(serverHost, serverPort);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());
		this.send(new IdentMessage(username, udpPort));
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

	public InetAddress getRemoteAddress() {
		return this.socket != null ? this.socket.getInetAddress() : null;
	}

	public int getPort() {
		return this.socket.getPort();
	}

	public void send(Message message) throws IOException {
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
					this.client.addChatMessage(((ChatMessage) msg).getText());
				}
			} catch (IOException | ClassNotFoundException e) {
				// Ignore exceptions.
			}
		}
	}
}
