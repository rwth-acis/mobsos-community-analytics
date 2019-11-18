package de.mediabaseapi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import graphql.GraphQL;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLSchema;
import graphql.schema.SelectedField;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;

@WebListener
public class Initialization implements ServletContextListener{
	
	private String restAPI = "http://localhost:8080/MediabaseRESTAPI/rest/mediabase/";
	private String propertyFile = "src/main/resources/config.properties";
	private String schemaFile = "src/main/resources/schema.graphqls";
	
	private String querySchemaFile = "src/main/resources/querySchema.graphqls";
	private String mutationSchemaFile = "src/main/resources/mutationSchema.graphqls";
	private String typeSchemaFile = "src/main/resources/typeSchema.graphqls";
	
	public void startServer() throws Exception {

	}
	
	public void shutDownServer() throws Exception {

	}
	
	/**
	 * Executes when server is shut down
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		System.out.println("ServletContextListener destroyed");
	}

	/**
	 * Executes when server is started
	 * Clears schema files, builds initial schema and initializes variables used by services
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			// clear schema files
			FileChannel.open(Paths.get(querySchemaFile), StandardOpenOption.WRITE).truncate(0).close();
			FileChannel.open(Paths.get(mutationSchemaFile), StandardOpenOption.WRITE).truncate(0).close();
			FileChannel.open(Paths.get(typeSchemaFile), StandardOpenOption.WRITE).truncate(0).close();
		} catch (IOException exc) {
			System.out.println("IOException: " + exc.toString());
		}
		
		ServletContext sc = event.getServletContext();
		// build initial schema and add mediabase and las2peer database from properties file to it
		sc.setAttribute("AddedDatabase", false);
		sc.setAttribute("RuntimeWiring", initialRuntimeWiring());
		String querySchema = initialQuerySchema();
		String mutationSchema = initialMutationSchema();
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(querySchemaFile, true));
		    writer.write(querySchema.toString());
		    writer.close();
		    
		    writer = new BufferedWriter(new FileWriter(mutationSchemaFile, true));
		    writer.write(mutationSchema.toString());
		    writer.close();
			
			StringBuilder schema = new StringBuilder();
			schema.append(querySchema);
			schema.append(mutationSchema);
			
//			writer = new BufferedWriter(new FileWriter(schemaFile, true));
//		    writer.write(schema.toString());
//		    writer.close();
		    
		    InputStream input = new FileInputStream(propertyFile);
		    Properties prop = new Properties();
			prop.load(input);
			String mediabaseSchema = prop.getProperty("db.dbSchema_mediabase");
			String las2peerSchema = prop.getProperty("db.dbSchema_las2peer");
			input.close();
			
			RuntimeWiring.Builder runtimeWiring = (RuntimeWiring.Builder) sc.getAttribute("RuntimeWiring");
			System.out.println("Building runtime wiring.");
			runtimeWiring = GraphQLREST.updateRuntimeWiring("mediabase", mediabaseSchema, runtimeWiring);
			System.out.println("Runtime Wiring Mediabase complete.");
			System.out.println("las2peer Schema:" + las2peerSchema);
			runtimeWiring = GraphQLREST.updateRuntimeWiring("las2peer", las2peerSchema, runtimeWiring);
			sc.setAttribute("RuntimeWiring", runtimeWiring);
		    
			System.out.println("Building query schema.");
		    GraphQLREST.updateQuerySchema("mediabase", mediabaseSchema);
		    GraphQLREST.updateQuerySchema("las2peer", las2peerSchema);
		    
		    System.out.println("Building mutation schema.");
		    GraphQLREST.updateMutationSchema("mediabase", mediabaseSchema);
		    GraphQLREST.updateMutationSchema("las2peer", las2peerSchema);
		    
		    System.out.println("Building type schema.");
		    GraphQLREST.updateTypeSchema("mediabase", mediabaseSchema);
		    GraphQLREST.updateTypeSchema("las2peer", las2peerSchema);
		    
		    
		} catch (IOException exc) {
			System.out.println("IOException: " + exc.toString());
		}
		System.out.println("ServletContextListener started");	
	}
	
	public String initialSchema() {
		return "schema {" + "\r\n" + "query: Query" + "\r\n"
				+ "mutation: Mutation" + "\r\n}"
				+ "type Query { " + "\r\n}"
				+ "type Mutation { " + "\r\n"
				+ "addDatabase(name: String!, url: String!, user: String!, password:String!, dbType: String!): String \r\n"
				+ "deleteDatabase(name: String!): String}" + "\r\n";
	}
	
	public String initialQuerySchema() {
		return "schema {" + "\r\n" + "query: Query" + "\r\n"
				+ "mutation: Mutation" + "\r\n}"
				+ "type Query { customQuery(name: String!, dbSchema: String!, query: String!): String \r\n}";
	}
	
	public String initialMutationSchema() {
		return "type Mutation { " + "\r\n"
				+ "addDatabase(name: String!, url: String!, dbSchema: String!, user: String!,"
				+ " password:String!, dbType: String!): String \r\n"
				+ "deleteDatabase(name: String!): String \r\n}";
	}
	
	public RuntimeWiring.Builder initialRuntimeWiring() {
		RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();
		runtimeWiring = runtimeWiring.type("Query",
				typeWiring -> typeWiring.dataFetcher("customQuery", createCustomDataFetcher()));
		runtimeWiring = runtimeWiring.type("Mutation",
	    		typeWiring -> typeWiring.dataFetcher("addDatabase", createAddDBDataFetcher()));
		runtimeWiring = runtimeWiring.type("Mutation",
				typeWiring -> typeWiring.dataFetcher("deleteDatabase", createDeleteDBDataFetcher()));
		return runtimeWiring;
	}
	
	private DataFetcher<String> createCustomDataFetcher () {
		return new DataFetcher<String>() {
			@Override
			public String get(DataFetchingEnvironment environment) {
				String name = environment.getArgument("name");
				String dbSchema = environment.getArgument("dbSchema");
				String query = environment.getArgument("query");
				String modQuery = query.replaceAll(" ", "%20");
				String urlString = restAPI + "data/query/" + name + "/" + dbSchema + "?query=" + modQuery;
				System.out.println("In data fetcher, URL: " + urlString);
				String responseData = "";
				
				try {
					URL url = new URL(urlString);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setDoOutput(true);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
							responseData = responseData + inputLine;
						}
					in.close();
					
					return responseData;
				} catch (JSONException exc) {
					System.out.println("JSONException: " + exc.toString());
					return null;
				}
				catch (IOException exc) {
					System.out.println("IOException: " + exc.toString());
					return null;
				}
			}
		};
	}
	private DataFetcher<String> createAddDBDataFetcher() {
		return new DataFetcher<String>() {
			@Override
			public String get(DataFetchingEnvironment environment) {
				String name = environment.getArgument("name");
				String dburl = environment.getArgument("url");
				String dbSchema = environment.getArgument("dbSchema");
				String user = environment.getArgument("user");
				String password = environment.getArgument("password");
				String type = environment.getArgument("type");
				String urlString = restAPI + "database/" + name;
				String data = "{" + "\"url\":\"" + dburl + "\", \"dbSchema\":\"" + dbSchema +
						"\", \"user\":\"" + user + "\", \"password\":\"" + password + "\", \"dbType\":\"" + type + "\"}";
				String responseData = "";
				
				try {
					URL url = new URL(urlString);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type", "application/json");
					con.setDoOutput(true);
		            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		            wr.writeBytes(data);
		            wr.flush();
		            wr.close();
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
							responseData = responseData + inputLine;
						}
					in.close();
					
					return responseData;
				} catch (JSONException exc) {
					System.out.println("JSONException: " + exc.toString());
					return null;
				}
				catch (IOException exc) {
					System.out.println("IOException: " + exc.toString());
					return null;
				}
			}
		};
	}
	
	private DataFetcher<String> createDeleteDBDataFetcher() {
		return new DataFetcher<String>() {
			@Override
			public String get(DataFetchingEnvironment environment) {
				String name = environment.getArgument("name");
				String urlString = restAPI + "database/list/" + name;
				String responseData = "";
				
				try {
					URL url = new URL(urlString);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type", "application/json");
					con.setDoOutput(true);
		            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		            wr.close();
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
							responseData = responseData + inputLine;
						}
					in.close();
					return responseData;
				} catch (JSONException exc) {
					System.out.println("JSONException: " + exc.toString());
					return null;
				}
				catch (IOException exc) {
					System.out.println("IOException: " + exc.toString());
					return null;
				}
			}
		};
	}
	
	
}
