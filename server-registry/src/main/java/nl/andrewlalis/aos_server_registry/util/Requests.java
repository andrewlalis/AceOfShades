package nl.andrewlalis.aos_server_registry.util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;

import static nl.andrewlalis.aos_server_registry.ServerRegistry.mapper;

/**
 * Helper methods for working with HTTP requests.
 */
public class Requests {
	public static <T> T getBody(HttpServletRequest req, Class<T> bodyClass) throws IOException {
		return mapper.readValue(req.getInputStream(), bodyClass);
	}

	public static int getIntParam(HttpServletRequest req, String name, int defaultValue, Function<Integer, Boolean> validator) {
		return getParam(req, name, defaultValue, Integer::parseInt, validator);
	}

	public static String getStringParam(HttpServletRequest req, String name, String defaultValue, Function<String, Boolean> validator) {
		return getParam(req, name, defaultValue, s -> s, validator);
	}

	private static <T> T getParam(HttpServletRequest req, String name, T defaultValue, Function<String, T> parser, Function<T, Boolean> validator) {
		var values = req.getParameterValues(name);
		if (values == null || values.length == 0) return defaultValue;
		try {
			T value = parser.apply(values[0]);
			if (!validator.apply(value)) {
				return defaultValue;
			}
			return value;
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
