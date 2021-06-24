package nl.andrewlalis.aos_client.launcher.servers;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServerInfoListModel extends AbstractListModel<ServerInfo> {
	private final List<ServerInfo> servers;

	public ServerInfoListModel() {
		this.servers = new ArrayList<>();
	}

	public void add(ServerInfo server) {
		if (this.servers.contains(server)) return;
		this.servers.add(server);
		this.servers.sort(Comparator.naturalOrder());
		this.fireContentsChanged(this, 0, this.getSize());
	}

	public void remove(ServerInfo server) {
		int index = this.servers.indexOf(server);
		if (index == -1) return;
		this.servers.remove(index);
		this.fireIntervalRemoved(this, index, index);
	}

	public void serverEdited() {
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public int getSize() {
		return this.servers.size();
	}

	@Override
	public ServerInfo getElementAt(int index) {
		return this.servers.get(index);
	}
}
