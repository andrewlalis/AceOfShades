package nl.andrewlalis.aos_client.launcher.servers;

import nl.andrewlalis.aos_client.launcher.Launcher;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Model which represents the list of servers. This model is backed by a file
 * containing the serialized list, which is updated any time a server is added,
 * edited, or removed.
 */
public class ServerInfoListModel extends AbstractListModel<ServerInfo> {
	public static final Path SERVERS_FILE = Launcher.DATA_DIR.resolve("servers.dat");

	private final List<ServerInfo> servers;

	@SuppressWarnings("unchecked")
	public ServerInfoListModel() {
		List<ServerInfo> list = null;
		if (Files.exists(SERVERS_FILE)) {
			try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(SERVERS_FILE))) {
				list = (ArrayList<ServerInfo>) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (list == null) list = new ArrayList<>();
		servers = list;
	}

	public void add(ServerInfo server) {
		if (this.servers.contains(server)) return;
		this.servers.add(server);
		this.servers.sort(Comparator.naturalOrder());
		this.fireContentsChanged(this, 0, this.getSize());
		this.save();
	}

	public void remove(ServerInfo server) {
		int index = this.servers.indexOf(server);
		if (index == -1) return;
		this.servers.remove(index);
		this.fireIntervalRemoved(this, index, index);
		this.save();
	}

	public void serverEdited() {
		this.fireContentsChanged(this, 0, this.getSize());
		this.save();
	}

	@Override
	public int getSize() {
		return this.servers.size();
	}

	@Override
	public ServerInfo getElementAt(int index) {
		return this.servers.get(index);
	}

	private void save() {
		try {
			Files.createDirectories(SERVERS_FILE.getParent());
			ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(SERVERS_FILE));
			oos.writeObject(this.servers);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
