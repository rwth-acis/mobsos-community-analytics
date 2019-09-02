package de.mediabaseapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.mediabaseapi.GraphQLEndpoint;

import org.junit.Assert;
import org.junit.Test;

public class GraphQLEndpointTest {
	
	@Test
	public void testgetPrimaryKeys() {
		String url = "jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_meas";
		String username = "db2info5";
		String password = "pfidb52ab";
		
		Connection con = null;
		try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		}
        try {
 	       // TODO get username and password from outside source
 		   con = DriverManager.getConnection(url, username, password);
        } catch (SQLException exc) {
 		    System.err.println("getConnection failed:" + exc.toString());
 		}
		
        String schema = "DB2INFO5";
        String tableName = "BW_BURSTS";
		List<String> primaryKeys = GraphQLEndpoint.getPrimaryKeys(tableName, schema, con);
		
		List<String> compareKeys = new ArrayList<>();
		compareKeys.add("ID");
		
		Assert.assertNotNull(primaryKeys);
		Assert.assertEquals(compareKeys.size(), primaryKeys.size());
		
		if (compareKeys.size() == primaryKeys.size()) {
			for (int i = 0; i < compareKeys.size(); i++) {
				Assert.assertEquals(compareKeys.get(i), primaryKeys.get(i));
			}
		}
		
		schema = "DOES_NOT_EXIST";
        tableName = "DOES_NOT_EXIST";
        
		primaryKeys = GraphQLEndpoint.getPrimaryKeys(tableName, schema, con);
		
		Assert.assertTrue(primaryKeys.isEmpty());
	}
	
	@Test
	public void testgetForeignTables() {
		String url = "jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_meas";
		String username = "db2info5";
		String password = "pfidb52ab";
		
		Connection con = null;
		try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		}
        try {
 	       // TODO get username and password from outside source
 		   con = DriverManager.getConnection(url, username, password);
        } catch (SQLException exc) {
 		    System.err.println("getConnection failed:" + exc.toString());
 		}
		
        String schema = "DB2INFO5";
        String tableName = "BW_BURSTS";
		List<String> foreignKeys = GraphQLEndpoint.getForeignTables(tableName, schema, con);
		
		List<String> compareKeys = new ArrayList<>();
		compareKeys.add("BW_ENTRIES");
		compareKeys.add("BW_PROJECTS");
		
		Assert.assertNotNull(foreignKeys);
		Assert.assertEquals(compareKeys.size(), foreignKeys.size());
		
		
		if (compareKeys.size() == foreignKeys.size()) {
			for (int i = 0; i < compareKeys.size(); i++) {
				Assert.assertEquals(compareKeys.get(i), foreignKeys.get(i));
			}
		}
		
		schema = "DOES_NOT_EXIST";
        tableName = "DOES_NOT_EXIST";
        
		foreignKeys = GraphQLEndpoint.getForeignTables(tableName, schema, con);
		
		Assert.assertTrue(foreignKeys.isEmpty());
	}

}
