package i5.las2peer.services.mediabaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
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
			
			// test cases are designed for specific databases, credentials are not publicly available
			// therefore tests should be skipped
			Assume.assumeTrue(getJUNIT().equals("true"));
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
				
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail(e.toString());
			}
		}
		
		@Test 
		public void addAndDeleteDatabase() {
			try {
				MiniClient client = new MiniClient(); 
				client.setConnectorEndpoint(connector.getHttpEndpoint());
				client.setLogin(testAgent.getIdentifier(), testPass);
				
				String pathing = "database/testing";
				String properties = "{\"name\":\"testing\","
						+ " \"url\":\"jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_dev\","
						+ " \"dbSchema\":\"DB2INFO5\", \"user\":\"db2info5\", \"password\":\"pfidb52ab\","
						+ " \"dbType\":\"DB2\"}";
				ClientResponse result = client.sendRequest("POST",
						mainPath + pathing, properties);
				Assert.assertEquals(201, result.getHttpCode());
				
				client = new MiniClient(); 
				client.setConnectorEndpoint(connector.getHttpEndpoint());
				client.setLogin(testAgent.getIdentifier(), testPass);
				
				pathing = "database/testing";
				result = client.sendRequest("DELETE",
						mainPath + pathing, "");
				Assert.assertEquals(200, result.getHttpCode());
				
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail(e.toString());
			}
		}
		
		@Test
		public void testGetDatabaseNames() {
			try {
				MiniClient client = new MiniClient(); 
				client.setConnectorEndpoint(connector.getHttpEndpoint());
				client.setLogin(testAgent.getIdentifier(), testPass);
				
				String pathing = "listDatabases";
				ClientResponse result = client.sendRequest("GET",
						mainPath + pathing, "");
				Assert.assertEquals(200, result.getHttpCode());
				Assert.assertEquals("[las2peer, mediabase, las2peer_reserve]",
				result.getResponse().trim());
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
				
				String pathing = "metadata/mediabase/DB2INFO5?views=true";
				ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
				Assert.assertEquals(200, result.getHttpCode());
				Assert.assertEquals("[{\"name\":\"SW_FILENAMES\"},{\"name\":\"SW_ANALYSIS\"},"
						+ "{\"name\":\"SW_ANALYSIS_DEAD_PROJECT\"},"
						+ "{\"name\":\"LW_MAILANALYSIS\"},{\"name\":\"SW_RUN_TASKS\"},"
						+ "{\"name\":\"SW_RUN_TASKS_DEAD_PROJECT\"},"
						+ "{\"name\":\"PROJ_CORRESPONDENCE\"},"
						+ "{\"name\":\"MAVIS_PROJECTS\"},"
						+ "{\"name\":\"SW_TEMP\"},{\"name\":\"SW_SCREENSHOTS\"},"
						+ "{\"name\":\"SW_SCREENSHOTS_DEAD_PROJECT\"},"
						+ "{\"name\":\"NW_ENTRIES\"},{\"name\":\"NW_USED_MIME_PARTS\"},"
						+ "{\"name\":\"NW_URL_REFERENCES\"},{\"name\":\"LW_URL_REFERENCES\"},"
						+ "{\"name\":\"NW_PROJECTS\"},{\"name\":\"UD_CLASSIFICATION\"},"
						+ "{\"name\":\"SW_PROJECTS\"},{\"name\":\"FW_PROJECTS\"},"
						+ "{\"name\":\"LW_PROJECT_WORD_OF_DAY\"},"
						+ "{\"name\":\"LW_PERSON_MAIL\"},{\"name\":\"SWAPIT_STOPWORDS_OLD\"},"
						+ "{\"name\":\"WORD_OF_DAY_STOPWORDS\"},{\"name\":\"EGRAECULI\"},"
						+ "{\"name\":\"LW_WORD_OF_DAY\"},{\"name\":\"LW_PROJECTS_MONTH_ANALYSIS\"},"
						+ "{\"name\":\"LW_THREAD_TREE\"},{\"name\":\"NW_PROJECT_REGEX_CHECK\"},"
						+ "{\"name\":\"LW_PROJECT_REGEX_CHECK\"},{\"name\":\"T20060606_084327\"},"
						+ "{\"name\":\"FW_ATOM_ENTRIES\"},{\"name\":\"LW_PROJECTS\"},"
						+ "{\"name\":\"PROJ_LANG\"},{\"name\":\"UDC\"},{\"name\":\"SUB_UDC\"},"
						+ "{\"name\":\"SUB_UDC_RELATION\"},{\"name\":\"FW_RSS_RUN\"},"
						+ "{\"name\":\"FW_ATOM_RUN\"},{\"name\":\"LW_PROJECT_ANALYSIS_TEMP\"},"
						+ "{\"name\":\"LW_ENTRIES\"},{\"name\":\"LW_USED_MIME_PARTS\"},"
						+ "{\"name\":\"FW_ATOM_ENTRYITEMCONNECT\"},{\"name\":\"FW_RSS_ENTRYITEMCONNECT\"},"
						+ "{\"name\":\"FW_RSS_ITEMS\"},{\"name\":\"T20060606_084327_EXCEPTION\"},"
						+ "{\"name\":\"FW_ATOM_ITEMS\"},{\"name\":\"FW_RSS_ENTRIES\"},"
						+ "{\"name\":\"LW_ENTRIES_TT\"},{\"name\":\"TEMP_LW_MONTH_ANALYSIS\"},"
						+ "{\"name\":\"EXPLAIN_INSTANCE\"},{\"name\":\"EXPLAIN_STATEMENT\"},"
						+ "{\"name\":\"EXPLAIN_ARGUMENT\"},{\"name\":\"EXPLAIN_OBJECT\"},"
						+ "{\"name\":\"EXPLAIN_OPERATOR\"},{\"name\":\"EXPLAIN_PREDICATE\"},"
						+ "{\"name\":\"EXPLAIN_STREAM\"},{\"name\":\"ADVISE_INSTANCE\"},"
						+ "{\"name\":\"ADVISE_INDEX\"},{\"name\":\"ADVISE_WORKLOAD\"},{\"name\":\"ADVISE_MQT\"},"
						+ "{\"name\":\"ADVISE_PARTITION\"},{\"name\":\"ADVISE_TABLE\"},"
						+ "{\"name\":\"REGIST_INFO\"},{\"name\":\"EVENT\"},{\"name\":\"LW_PROJECT_ANALYSIS\"},"
						+ "{\"name\":\"LW_PERSON_ADDR\"},{\"name\":\"COUNTRY\"},{\"name\":\"CONTACTS\"},"
						+ "{\"name\":\"BW_AUTHOR\"},{\"name\":\"BW_COMMENT\"},{\"name\":\"BW_MULTIMEDIA\"},"
						+ "{\"name\":\"BW_REFERENCES\"},{\"name\":\"BW_TRACKBACKS\"},{\"name\":\"BW_ENTRIES\"},"
						+ "{\"name\":\"BW_PROJECTS\"},{\"name\":\"LW_WOTD\"},{\"name\":\"P_MEMBER_MAILING_LIST\"},"
						+ "{\"name\":\"P_MEMBER_NETWORK\"},{\"name\":\"P_COMMON_MEMBER\"},{\"name\":\"P_THREAD\"},"
						+ "{\"name\":\"P_MEMBER_URL\"},{\"name\":\"P_CROSSMEDIA\"},{\"name\":\"P_GATEKEEPER\"},"
						+ "{\"name\":\"P_ANSWERING_PERSON\"},{\"name\":\"P_CONVERSATIONALIST\"},"
						+ "{\"name\":\"P_QUESTIONEER\"},{\"name\":\"P_SPAMMER\"},"
						+ "{\"name\":\"SWAPIT_COLLECTIONS\"},{\"name\":\"SWAPIT_STOPWORDS\"},"
						+ "{\"name\":\"FW_CHECKOFCHANGE_HELP\"}]",
						result.getResponse().trim());
				
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
				
				String pathing = "metadata/mediabase/DB2INFO5/BW_AUTHOR";
				ClientResponse result = client.sendRequest("GET", mainPath + pathing, "");
				Assert.assertEquals(200, result.getHttpCode());
				Assert.assertEquals("[{\"colname\":\"AUTHORNAME\","
						+ "\"nulls\":\"N\",\"typename\":\"VARCHAR\"},"
						+ "{\"colname\":\"AUTHORURL\",\"nulls\":\"Y\","
						+ "\"typename\":\"VARCHAR\"},{\"colname\":\"ID\","
						+ "\"nulls\":\"N\",\"typename\":\"BIGINT\"}]",
						result.getResponse().trim());
				
			} catch (Exception exc) {
				exc.printStackTrace();
				Assert.fail(exc.toString());
			}
		}
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
		
		@Test
		public void testGetAutoGenerated() {
			String filePath = "config.properties";
			String dbName = "mediabase";
			Connection connection = MediabaseAPI.dbConnection(filePath, dbName);
			
			Assert.assertNotNull(connection);
			List<String> expected = new ArrayList<>();
			expected.add("ID");
			
			List<String> actual = MediabaseAPI.getAutoGenerated(connection, dbName, "DB2INFO5", "BW_AUTHOR");
			Assert.assertFalse(actual.isEmpty());
			Assert.assertEquals(expected.size(), actual.size());
			for (int i = 0; i < expected.size(); i++) {
				Assert.assertEquals(expected.get(i), actual.get(i));
			}
			
			try {
				connection.close();
			} catch (SQLException e) {
				System.out.println("Exception");
			}
		}
		
		private String getJUNIT() {
			InputStream input;
			try {
				input = new FileInputStream("config.properties");
				Properties prop = new Properties();
				prop.load(input);
				String junit = prop.getProperty("junit");
				input.close();
				return junit;
			} catch (IOException exc) {
				return null;
			}
		}

}
