package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.net.*;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler extends Thread {
	private final ExecutorService sendingQueue = Executors.newSingleThreadExecutor();
	private final Server server;
	private final Socket socket;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	private int playerId;

	private volatile boolean running = true;

	public ClientHandler(Server server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
	}

	public int getPlayerId() {
		return playerId;
	}

	public void shutdown() {
		this.running = false;
	}

	public void send(Message message) {
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
		try {
			while (this.running) {
				try {
					Message msg = (Message) this.in.readObject();
					if (msg.getType() == Type.IDENT) {
						IdentMessage ident = (IdentMessage) msg;
						this.playerId = this.server.registerNewPlayer(ident.getName(), this);
					} else if (msg.getType() == Type.CHAT) {
						this.server.handlePlayerChat(this, this.playerId, (ChatMessage) msg);
					} else if (msg.getType() == Type.PLAYER_CONTROL_STATE) {
						this.server.updatePlayerState(((PlayerControlStateMessage) msg).getPlayerControlState());
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// Ignore this exception, consider the client disconnected.
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.server.clientDisconnected(this);
	}
}
