package nl.andrewlalis.aos_server.settings;

public class RegistrySettings {
	private boolean discoverable;
	private String registryUri;
	private String name;
	private String address;
	private String description;
	private String location;

	public boolean isDiscoverable() {
		return discoverable;
	}

	public String getRegistryUri() {
		return registryUri;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}
}
