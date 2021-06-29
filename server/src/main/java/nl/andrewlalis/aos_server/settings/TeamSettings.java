package nl.andrewlalis.aos_server.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamSettings {
	private float spawnPointRadius;
	private float supplyPointRadius;
	@JsonProperty("friendly-fire")
	private boolean friendlyFire;

	public float getSpawnPointRadius() {
		return spawnPointRadius;
	}

	public float getSupplyPointRadius() {
		return supplyPointRadius;
	}

	public boolean friendlyFireEnabled() {
		return friendlyFire;
	}
}
