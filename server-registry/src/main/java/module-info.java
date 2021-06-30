module aos_server_registry {
	requires undertow.core;
	requires undertow.servlet;
	requires jdk.unsupported; // Needed for undertow support.
	requires java.servlet;
	requires com.fasterxml.jackson.databind;
	requires com.h2database;
	requires java.sql;

	opens nl.andrewlalis.aos_server_registry to com.fasterxml.jackson.databind;
	opens nl.andrewlalis.aos_server_registry.servlet to com.fasterxml.jackson.databind;
	exports nl.andrewlalis.aos_server_registry.servlet to undertow.servlet;
	opens nl.andrewlalis.aos_server_registry.servlet.dto to com.fasterxml.jackson.databind;
}