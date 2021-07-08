package nl.andrewlalis.aos_server_registry.servlet.dto;

public record ServerInfoResponse(
	String name,
	String address,
	String version,
	String updatedAt,
	String description,
	String location,
	String icon,
	int maxPlayers,
	int currentPlayers
) {}
