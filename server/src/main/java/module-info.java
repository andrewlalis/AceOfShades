module aos_server {
	requires java.logging;
	requires aos_core;
	requires java.desktop;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;

	opens nl.andrewlalis.aos_server.settings to com.fasterxml.jackson.databind;
}