package i5.las2peer.services.mediabaseAPI;

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

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;

public class GraphQLTest {

	private static LocalNode node;
	private static WebConnector graphQLconnector;
	private static ByteArrayOutputStream graphQLlogStream;

	private static UserAgentImpl graphQLtestAgent;
	private static final String graphQLtestPass = "adamspass";

	private static final String graphQLmainPath = "graphql/";
	
	//private static LocalNode RESTnode;
	private static WebConnector RESTconnector;
	private static ByteArrayOutputStream RESTlogStream;
	
	private static UserAgentImpl RESTtestAgent;
	private static final String RESTtestPass = "adamspass";


	/**
	 * Called before a test starts.
	 * 
	 * Sets up the nodes, initializes connectors and adds user agents that can be used throughout the test.
	 * 
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {
		// start node
		node = new LocalNodeManager().newNode();
		node.launch();

		// add agent to node
		RESTtestAgent = MockAgentFactory.getAdam();
		RESTtestAgent.unlock(RESTtestPass); // agents must be unlocked in order to be stored
		node.storeAgent(RESTtestAgent);

		// start service
		// during testing, the specified service version does not matter
		node.startService(new ServiceNameVersion(MediabaseAPI.class.getName(), "1.0.0"), "a pass");

		// start connector
		RESTconnector = new WebConnector(true, 8088, false, 0); // port 0 means use system defined port
		RESTlogStream = new ByteArrayOutputStream();
		RESTconnector.setLogStream(new PrintStream(RESTlogStream));
		RESTconnector.start(node);
		
		// start node
		//node = new LocalNodeManager().newNode();
		//node.launch();

		// add agent to node
		graphQLtestAgent = MockAgentFactory.getAdam();
		graphQLtestAgent.unlock(graphQLtestPass); // agents must be unlocked in order to be stored
		node.storeAgent(graphQLtestAgent);

		// start service
		// during testing, the specified service version does not matter
		node.startService(new ServiceNameVersion(MediabaseGraphQLAPI.class.getName(), "1.0.0"), "a pass");

		// start connector
		graphQLconnector = new WebConnector(true, 9000, false, 0); // port 0 means use system defined port
		graphQLlogStream = new ByteArrayOutputStream();
		graphQLconnector.setLogStream(new PrintStream(graphQLlogStream));
		graphQLconnector.start(node);
		
	}
	
	/**
	 * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@After
	public void shutDownServer() throws Exception {
		if (graphQLconnector != null) {
			graphQLconnector.stop();
			graphQLconnector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (graphQLlogStream != null) {
			System.out.println("GraphQL Connector-Log:");
			System.out.println("--------------");
			System.out.println(graphQLlogStream.toString());
			graphQLlogStream = null;
		}
		
		if (RESTconnector != null) {
			RESTconnector.stop();
			RESTconnector = null;
		}
//		if (RESTnode != null) {
//			RESTnode.shutDown();
//			RESTnode = null;
//		}
		if (RESTlogStream != null) {
			System.out.println("REST Connector-Log:");
			System.out.println("--------------");
			System.out.println(RESTlogStream.toString());
			RESTlogStream = null;
		}
	}
	
	@Test
	public void testquery() {
		try {
			MiniClient client = new MiniClient(); 
			//client.setConnectorEndpoint(graphQLconnector.getHttpEndpoint());
			client.setConnectorEndpoint(graphQLconnector.getHttpEndpoint());
			System.out.println("Endpoint: " + graphQLconnector.getHttpEndpoint());
			client.setLogin(graphQLtestAgent.getIdentifier(), graphQLtestPass);
			
			// encoding for "{" = %7B and "}" = %7D as they are unsafe according to RFC 1738
			String pathing = "graphql?input=query%7Bmediabase_bw_entries(id:915609)%7Bperma_link%7D%7D";
			System.out.println("Path: " + graphQLmainPath + pathing);
			ClientResponse result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testGet': " + result.getResponse());
			Assert.assertEquals("{}", result.getResponse().trim());
			
			pathing = "graphql?input=query%7Bmediabase_bw_entries(id:14)%7Bmood%7D%7D";
			System.out.println("Path: " + graphQLmainPath + pathing);
			result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("{\"mediabase_bw_entries\":[{\"mood\":\"5\"}]}", result.getResponse().trim());
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
			
			pathing = "graphql?input=mutation%7BaddDatabase(name:%22testing%22,"
					+ "url:%22jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_dev%22,"
					+ "dbSchema:%22DB2INFO5%22,"
					+ "user:%22db2info5%22,"
					+ "password:%22pfidb52ab%22,"
					+ "dbType:%22DB2%22)%7D";
			//pathing = "graphql?input=mutation%7BaddDatabase(name:'testing')%7D";
			System.out.println("Path: " + graphQLmainPath + pathing);
			result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			//Assert.assertEquals("{\"mediabase_bw_entries\":[{\"mood\":\"4\"}]}", result.getResponse().trim());
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
			
			pathing = "graphql?input=query%7Btesting_bw_entries(id:16)%7Bmood%7D%7D";
			System.out.println("Path: " + graphQLmainPath + pathing);
			result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("{\"testing_bw_entries\":[{\"mood\":\"4\"}]}", result.getResponse().trim());
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
			
			pathing = "graphql?input=mutation%7BdeleteDatabase(name:%22testing%22)%7D";
			System.out.println("Path: " + graphQLmainPath + pathing);
			result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			//Assert.assertEquals("{\"mediabase_bw_entries\":[{\"mood\":\"4\"}]}", result.getResponse().trim());
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
			
//			pathing = "graphql?input=query%7Ball_reviews%7Bid%7D%7D";
//			System.out.println("Path: " + graphQLmainPath + pathing);
//			result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
//			Assert.assertEquals(200, result.getHttpCode());
//			Assert.assertEquals("{\"mediabase_bw_entries\":[{\"mood\":\"4\"}]}", result.getResponse().trim());
//			System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	//@Test
	public void testDatabaseNames() {
		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(graphQLconnector.getHttpEndpoint());
			System.out.println("Endpoint: " + graphQLconnector.getHttpEndpoint());
			client.setLogin(graphQLtestAgent.getIdentifier(), graphQLtestPass);
			
			// encoding for "{" = %7B and "}" = %7D as they are unsafe according to RFC 1738
			String pathing = "graphql?input=query%7BdatabaseNames%7D";
			System.out.println("Path: " + graphQLmainPath + pathing);
			ClientResponse result = client.sendRequest("GET",  graphQLmainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testGet': " + result.getResponse());
			Assert.assertEquals("{\"databaseNames\":\"[las2peer, mediabase, las2peer_reserve]\"}", result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	//@Test
	public void testgetPrimaryKeys() {
		String filePath = "config.properties";
        try {
        	InputStream input = new FileInputStream(filePath);
        	Properties prop = new Properties();
        	prop.load(input);
    		Connection con = null;
			if (prop.getProperty("db.dbType_mediabase").equals("DB2")) {
				Class.forName("com.ibm.db2.jcc.DB2Driver");
			}
			if (prop.getProperty("db.dbType_mediabase").equals("MySQL")) {
				Class.forName("com.mysql.cj.jdbc.Driver");
			}
			
 		    con = DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
 		    String schema = "DB2INFO5";
 	        String tableName = "BW_ENTRIES";
 			List<String> primaryKeys = MediabaseGraphQLAPI.getPrimaryKeys(tableName, schema, con);
 			
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
 	        
 			primaryKeys = MediabaseGraphQLAPI.getPrimaryKeys(tableName, schema, con);
 			
 			Assert.assertTrue(primaryKeys.isEmpty());
        } catch (SQLException exc) {
 		    System.err.println("getConnection failed: " + exc.toString());
 		   Assert.fail("Exception");
 		} catch (IOException exc) {
 			System.err.println("Input failed: " + exc.toString());
 			Assert.fail("Exception");
 		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load database driver:" + exc.toString());
		    Assert.fail("Exception");
		}
	}
	
	//@Test
	public void testgetForeignTables() {
		String filePath = "config.properties";
        try {
        	InputStream input = new FileInputStream(filePath);
        	Properties prop = new Properties();
        	prop.load(input);
    		Connection con = null;
			if (prop.getProperty("db.dbType_mediabase").equals("DB2")) {
				Class.forName("com.ibm.db2.jcc.DB2Driver");
			}
			if (prop.getProperty("db.dbType_mediabase").equals("MySQL")) {
				Class.forName("com.mysql.cj.jdbc.Driver");
			}
			
			String schema = "DB2INFO5";
	        String tableName = "FW_ATOM_ENTRYITEMCONNECT";
			List<String> foreignKeys = MediabaseGraphQLAPI.getForeignTables(tableName, schema, con);
			
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
	        
			foreignKeys = MediabaseGraphQLAPI.getForeignTables(tableName, schema, con);
			
			Assert.assertTrue(foreignKeys.isEmpty());
        } catch (IOException exc) {
 			System.err.println("Input failed: " + exc.toString());
 			Assert.fail("Exception");
 		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load database driver:" + exc.toString());
		    Assert.fail("Exception");
		}
	}
}
