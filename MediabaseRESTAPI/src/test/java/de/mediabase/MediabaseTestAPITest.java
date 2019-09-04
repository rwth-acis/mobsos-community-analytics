/**
 * Test cases for @Link{de.mediabase.MediabaseTESTAPI}
 */
package de.mediabase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.mediabaseapi.MediabaseTestAPI;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.Connector;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;

public class MediabaseTestAPITest {

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgentImpl testAgent;
	private static final String testPass = "adamspass";

	private static final String mainPath = "mediabase/";

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
		node.startService(new ServiceNameVersion(MediabaseTestAPI.class.getName(), "1.0.0"), "a pass");

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
	
	/**
	 * 
	 * Tests the validation method.
	 * 
	 */
	@Test
	public void testGetEntry() {

		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(testAgent.getIdentifier(), testPass);
			
			String pathing = "data/DB2INFO5/BW_ENTRIES/?colname=PERMA_LINK&condition=ID=175105";
			ClientResponse result = client.sendRequest("GET",
					mainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("[{\"perma_link\":\"http://headspicket.de/2008/04/wie-viele-t-shirts/\"}]",
					result.getResponse().trim());
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
			
			pathing = "data/DOES_NOT_EXIST/BW_ENTRIES/?colname=PERMA_LINK&condition=ID=175105";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(454, result.getHttpCode());
			
			pathing = "data/DB2INFO5/NOTHING_HERE/?colname=PERMA_LINK&condition=ID=175105";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(454, result.getHttpCode());
			
			pathing = "data/DB2INFO5/BW_ENTRIES/?colname=PERMA_LINK&condition=ID=42069360";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(455, result.getHttpCode());
			
			pathing = "data/DB2INFO5/BW_ENTRIES/?colname=NOTHING_HERE&condition=ID=175105";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(453, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	@Test
	public void testGetTableNames() {
		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(testAgent.getIdentifier(), testPass);
			
			String pathing = "metadata/DB2INFO5";
			ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("[{\"name\":\"ADVISE_INDEX\"},"
					+ "{\"name\":\"ADVISE_INSTANCE\"},"
					+ "{\"name\":\"ADVISE_MQT\"},"
					+ "{\"name\":\"ADVISE_PARTITION\"},"
					+ "{\"name\":\"ADVISE_TABLE\"},"
					+ "{\"name\":\"ADVISE_WORKLOAD\"},"
					+ "{\"name\":\"BW_AUTHOR\"},"
					+ "{\"name\":\"BW_BURSTS\"},"
					+ "{\"name\":\"BW_COMMENT\"},"
					+ "{\"name\":\"BW_ENTRIES\"},"
					+ "{\"name\":\"BW_MULTIMEDIA\"},"
					+ "{\"name\":\"BW_PROJECTS\"},"
					+ "{\"name\":\"BW_REFERENCES\"},"
					+ "{\"name\":\"BW_TRACKBACKS\"},"
					+ "{\"name\":\"CONTACTS\"},"
					+ "{\"name\":\"COUNTRY\"}]", result.getResponse().trim());
			
			pathing = "metadata/NOTHING_HERE";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(454, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	@Test 
	public void testGetColumnNames() {
		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(testAgent.getIdentifier(), testPass);
			
			String pathing = "metadata/DB2INFO5/BW_ENTRIES";
			ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("[{\"colname\":\"AUTHOR_ID\"},"
					+ "{\"colname\":\"COMMENTLINK\"},"
					+ "{\"colname\":\"CONTENT\"},"
					+ "{\"colname\":\"CONTENT_CHUNK\"},"
					+ "{\"colname\":\"EST_DATE\"},"
					+ "{\"colname\":\"ID\"},"
					+ "{\"colname\":\"INSERT_DATE\"},"
					+ "{\"colname\":\"MOOD\"},"
					+ "{\"colname\":\"PERMA_LINK\"},"
					+ "{\"colname\":\"PROJECT_ID\"},"
					+ "{\"colname\":\"RES_CODE\"},"
					+ "{\"colname\":\"TITLE\"},"
					+ "{\"colname\":\"TRACKBACK_URL\"},"
					+ "{\"colname\":\"WORD_COUNT\"},"
					+ "{\"colname\":\"WORD_SET\"}]", result.getResponse().trim());
			
			pathing = "metadata/NOTHING_HERE/BW_ENTRIES";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(454, result.getHttpCode());
			
			pathing = "metadata/DB2INFO5/NOTHING_HERE";
			result = client.sendRequest("GET", mainPath + pathing, "");
			Assert.assertEquals(454, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
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
