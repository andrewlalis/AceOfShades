package nl.andrewlalis.aos_server_registry.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataManager {
	private static final String JDBC_URL = "jdbc:h2:mem:server_registry;MODE=MySQL";

	private static DataManager instance;

	public static DataManager getInstance() throws SQLException {
		if (instance == null) {
			instance = new DataManager();
			instance.resetDatabase();
		}
		return instance;
	}

	private final Connection connection;

	private DataManager() throws SQLException {
		this.connection = DriverManager.getConnection(JDBC_URL);
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void resetDatabase() throws SQLException {
		var in = DataManager.class.getResourceAsStream("/nl/andrewlalis/aos_server_registry/schema.sql");
		if (in == null) throw new SQLException("Missing schema.sql. Cannot reset database.");
		try {
			ScriptRunner runner = new ScriptRunner(this.connection, false, true);
			runner.setErrorLogWriter(new PrintWriter(System.err));
			runner.runScript(new InputStreamReader(in));
			System.out.println("Successfully reset database.");
		} catch (IOException e) {
			throw new SQLException(e);
		}
	}
}
