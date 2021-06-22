package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.IdentMessage;
import nl.andrewlalis.aos_core.net.Message;
import nl.andrewlalis.aos_core.net.PlayerRegisteredMessage;
import nl.andrewlalis.aos_core.net.Type;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread which handles communicating with a single client socket connection.
 */
public class ClientHandler extends Thread {
	private final ExecutorService sendingQueue = Executors.newSingleThreadExecutor();
	private final Server server;
	private final Socket socket;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	private Player player;
	private final InetAddress clientAddress;
	private int clientUdpPort = -1;

	private volatile boolean running = true;

	public ClientHandler(Server server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		this.clientAddress = this.socket.getInetAddress();
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public Player getPlayer() {
		return player;
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public int getClientUdpPort() {
		return clientUdpPort;
	}

	public void shutdown() {
		this.running = false;
		this.sendingQueue.shutdown();
		try {
			this.in.close();
			this.out.close();
			this.socket.close();
		} catch (IOException e) {
			System.err.println("Could not close streams when shutting down client handler for player " + this.player.getId() + ": " + e.getMessage());
		}
	}

	public void send(Message message) {
		if (this.socket.isClosed()) return;
		this.sendingQueue.submit(() -> {
			try {
				this.out.reset();
				this.out.writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void run() {
		while (this.running) {
			try {
				Message msg = (Message) this.in.readObject();
				if (msg.getType() == Type.IDENT) {
					IdentMessage ident = (IdentMessage) msg;
					this.player = this.server.registerNewPlayer(ident.getName());
					this.clientUdpPort = ident.getUdpPort();
					this.send(new PlayerRegisteredMessage(this.player, this.server.getWorld()));
				} else if (msg.getType() == Type.CHAT) {
					this.server.getChatManager().handlePlayerChat(this, this.player, (ChatMessage) msg);
				}
			} catch (SocketException e) {
				if (e.getMessage().equals("Socket closed")) {
					this.shutdown();
				} else {
					e.printStackTrace();
				}
			} catch (EOFException e) {
				this.shutdown();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				this.shutdown();
			}
		}
		this.server.clientDisconnected(this);
	}
}
