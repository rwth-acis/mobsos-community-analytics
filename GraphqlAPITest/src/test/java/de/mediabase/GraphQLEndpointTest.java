package de.mediabase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.mediabaseapi.GraphQLEndpoint;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;


public class GraphQLEndpointTest {
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgentImpl testAgent;
	private static final String testPass = "adamspass";

	private static final String mainPath = "graphql/";

	/**
	 * Called before a test starts.
	 * 
	 * Sets up the node, initializes connector and adds user agent that can be used throughout the test.
	 * 
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {
		// start node
		node = new LocalNodeManager().newNode();
		node.launch();

		// add agent to node
		testAgent = MockAgentFactory.getAdam();
		testAgent.unlock(testPass); // agents must be unlocked in order to be stored
		node.storeAgent(testAgent);

		// start service
		// during testing, the specified service version does not matter
		node.startService(new ServiceNameVersion(GraphQLEndpoint.class.getName(), "1.0.0"), "a pass");

		// start connector
		connector = new WebConnector(true, 0, false, 0); // port 0 means use system defined port
		logStream = new ByteArrayOutputStream();
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
	}
	
	/**
	 * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@After
	public void shutDownServer() throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (logStream != null) {
			System.out.println("Connector-Log:");
			System.out.println("--------------");
			System.out.println(logStream.toString());
			logStream = null;
		}
	}
	
	@Test
	public void testquery() {
		try {
			MiniClient client = new MiniClient(); 
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(testAgent.getIdentifier(), testPass);
			
			// encoding for "{" = %7B and "}" = %7D as they are unsafe according to RFC 1738
			String pathing = "testing/query%7Bbw_entries(id:915609)%7Bperma_link%7D%7D";
			ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("{bw_entries=[]}", result.getResponse().trim());
			//Assert.assertEquals("{bw_entries=[{perma_link=http://www.spiegel.de/politik/deutschland/0,1518,561565,00.html}]}",result.getResponse().trim());
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
		
	}
	
	@Test
	public void testgetPrimaryKeys() {
		String filePath = "src/main/resources/config.properties";
		
		Connection con = null;
		try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		    Assert.fail("Exception");
		}
        try {
        	InputStream input = new FileInputStream(filePath);
        	Properties prop = new Properties();
        	prop.load(input);
 		    con = DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
        } catch (SQLException exc) {
 		    System.err.println("getConnection failed: " + exc.toString());
 		   Assert.fail("Exception");
 		} catch (IOException exc) {
 			System.err.println("Input failed: " + exc.toString());
 			Assert.fail("Exception");
 		}
		
        String schema = "DB2INFO5";
        String tableName = "BW_ENTRIES";
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
		String filePath = "src/main/resources/config.properties";
		
		Connection con = null;
		try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		    Assert.fail("Exception");
		}
        try {
        	InputStream input = new FileInputStream(filePath);
        	Properties prop = new Properties();
        	prop.load(input);
 		    con = DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
        } catch (SQLException exc) {
 		    System.err.println("getConnection failed: " + exc.toString());
 		   Assert.fail("Exception");
 		} catch (IOException exc) {
 			System.err.println("Input failed: " + exc.toString());
 			Assert.fail("Exception");
 		}
		
        String schema = "DB2INFO5";
        String tableName = "FW_ATOM_ENTRYITEMCONNECT";
		List<String> foreignKeys = GraphQLEndpoint.getForeignTables(tableName, schema, con);
		
		List<String> compareKeys = new ArrayList<>();
		compareKeys.add("FW_ATOM_ENTRIES");
		compareKeys.add("FW_ATOM_ITEMS");
		
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
