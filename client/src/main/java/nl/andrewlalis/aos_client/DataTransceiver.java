package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.net.data.DataTypes;
import nl.andrewlalis.aos_core.net.data.PlayerDetailUpdate;
import nl.andrewlalis.aos_core.net.data.WorldUpdate;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * This thread is responsible for managing incoming datagram packets, and also
 * offers functionality to send packets back to the server.
 */
public class DataTransceiver extends Thread {
	private final Client client;
	private final DatagramSocket socket;

	private final InetAddress serverAddress;
	private final int serverPort;

	private volatile boolean running;

	/**
	 * Constructs a new data transceiver thread, and immediately initiates a
	 * connection to the server to ensure we've got an "outbound" connection.
	 * @param client A reference to the client this thread is for.
	 * @param serverAddress The server's address to connect to.
	 * @param serverPort The server's port to connect to.
	 * @throws IOException If we could not open the socket and initialize the
	 * connection.
	 */
	public DataTransceiver(Client client, InetAddress serverAddress, int serverPort) throws IOException {
		this.client = client;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.socket = new DatagramSocket();
		this.initiateConnection();
	}

	/**
	 * Initiates the "connection" to the server's UDP socket. This is so that
	 * the router knows that this is an "outbound" connection which the client
	 * initiated. Otherwise, we can't receive UDP packets from the server.
	 * @throws IOException If we couldn't connect.
	 */
	private void initiateConnection() throws IOException {
		boolean established = false;
		int attempts = 0;
		while (!established && attempts < 100) {
			this.send(new byte[]{DataTypes.INIT});
			byte[] buffer = new byte[1400];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			this.socket.receive(packet);
			if (packet.getLength() == 1 && packet.getData()[0] == DataTypes.INIT) {
				established = true;
			}
			attempts++;
		}
		if (!established) {
			throw new IOException("Could not initiate UDP connection after " + attempts + " attempts.");
		}
		System.out.println("Initiated UDP connection with server.");
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	public void send(byte[] bytes) throws IOException {
		if (this.socket.isClosed()) return;
		var packet = new DatagramPacket(bytes, bytes.length, this.serverAddress, this.serverPort);
		this.socket.send(packet);
	}

	public void shutdown() {
		this.running = false;
		if (!this.socket.isClosed()) {
			this.socket.close();
		}
	}

	@Override
	public void run() {
		this.running = true;
		byte[] buffer = new byte[1400];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (this.running) {
			try {
				this.socket.receive(packet);
				ByteBuffer b = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
				byte type = b.get();
				if (type == DataTypes.WORLD_DATA) {
					byte[] worldData = new byte[b.remaining()];
					b.get(worldData);
					WorldUpdate update = WorldUpdate.fromBytes(worldData);
					this.client.updateWorld(update);
				} else if (type == DataTypes.PLAYER_DETAIL) {
					byte[] detailData = new byte[b.remaining()];
					b.get(detailData);
					PlayerDetailUpdate update = PlayerDetailUpdate.fromBytes(detailData);
					this.client.updatePlayer(update);
				}
			} catch (SocketException e) {
				if (!e.getMessage().equals("Socket closed")) {
					e.printStackTrace();
				}
				this.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
