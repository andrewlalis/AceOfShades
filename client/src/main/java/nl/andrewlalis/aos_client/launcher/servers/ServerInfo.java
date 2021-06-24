package nl.andrewlalis.aos_client.launcher.servers;

import java.util.Objects;

public class ServerInfo implements Comparable<ServerInfo> {
	private String name;
	private String host;
	private String username;

	public ServerInfo(String name, String host, String username) {
		this.name = name;
		this.host = host;
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getHostAddress() {
		return this.host.split(":")[0];
	}

	public int getHostPort() {
		String[] parts = this.host.split(":");
		if (parts.length < 2) return 8035;
		return Integer.parseInt(parts[1]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServerInfo that = (ServerInfo) o;
		return getName().equals(that.getName()) && getHost().equals(that.getHost()) && Objects.equals(getUsername(), that.getUsername());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getHost(), getUsername());
	}

	@Override
	public int compareTo(ServerInfo o) {
		return this.name.compareTo(o.name);
	}
}
