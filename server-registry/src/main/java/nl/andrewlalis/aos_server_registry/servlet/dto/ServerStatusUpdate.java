package nl.andrewlalis.aos_server_registry.servlet.dto;

public record ServerStatusUpdate (
	String name,
	String address,
	int currentPlayers
) {}
