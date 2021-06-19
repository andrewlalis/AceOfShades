package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.net.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
	private final Server server;
	private final Socket socket;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	private int datagramPort = -1;
	private int playerId;

	private volatile boolean running = true;

	public ClientHandler(Server server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public Socket getSocket() {
		return socket;
	}

	public int getDatagramPort() {
		return datagramPort;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void shutdown() {
		this.running = false;
	}

	public void send(Message message) throws IOException {
		this.out.writeObject(message);
	}

	@Override
	public void run() {
		try {
			while (this.running) {
				try {
					Message msg = (Message) this.in.readObject();
					if (msg.getType() == Type.IDENT) {
						IdentMessage ident = (IdentMessage) msg;
						int id = this.server.registerNewPlayer(ident.getName());
						this.playerId = id;
						this.datagramPort = ident.getDatagramPort();
						this.send(new PlayerRegisteredMessage(id));
					} else if (msg.getType() == Type.CHAT) {
						this.server.broadcastPlayerChat(this.playerId, (ChatMessage) msg);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// Ignore this exception, consider the client disconnected.
		}
		this.datagramPort = -1;
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.server.clientDisconnected(this);
	}
}
