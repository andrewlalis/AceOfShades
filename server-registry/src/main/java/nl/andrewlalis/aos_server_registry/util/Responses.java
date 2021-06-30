package nl.andrewlalis.aos_server_registry.util;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static nl.andrewlalis.aos_server_registry.ServerRegistry.mapper;

/**
 * Helper class which provides some convenience methods for returning simple
 * JSON responses.
 */
public class Responses {
	public static void ok(HttpServletResponse resp, Object body) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		mapper.writeValue(resp.getOutputStream(), body);
	}

	public static void badRequest(HttpServletResponse resp, String msg) throws IOException {
		respond(resp, HttpServletResponse.SC_BAD_REQUEST, msg);
	}

	public static void notFound(HttpServletResponse resp) throws IOException {
		respond(resp, HttpServletResponse.SC_NOT_FOUND, "Not found.");
	}

	public static void notFound(HttpServletResponse resp, String msg) throws IOException {
		respond(resp, HttpServletResponse.SC_NOT_FOUND, msg);
	}

	public static void internalServerError(HttpServletResponse resp) throws IOException {
		respond(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error.");
	}

	public static void internalServerError(HttpServletResponse resp, String msg) throws IOException {
		respond(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
	}

	private static void respond(HttpServletResponse resp, int status, String msg) throws IOException {
		resp.setStatus(status);
		resp.setContentType("application/json");
		mapper.writeValue(resp.getOutputStream(), msg);
	}

	private static record ResponseBody(String message) {}
}
