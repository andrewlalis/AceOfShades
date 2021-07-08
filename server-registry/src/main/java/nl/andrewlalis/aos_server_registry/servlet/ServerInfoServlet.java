package nl.andrewlalis.aos_server_registry.servlet;

import nl.andrewlalis.aos_server_registry.data.DataManager;
import nl.andrewlalis.aos_server_registry.servlet.dto.ServerInfoResponse;
import nl.andrewlalis.aos_server_registry.servlet.dto.ServerInfoUpdate;
import nl.andrewlalis.aos_server_registry.servlet.dto.ServerStatusUpdate;
import nl.andrewlalis.aos_server_registry.util.Requests;
import nl.andrewlalis.aos_server_registry.util.Responses;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ServerInfoServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ServerInfoServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int page = Requests.getIntParam(req, "page", 0, i -> i >= 0);
		int size = Requests.getIntParam(req, "size", 20, i -> i >= 5 && i <= 50);
		String searchQuery = Requests.getStringParam(req, "q", null, s -> !s.isBlank());
		String order = Requests.getStringParam(req, "order", "name", s -> !s.isBlank() && (
				s.equalsIgnoreCase("name") ||
				s.equalsIgnoreCase("address") ||
				s.equalsIgnoreCase("location") ||
				s.equalsIgnoreCase("max_players") ||
				s.equalsIgnoreCase("current_players")
		));
		String orderDir = Requests.getStringParam(req, "dir", "ASC", s -> s.equalsIgnoreCase("ASC") || s.equalsIgnoreCase("DESC"));
		try {
			var results = this.getData(size, page, searchQuery, null, order, orderDir);
			Responses.ok(resp, new Page<>(results, page, size, order, orderDir));
		} catch (SQLException t) {
			t.printStackTrace();
			Responses.internalServerError(resp, "Database error.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		var info = Requests.getBody(req, ServerInfoUpdate.class);
		try {
			this.saveNewServer(info);
			Responses.ok(resp, Map.of("message", "Server icon saved."));
		} catch (ResponseStatusException e) {
			Responses.json(resp, e.getStatusCode(), Map.of("message", e.getMessage()));
		} catch (SQLException e) {
			e.printStackTrace();
			Responses.internalServerError(resp, "Database error.");
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		var status = Requests.getBody(req, ServerStatusUpdate.class);
		this.updateServerStatus(status, resp);
	}

	private List<ServerInfoResponse> getData(int size, int page, String searchQuery, String versionQuery, String order, String orderDir) throws SQLException, IOException {
		final List<ServerInfoResponse> results = new ArrayList<>(20);
		var con = DataManager.getInstance().getConnection();
		String selectQuery = """
			SELECT name, address, version, updated_at, description, location, icon, max_players, current_players
			FROM servers
			//CONDITIONS
			ORDER BY name
			LIMIT ?
			OFFSET ?
			""";
		selectQuery = selectQuery.replace("ORDER BY name", "ORDER BY " + order + " " + orderDir);
		List<String> conditions = new ArrayList<>();
		List<Object> conditionParams = new ArrayList<>();
		if (searchQuery != null && !searchQuery.isBlank()) {
			conditions.add("UPPER(name) LIKE ?");
			conditionParams.add("%" + searchQuery.toUpperCase() + "%");
		}
		if (versionQuery != null && !versionQuery.isBlank()) {
			conditions.add("version = ?");
			conditionParams.add(versionQuery);
		}
		if (!conditions.isEmpty()) {
			selectQuery = selectQuery.replace("//CONDITIONS", "WHERE " + String.join(" AND ", conditions));
		}
		PreparedStatement stmt = con.prepareStatement(selectQuery);
		int index = 1;
		for (var param : conditionParams) {
			stmt.setObject(index++, param);
		}
		stmt.setInt(index++, size);
		stmt.setInt(index, page * size);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			// Attempt to load the server's icon, if it is not null.
			InputStream iconInputStream = rs.getBinaryStream(7);
			String encodedIconImage = null;
			if (iconInputStream != null) {
				encodedIconImage = Base64.getUrlEncoder().encodeToString(iconInputStream.readAllBytes());
			}
			results.add(new ServerInfoResponse(
				rs.getString(1),
				rs.getString(2),
				rs.getString(3),
				rs.getTimestamp(4).toInstant().atOffset(ZoneOffset.UTC).toString(),
				rs.getString(5),
				rs.getString(6),
				encodedIconImage,
				rs.getInt(8),
				rs.getInt(9)
			));
		}
		stmt.close();
		return results;
	}

	private void saveNewServer(ServerInfoUpdate info) throws SQLException, ResponseStatusException {
		var con = DataManager.getInstance().getConnection();
		PreparedStatement stmt = con.prepareStatement("SELECT name, address FROM servers WHERE name = ? AND address = ?");
		stmt.setString(1, info.name());
		stmt.setString(2, info.address());
		ResultSet rs = stmt.executeQuery();
		boolean exists = rs.next();
		stmt.close();
		String version = info.version() == null ? "Unknown" : info.version();
		if (!exists) {
			PreparedStatement createStmt = con.prepareStatement("""
				INSERT INTO servers (name, address, version, description, location, icon, max_players, current_players)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?);
				""");
			createStmt.setString(1, info.name());
			createStmt.setString(2, info.address());
			createStmt.setString(3, version);
			createStmt.setString(4, info.description());
			createStmt.setString(5, info.location());
			InputStream inputStream = null;
			if (info.icon() != null) {
				inputStream = new ByteArrayInputStream(Base64.getUrlDecoder().decode(info.icon()));
			}
			createStmt.setBinaryStream(6, inputStream);
			createStmt.setInt(7, info.maxPlayers());
			createStmt.setInt(8, info.currentPlayers());
			int rowCount = createStmt.executeUpdate();
			createStmt.close();
			if (rowCount != 1) throw new SQLException("Could not insert new server.");
			log.info("Registered new server " + info.name() + " @ " + info.address() + " running version " + version + ".");
		} else {
			PreparedStatement updateStmt = con.prepareStatement("""
				UPDATE servers SET version = ?, description = ?, location = ?, icon = ?, max_players = ?, current_players = ?
				WHERE name = ? AND address = ?;
				""");
			updateStmt.setString(1, version);
			updateStmt.setString(2, info.description());
			updateStmt.setString(3, info.location());
			InputStream inputStream = null;
			if (info.icon() != null) {
				inputStream = new ByteArrayInputStream(Base64.getUrlDecoder().decode(info.icon()));
			}
			updateStmt.setBinaryStream(4, inputStream);
			updateStmt.setInt(5, info.maxPlayers());
			updateStmt.setInt(6, info.currentPlayers());
			updateStmt.setString(7, info.name());
			updateStmt.setString(8, info.address());
			int rowCount = updateStmt.executeUpdate();
			updateStmt.close();
			if (rowCount != 1) throw new SQLException("Could not update server.");
			log.info("Updated server information for " + info.name() + " @ " + info.address());
		}
	}

	private void updateServerStatus(ServerStatusUpdate status, HttpServletResponse resp) throws IOException {
		try {
			var con = DataManager.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("""
			UPDATE servers SET current_players = ?, updated_at = CURRENT_TIMESTAMP(0)
			WHERE name = ? AND address = ?
			""");
			stmt.setInt(1, status.currentPlayers());
			stmt.setString(2, status.name());
			stmt.setString(3, status.address());
			int rowCount = stmt.executeUpdate();
			stmt.close();
			if (rowCount != 1) {
				Responses.notFound(resp);
			} else {
				Responses.ok(resp);
				log.info("Status updated for " + status.name() + " @ " + status.address());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Responses.internalServerError(resp, "Database error.");
		}
	}
}
