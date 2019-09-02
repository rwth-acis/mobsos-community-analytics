package de.mediabaseapi;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import de.mediabaseapi.MediabaseTestAPI;

import org.junit.Assert;
import org.junit.Test;

public class MediabaseTestAPITest {

	@Test
	public void testDbConnection() {
		String url = "jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_meas";
		String username = "db2info5";
		String password = "pfidb52ab";
		Connection connection = MediabaseTestAPI.dbConnection(url, username, password);

		Assert.assertNotNull(connection);
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("Exception");
		}
		
		
		url = "";
		username = "";
		password = "";
		System.out.println("connection");
		connection = MediabaseTestAPI.dbConnection(url, username, password);
		Assert.assertNull(connection);
	}

}
