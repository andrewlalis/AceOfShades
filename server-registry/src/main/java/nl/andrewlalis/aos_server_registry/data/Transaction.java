package nl.andrewlalis.aos_server_registry.data;

import java.sql.Connection;
import java.sql.SQLException;

public interface Transaction {
	void execute(Connection con) throws SQLException;
}
