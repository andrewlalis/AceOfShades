module aos_core {
	requires java.desktop;

	exports nl.andrewlalis.aos_core.net to aos_server, aos_client;
	exports nl.andrewlalis.aos_core.net.chat to aos_client, aos_server;
	exports nl.andrewlalis.aos_core.net.data to aos_server, aos_client;

	exports nl.andrewlalis.aos_core.model to aos_server, aos_client;
	exports nl.andrewlalis.aos_core.model.tools to aos_client, aos_server;

	exports nl.andrewlalis.aos_core.geom to aos_server, aos_client;
	exports nl.andrewlalis.aos_core.util to aos_server, aos_client;
}