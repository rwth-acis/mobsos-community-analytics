package i5.las2peer.services.mediabaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import i5.las2peer.services.mediabaseAPI.MediabaseAPI;
import i5.las2peer.testing.MockAgentFactory;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
public class RESTTest {


		private static LocalNode node;
		private static WebConnector connector;
		private static ByteArrayOutputStream logStream;

		private static UserAgentImpl testAgent;
		private static final String testPass = "adamspass";

		private static final String mainPath = "rest/";

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
			node.startService(new ServiceNameVersion(MediabaseAPI.class.getName(), "1.0.0"), "a pass");

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
				
				String pathing = "data/mediabase/DB2INFO5/BW_ENTRIES?colname=MOOD&condition=ID=14";
				ClientResponse result = client.sendRequest("GET",
						mainPath + pathing, "");
				Assert.assertEquals(200, result.getHttpCode());
				Assert.assertEquals("[{\"mood\":\"5\"}]",
						result.getResponse().trim());
				
//				pathing = "data/DOES_NOT_EXIST/BW_AUTHOR/?colname=AUTHORURL&condition=ID=175105";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(454, result.getHttpCode());
//				
//				pathing = "data/DB2INFO5/NOTHING_HERE/?colname=AUTHORURL&condition=ID=175105";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(454, result.getHttpCode());
//				
//				pathing = "data/DB2INFO5/BW_AUTHOR/?colname=AUTHORURL&condition=ID=42069360";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(455, result.getHttpCode());
//				
//				pathing = "data/DB2INFO5/BW_AUTHOR/?colname=NOTHING_HERE&condition=ID=175105";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(453, result.getHttpCode());
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail(e.toString());
			}
		}
//		
//		@Test
//		public void testGetTableNames() {
//			try {
//				MiniClient client = new MiniClient();
//				client.setConnectorEndpoint(connector.getHttpEndpoint());
//				client.setLogin(testAgent.getIdentifier(), testPass);
//				
//				String pathing = "metadata/DB2INFO5?views=true";
//				ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(200, result.getHttpCode());
//				Assert.assertEquals("[{\"name\":\"SW_FILENAMES\"},{\"name\":\"SW_ANALYSIS\"},"
//						+ "{\"name\":\"SW_ANALYSIS_DEAD_PROJECT\"},{\"name\":\"LW_MAILANALYSIS\"},"
//						+ "{\"name\":\"SW_RUN_TASKS\"},{\"name\":\"SW_RUN_TASKS_DEAD_PROJECT\"},"
//						+ "{\"name\":\"PROJ_CORRESPONDENCE\"},{\"name\":\"MAVIS_PROJECTS\"},"
//						+ "{\"name\":\"SW_TEMP\"},{\"name\":\"SW_SCREENSHOTS\"},"
//						+ "{\"name\":\"SW_SCREENSHOTS_DEAD_PROJECT\"},{\"name\":\"NW_ENTRIES\"},"
//						+ "{\"name\":\"NW_USED_MIME_PARTS\"},{\"name\":\"NW_URL_REFERENCES\"},"
//						+ "{\"name\":\"LW_URL_REFERENCES\"},{\"name\":\"NW_PROJECTS\"}]",
//						result.getResponse().trim());
//				
//				pathing = "metadata/NOTHING_HERE?views=false";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(454, result.getHttpCode());
//			} catch (Exception e) {
//				e.printStackTrace();
//				Assert.fail(e.toString());
//			}
//		}
//		
//		@Test 
//		public void testGetColumnNames() {
//			try {
//				MiniClient client = new MiniClient();
//				client.setConnectorEndpoint(connector.getHttpEndpoint());
//				client.setLogin(testAgent.getIdentifier(), testPass);
//				
//				String pathing = "metadata/DB2INFO5/BW_AUTHOR";
//				ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(200, result.getHttpCode());
//				Assert.assertEquals("[{\"colname\":\"AUTHORNAME\"},"
//						+ "{\"colname\":\"AUTHORURL\"},{\"colname\":\"ID\"}]",
//						result.getResponse().trim());
//				
//				pathing = "metadata/NOTHING_HERE/BW_AUTHOR";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(454, result.getHttpCode());
//				
//				pathing = "metadata/DB2INFO5/NOTHING_HERE";
//				result = client.sendRequest("GET", mainPath + pathing, "");
//				Assert.assertEquals(454, result.getHttpCode());
//				
//			} catch (Exception exc) {
//				exc.printStackTrace();
//				Assert.fail(exc.toString());
//			}
//		}
//		
//		@Test
//		public void testPutEntry() {
//			
//			try {
//				MiniClient client = new MiniClient();
//				client.setConnectorEndpoint(connector.getHttpEndpoint());
//				client.setLogin(testAgent.getIdentifier(), testPass);
//				String pathing = "data/DB2INFO5/BW_AUTHOR/";
//				String json = "{\"AUTHORNAME\":\"Test Testing\", \"AUTHORURL\":\"www.uniquetest.de\"}";
//				System.out.println("JSON: " + json);
//				ClientResponse result = client.sendRequest("PUT", mainPath + pathing, json);
//				Assert.assertEquals(200, result.getHttpCode());
//			} catch (Exception exc) {
//				exc.printStackTrace();
//				Assert.fail(exc.toString());
//			}
//		}
//		
//		@Test
//		public void testDeleteEntry() {
//			try {
//				MiniClient client = new MiniClient();
//				client.setConnectorEndpoint(connector.getHttpEndpoint());
//				client.setLogin(testAgent.getIdentifier(), testPass);
//				String pathing = "data/DB2INFO5/BW_AUTHOR?condition=AUTHORNAME=\'www.uniquetest.de\'";
//				ClientResponse result = client.sendRequest("DELETE", mainPath + pathing, "");
//				Assert.assertEquals(200, result.getHttpCode());
//			} catch (Exception exc) {
//				exc.printStackTrace();
//				Assert.fail(exc.toString());
//			}
//		}
//		
		@Test
		public void testDbConnection() {
			String filePath = "config.properties";
			String dbName = "mediabase";
			Connection connection = MediabaseAPI.dbConnection(filePath, dbName);

			Assert.assertNotNull(connection);
			try {
				connection.close();
			} catch (SQLException e) {
				System.out.println("Exception");
			}
			
			filePath = "";
			System.out.println("connection");
			connection = MediabaseAPI.dbConnection(filePath, dbName);
			Assert.assertNull(connection);
		}
//		
//		@Test
//		public void testGetAutoGenerated() {
//			String filePath = "src/main/resources/config.properties";
//			String dbName = "testdb";
//			Connection connection = MediabaseAPI.dbConnection(filePath, dbName);
//			
//			Assert.assertNotNull(connection);
//			List<String> expected = new ArrayList<>();
//			expected.add("ID");
//			
//			List<String> actual = MediabaseAPI.getAutoGenerated(connection, dbName, "DB2INFO5", "BW_AUTHOR");
//			Assert.assertFalse(actual.isEmpty());
//			Assert.assertEquals(expected.size(), actual.size());
//			for (int i = 0; i < expected.size(); i++) {
//				Assert.assertEquals(expected.get(i), actual.get(i));
//			}
//			
//			actual = MediabaseAPI.getAutoGenerated(connection, dbName, "FAILURE", "NOTHING_HERE");
//			Assert.assertTrue(actual.isEmpty());
//		}

	}
