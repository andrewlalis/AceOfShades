module aos_core {
	requires java.desktop;
	exports nl.andrewlalis.aos_core.net to aos_server, aos_client;
	exports nl.andrewlalis.aos_core.model to aos_server, aos_client;
	exports nl.andrewlalis.aos_core.geom to aos_server, aos_client;
}