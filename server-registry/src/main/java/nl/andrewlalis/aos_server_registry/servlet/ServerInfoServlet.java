package nl.andrewlalis.aos_server_registry.servlet;

import nl.andrewlalis.aos_server_registry.data.DataManager;
import nl.andrewlalis.aos_server_registry.servlet.dto.ServerInfoResponse;
import nl.andrewlalis.aos_server_registry.servlet.dto.ServerInfoUpdate;
import nl.andrewlalis.aos_server_registry.servlet.dto.ServerStatusUpdate;
import nl.andrewlalis.aos_server_registry.util.Requests;
import nl.andrewlalis.aos_server_registry.util.Responses;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerInfoServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
			var results = this.getData(size, page, searchQuery, order, orderDir);
			Responses.ok(resp, new Page<>(results, page, size));
		} catch (SQLException t) {
			t.printStackTrace();
			Responses.internalServerError(resp, "Database error.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var info = Requests.getBody(req, ServerInfoUpdate.class);
		try {
			this.saveNewServer(info);
			Responses.ok(resp, Map.of("message", "Server info saved."));
		} catch (SQLException e) {
			e.printStackTrace();
			Responses.internalServerError(resp, "Database error.");
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var status = Requests.getBody(req, ServerStatusUpdate.class);
		try {
			this.updateServerStatus(status);
			Responses.ok(resp, Map.of("message", "Server status updated."));
		} catch (SQLException e) {
			e.printStackTrace();
			Responses.internalServerError(resp, "Database error.");
		}
	}

	private List<ServerInfoResponse> getData(int size, int page, String searchQuery, String order, String orderDir) throws SQLException {
		final List<ServerInfoResponse> results = new ArrayList<>(20);
		var con = DataManager.getInstance().getConnection();
		String selectQuery = """
			SELECT name, address, updated_at, description, location, max_players, current_players
			FROM servers
			//CONDITIONS
			ORDER BY name
			LIMIT ?
			OFFSET ?
			""";
		selectQuery = selectQuery.replace("ORDER BY name", "ORDER BY " + order + " " + orderDir);
		if (searchQuery != null && !searchQuery.isBlank()) {
			selectQuery = selectQuery.replace("//CONDITIONS", "WHERE UPPER(name) LIKE ?");
		}
		PreparedStatement stmt = con.prepareStatement(selectQuery);
		int index = 1;
		if (searchQuery != null && !searchQuery.isBlank()) {
			stmt.setString(index++, "%" + searchQuery.toUpperCase() + "%");
		}
		stmt.setInt(index++, size);
		stmt.setInt(index, page * size);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			results.add(new ServerInfoResponse(
				rs.getString(1),
				rs.getString(2),
				rs.getTimestamp(3).toInstant().atOffset(ZoneOffset.UTC).toString(),
				rs.getString(4),
				rs.getString(5),
				rs.getInt(6),
				rs.getInt(7)
			));
		}
		stmt.close();
		return results;
	}

	private void saveNewServer(ServerInfoUpdate info) throws SQLException {
		var con = DataManager.getInstance().getConnection();
		PreparedStatement stmt = con.prepareStatement("SELECT name, address FROM servers WHERE name = ? AND address = ?");
		stmt.setString(1, info.name());
		stmt.setString(2, info.address());
		ResultSet rs = stmt.executeQuery();
		boolean exists = rs.next();
		stmt.close();
		if (!exists) {
			PreparedStatement createStmt = con.prepareStatement("""
				INSERT INTO servers (name, address, description, location, max_players, current_players)
				VALUES (?, ?, ?, ?, ?, ?);
				""");
			createStmt.setString(1, info.name());
			createStmt.setString(2, info.address());
			createStmt.setString(3, info.description());
			createStmt.setString(4, info.location());
			createStmt.setInt(5, info.maxPlayers());
			createStmt.setInt(6, info.currentPlayers());
			int rowCount = createStmt.executeUpdate();
			createStmt.close();
			if (rowCount != 1) throw new SQLException("Could not insert new server.");
			System.out.println("Registered new server " + info.name() + " @ " + info.address());
		} else {
			PreparedStatement updateStmt = con.prepareStatement("""
				UPDATE servers SET description = ?, location = ?, max_players = ?, current_players = ?
				WHERE name = ? AND address = ?;
				""");
			updateStmt.setString(1, info.description());
			updateStmt.setString(2, info.location());
			updateStmt.setInt(3, info.maxPlayers());
			updateStmt.setInt(4, info.currentPlayers());
			updateStmt.setString(5, info.name());
			updateStmt.setString(6, info.address());
			int rowCount = updateStmt.executeUpdate();
			updateStmt.close();
			if (rowCount != 1) throw new SQLException("Could not update server.");
			System.out.println("Updated server information for " + info.name() + " @ " + info.address());
		}
	}

	private void updateServerStatus(ServerStatusUpdate status) throws SQLException {
		var con = DataManager.getInstance().getConnection();
		PreparedStatement stmt = con.prepareStatement("""
			UPDATE servers SET current_players = ?
			WHERE name = ? AND address = ?
			""");
		stmt.setInt(1, status.currentPlayers());
		stmt.setString(2, status.name());
		stmt.setString(3, status.address());
		int rowCount = stmt.executeUpdate();
		stmt.close();
		if (rowCount != 1) throw new SQLException("Could not update server status.");
		System.out.println("Updated server status for " + status.name() + " @ " + status.address());
	}
}
