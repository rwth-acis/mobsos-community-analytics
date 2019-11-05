package de.mediabaseapi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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
	 * Clears schema files and initializes variables used by services
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
		sc.setAttribute("AddedDatabase", false);
		sc.setAttribute("RuntimeWiring", RuntimeWiring.newRuntimeWiring());
		System.out.println("ServletContextListener started");	
	}
	

}
