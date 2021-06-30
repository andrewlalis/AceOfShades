package nl.andrewlalis.aos_server_registry.servlet.dto;

public record ServerInfoUpdate (
	String name,
	String address,
	String description,
	String location,
	int maxPlayers,
	int currentPlayers
) {}
