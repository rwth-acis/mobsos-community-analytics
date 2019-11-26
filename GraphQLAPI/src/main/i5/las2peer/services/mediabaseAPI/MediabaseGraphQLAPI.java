package i5.las2peer.services.mediabaseAPI;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;


import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
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
import i5.las2peer.api.ServiceException;
import i5.las2peer.api.Context;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

@Api
@SwaggerDefinition(
		info = @Info(
				title = "MediabaseGraphQLAPI",
				version = "1.0.0",
				description = "A GraphQL API wrapped around a RESTful API for databases.",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "John Doe",
						url = "provider.com",
						email = "john.doe@provider.com"),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com")))

@ServicePath("/graphql")
public class MediabaseGraphQLAPI extends RESTService{
	
	
	// GraphQL configuration
	private RuntimeWiring.Builder runtimeWiring;
	private String restAPIURL;
	
	/**
	 * Initialization of GraphQL API at start of server
	 * @throws ServiceException las2peer service error
	 */
	@Override
	public void onStart() throws ServiceException {
		
		try {
			// clear schema files
			FileChannel.open(Paths.get(querySchemaFile), StandardOpenOption.WRITE).truncate(0).close();
			FileChannel.open(Paths.get(mutationSchemaFile), StandardOpenOption.WRITE).truncate(0).close();
			FileChannel.open(Paths.get(typeSchemaFile), StandardOpenOption.WRITE).truncate(0).close();
			
			//get RESTAPI connection
//			InputStream initInput = new FileInputStream("config.properties");
//		    Properties prop = new Properties();
//			prop.load(initInput);
//			String url = prop.getProperty("restAPIURL");
//			if (url.equals("build")) {
//				((MediabaseGraphQLAPI)Context.get().getService()).setRestAPIURL("http://localhost:8000/rest/");
//			} else {
//				((MediabaseGraphQLAPI)Context.get().getService()).setRestAPIURL(url);
//			}
//			initInput.close();
		} catch (IOException exc) {
			System.out.println("IOException: " + exc.toString());
		}
		
		// build initial schema and add mediabase and las2peer database from properties file to it
		//sc.setAttribute("RuntimeWiring", initialRuntimeWiring());
		runtimeWiring = initialRuntimeWiring();
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
			
			System.out.println("Building runtime wiring.");
			runtimeWiring = updateRuntimeWiring("mediabase", mediabaseSchema, runtimeWiring);
			System.out.println("Runtime Wiring Mediabase complete.");
			System.out.println("las2peer Schema:" + las2peerSchema);
			runtimeWiring = updateRuntimeWiring("las2peer", las2peerSchema, runtimeWiring);
		    
			System.out.println("Building query schema.");
		    updateQuerySchema("mediabase", mediabaseSchema);
		    updateQuerySchema("las2peer", las2peerSchema);
		    
		    System.out.println("Building mutation schema.");
		    updateMutationSchema("mediabase", mediabaseSchema);
		    updateMutationSchema("las2peer", las2peerSchema);
		    
		    System.out.println("Building type schema.");
		    updateTypeSchema("mediabase", mediabaseSchema);
		    updateTypeSchema("las2peer", las2peerSchema);		    
		    
		} catch (IOException exc) {
			System.out.println("IOException: " + exc.toString());
		}
	}
	
	public RuntimeWiring.Builder getRuntimeWiring() {
		return this.runtimeWiring;
	}
	
	public void setRuntimeWiring(RuntimeWiring.Builder runtimeWiring) {
		this.runtimeWiring = runtimeWiring;		
	}
	
	public String getRestAPIURL() {
		return this.restAPIURL;
	}
	
	public void setRestAPIURL(String restAPI) {
		this.restAPIURL = restAPI;
	}
	
	//private static String restAPI = "http://localhost:8000/rest/";
	//private String restAPITest = "http://localhost:8000/rest/";
	//private static String restAPI = "http://137.226.58.243:8000/rest/";
	private static String propertyFile = "config.properties";
	private static String schemaFile = "schemas/schema.graphqls";
	
	private static String querySchemaFile = "schemas/querySchema.graphqls";
	private static String mutationSchemaFile = "schemas/mutationSchema.graphqls";
	private static String typeSchemaFile = "schemas/typeSchema.graphqls";
	
	@Path("/graphql")
	@GET
	@ApiOperation(value = "Processes GraphQL request.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Executed request successfully."),
							@ApiResponse(code = 400, message = "GraphQL call is not in correct syntax."),
			 				@ApiResponse(code = 415, message = "Request is missing GraphQL call."),
			 				@ApiResponse(code = 512, message = "Response is not in correct format."),
			 				@ApiResponse(code = 513, message = "Internal GraphQL server error."),
			 				@ApiResponse(code = 514, message = "Schemafile error.")})
	public Response queryExecute(@QueryParam("input") String input) {
		
		try {
			if (input == null) {
				return Response.status(415).entity("No graphQL call present in request.").build();
			}
			SchemaParser schemaParser = new SchemaParser();
	        SchemaGenerator schemaGenerator = new SchemaGenerator();
		    
		    BufferedReader reader = new BufferedReader(new FileReader(querySchemaFile));
			StringBuilder schemaBuilder = new StringBuilder();
			// for reading one line
			String line = null;
			// keep reading till readLine returns null
			while ((line = reader.readLine()) != null) {
			    // keep appending last line read to buffer
			    schemaBuilder.append(line + "\r\n");
			}
		    reader.close();
		    reader = new BufferedReader(new FileReader(mutationSchemaFile));
		    line = null;
			// keep reading till readLine returns null
			while ((line = reader.readLine()) != null) {
			    // keep appending last line read to buffer
			    schemaBuilder.append(line + "\r\n");
			}
		    reader.close();
		    
		    reader = new BufferedReader(new FileReader(typeSchemaFile));
		    line = null;
			// keep reading till readLine returns null
			while ((line = reader.readLine()) != null) {
			    // keep appending last line read to buffer
			    schemaBuilder.append(line + "\r\n");
			}
		    reader.close();
		    
		    String schema = schemaBuilder.toString();
		    System.out.println("GraphQL Schema built.");

	        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
	        GraphQLSchema graphQLSchema = schemaGenerator
	        		.makeExecutableSchema(typeDefinitionRegistry,
	        				((MediabaseGraphQLAPI)(Context.get().getService())).getRuntimeWiring().build());
			GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
			System.out.println("GraphQL API built.");
			
			ExecutionResult executionResult = graphQL.execute(input);
			List<GraphQLError> errors = executionResult.getErrors();
			
			if (input.contains("addDatabase")) {
				String name = getInputProperty(input, "name");
				String dbSchema = getInputProperty(input, "dbSchema");
				updateQuerySchema(name, dbSchema);
				updateMutationSchema(name, dbSchema);
				updateTypeSchema(name, dbSchema);

				RuntimeWiring.Builder test = ((MediabaseGraphQLAPI)(Context.get().getService())).getRuntimeWiring();
				test = updateRuntimeWiring(name, dbSchema, test);
				((MediabaseGraphQLAPI)(Context.get().getService())).setRuntimeWiring(test);
			}
			System.out.println(errors.toString());
			System.out.println("Input: " + input);
			if (errors.isEmpty()) {
				Object result = executionResult.getData();
				System.out.println("Data:" + result.toString());
				if (result instanceof LinkedHashMap) {
					JSONObject json = new JSONObject((Map<?, ?>)executionResult.getData());
					return Response.status(200).header("Access-Control-Allow-Origin", "*")
							.entity(json.toString()).build();
				}
				System.out.println("Execution Result: " + executionResult.getData().toString());
				System.out.println("Execution Result Datatype: "
				+ executionResult.getData().getClass().toString());
				return Response.status(512).header("Access-Control-Allow-Origin", "*")
						.entity("Response is not in correct format.").build();
			} else {
				for (GraphQLError error: errors) {
					if (error.getErrorType().toString().equals("ValidationError")) {
						return Response.status(400).entity("GraphQL Input not correct.").build();
					}
				}
				return Response.status(513).header("Access-Control-Allow-Origin", "*")
						.entity("Internal GraphQL server error.").build();
			}
		} catch (IOException exc) {
			System.out.println("IOException: " + exc.toString());
			return Response.status(514).header("Access-Control-Allow-Origin", "*")
					.entity("Schemafile Error.").build();
		}		
	}
	
	/**
	 * Builds initial schema, which allows for database management
	 * @return	Initial schema
	 */
	public String initialSchema() {
		return "schema {" + "\r\n" + "query: Query" + "\r\n"
				+ "mutation: Mutation" + "\r\n}"
				+ "type Query { " + "\r\n}"
				+ "type Mutation { " + "\r\n"
				+ "addDatabase(name: String!, url: String!, user: String!, password:String!, dbType: String!): String \r\n"
				+ "deleteDatabase(name: String!): String}" + "\r\n";
	}
	
	/**
	 * Builds initial database independent queries
	 * @return initial query schema
	 */
	public String initialQuerySchema() {
		return "schema {" + "\r\n" + "query: Query" + "\r\n"
				+ "mutation: Mutation" + "\r\n}"
				+ "type Query { customQuery(name: String!, dbSchema: String!, query: String!): String \r\n}";
	}
	
	/**
	 * Builds initial mutations for database management
	 * @return	initial mutation schema
	 */
	public String initialMutationSchema() {
		return "type Mutation { " + "\r\n"
				+ "addDatabase(name: String!, url: String!, dbSchema: String!, user: String!,"
				+ " password:String!, dbType: String!): String \r\n"
				+ "deleteDatabase(name: String!): String \r\n}";
	}
	
	/**
	 * Initializes runtime wiring builder for initial schema
	 * @return	runtime builder for initial schema
	 */
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
	
	/**
	 * sets up DataFetcher for unique SQL queries, used in definition of runtime wiring
	 * @return DataFetcher for unique queries
	 */
	private DataFetcher<String> createCustomDataFetcher () {
		return new DataFetcher<String>() {
			@Override
			public String get(DataFetchingEnvironment environment) {
				String name = environment.getArgument("name");
				String dbSchema = environment.getArgument("dbSchema");
				String query = environment.getArgument("query");
				String modQuery = query.replaceAll(" ", "%20");
				String urlString = retrieveRESTURL() + "data/query/" + name + "/" + dbSchema + "?query=" + modQuery;
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
	
	/**
	 * sets up DataFetcher enabling addition of databases, used in definition of runtime wiring
	 * @return DataFetcher for adding databases
	 */
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
				String urlString = retrieveRESTURL() + "database/" + name;
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
	
	/**
	 * sets up DataFetcher enabling deletion of databases, used in definition of runtime wiring
	 * @return DataFetcher for deleting databases
	 */
	private DataFetcher<String> createDeleteDBDataFetcher() {
		return new DataFetcher<String>() {
			@Override
			public String get(DataFetchingEnvironment environment) {
				String name = environment.getArgument("name");
				String urlString = retrieveRESTURL() + "database/list/" + name;
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
	
	/**
	 * Expand query schema to enable GraphQL Requests on specified database
	 * @param name	name of database defined previously
	 * @param databaseSchema schema of database
	 * @throws IOException read/write on schema files
	 */
	public static void updateQuerySchema(String name, String databaseSchema) throws IOException {
		URL url = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema);
		System.out.println("URL: " + url.toString());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setDoOutput(false);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		List<String> output = new ArrayList<>();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			JSONArray jsonArray = new JSONArray(inputLine);
			for (int i = 0; i < jsonArray.length(); i++) {
				output.add(((JSONObject) jsonArray.get(i)).getString("name"));					
			}	
		}
		System.out.println("Output: " + output.toString());
		in.close();
		StringBuilder querySchemaBuilder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(querySchemaFile));
		// for reading one line
		String line = null;
		// keep reading till readLine returns null
		while ((line = reader.readLine()) != null) {
		    // keep appending last line read to buffer
		    querySchemaBuilder.append(line + "\r\n");
		}
	    reader.close();
		
	    // delete last closing bracket
	    // why 3? no idea...
	    querySchemaBuilder.deleteCharAt(querySchemaBuilder.length() - 3);
		querySchemaBuilder.append(name + "_database_metadata(dbName: String, schema: String): [String]" + "\r\n");
		querySchemaBuilder.append(name + "_table_metadata(dbName: String, schema: String, name: String):"
				+ " [" + name + "_TABLE_METADATA]" + "\r\n");

		List<String> primaryKeys = new ArrayList<String>();
		int primaryKeyCount = 0;
		for (String tableName: output) {			
			URL keyUrl = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema + "/"
			+ tableName + "/primaryKeys");
			System.out.println(keyUrl.toString());
			HttpURLConnection keyCon = (HttpURLConnection) keyUrl.openConnection();
			keyCon.setRequestMethod("GET");
			keyCon.setDoOutput(false);
			BufferedReader keyIn = new BufferedReader(new InputStreamReader(keyCon.getInputStream()));
			String keyInputLine;
			
			primaryKeys.clear();
			primaryKeyCount = 0;
			while ((keyInputLine = keyIn.readLine()) != null) {
				JSONArray jsonArray = new JSONArray(keyInputLine);
				for (int i = 0; i < jsonArray.length(); i++) {
					primaryKeys.add(((JSONObject) jsonArray.get(i)).getString("name"));
				}	
			}
			keyIn.close();
			
			// build query type definition for each table, such that it returns array of table types
			// e.g. query {tableName: [TABLENAME]}
			querySchemaBuilder.append(name + "_" + tableName.toLowerCase());
			if (!primaryKeys.isEmpty()) {
				for (String primaryKey: primaryKeys) {
					if (primaryKeyCount == 0) {
						querySchemaBuilder.append("(" + primaryKey.toLowerCase() + ": ID");
					} else {
						querySchemaBuilder.append(", " + primaryKey.toLowerCase() + ": ID");
					}
					primaryKeyCount++;
				}
				querySchemaBuilder.append("): [" + name + "_" + tableName + "] " + "\r\n");
			} else {
				querySchemaBuilder.append(": [" + name + "_" + tableName + "] " + "\r\n");
			}
		}
				
		querySchemaBuilder.append("}" + "\r\n");
		
		FileChannel.open(Paths.get(querySchemaFile), StandardOpenOption.WRITE).truncate(0).close();
	    
	    BufferedWriter writer = new BufferedWriter(new FileWriter(querySchemaFile, true));
	    writer.write(querySchemaBuilder.toString());	     
	    writer.close();

	}
	
	/**
	 * Expand mutation schema to enable GraphQL requests on specified database
	 * @param name	name of database defined previously
	 * @param databaseSchema schema of database
	 * @throws IOException read/write on schema files
	 */
	public static void updateMutationSchema(String name, String databaseSchema) throws IOException {
		
		URL url = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema);
		System.out.println("URL: " + url.toString());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setDoOutput(false);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		List<String> output = new ArrayList<>();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			JSONArray jsonArray = new JSONArray(inputLine);
			for (int i = 0; i < jsonArray.length(); i++) {
				output.add(((JSONObject) jsonArray.get(i)).getString("name"));					
			}	
		}
		System.out.println("Output: " + output.toString());
		in.close();

		StringBuilder mutationSchemaBuilder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(mutationSchemaFile));
		// for reading one line
		String line = null;
		// keep reading till readLine returns null
		while ((line = reader.readLine()) != null) {
		    // keep appending last line read to buffer
			mutationSchemaBuilder.append(line + "\r\n");
		}
		
		reader.close();
		// delete last closing bracket
		mutationSchemaBuilder.deleteCharAt(mutationSchemaBuilder.length() - 3);
	    
		for (String tableName: output) {
			mutationSchemaBuilder.append(name + "_" +tableName.toLowerCase()
			+ "(dbName: String, schema: String, tableName: String, data: String): " 
					+ name + "_" + tableName + "\r\n");
		}
		mutationSchemaBuilder.append(name + "_deleteEntry(dbName: String, schema: String, tableName: String,"
				+ " condition: String): String" + "\r\n" + "}" + "\r\n");
		
		FileChannel.open(Paths.get(mutationSchemaFile), StandardOpenOption.WRITE).truncate(0).close();
	    BufferedWriter writer = new BufferedWriter(new FileWriter(mutationSchemaFile, true));
	    writer.write(mutationSchemaBuilder.toString());	     
	    writer.close();

	}
	
	/**
	 * Expand type schema to enable GraphQL Requests on specified database
	 * @param name	name of database defined previously
	 * @param databaseSchema schema of database
	 * @throws IOException read/write on schema files
	 */
	public static void updateTypeSchema (String name, String databaseSchema) throws IOException {
		URL url = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema);
		System.out.println("URL: " + url.toString());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setDoOutput(false);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		List<String> output = new ArrayList<>();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			JSONArray jsonArray = new JSONArray(inputLine);
			for (int i = 0; i < jsonArray.length(); i++) {
				output.add(((JSONObject) jsonArray.get(i)).getString("name"));					
			}	
		}
		System.out.println("Output: " + output.toString());
		in.close();

		
		StringBuilder typeSchemaBuilder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(typeSchemaFile));
		// for reading one line
		String line = null;
		// keep reading till readLine returns null
		while ((line = reader.readLine()) != null) {
		    // keep appending last line read to buffer
			typeSchemaBuilder.append(line + "\r\n");
		}
		
		reader.close();
		
		List<String> primaryKeys = new ArrayList<String>();
		List<String> foreignKeys = new ArrayList<String>();
		for (String tableName: output) {
			URL tableUrl = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema + "/" + tableName);
			System.out.println(tableUrl.toString());
			HttpURLConnection tableCon = (HttpURLConnection) tableUrl.openConnection();
			tableCon.setRequestMethod("GET");
			tableCon.setDoOutput(false);
			BufferedReader tableIn = new BufferedReader(new InputStreamReader(tableCon.getInputStream()));
			String tableInputLine;
			List<String> tableColName = new ArrayList<>();
			List<String> tableColType = new ArrayList<>();
			List<String> tableColNull = new ArrayList<>();
			
			while ((tableInputLine = tableIn.readLine()) != null) {
				JSONArray jsonArray = new JSONArray(tableInputLine);
				for (int i = 0; i < jsonArray.length(); i++) {
					tableColName.add(((JSONObject) jsonArray.get(i)).getString("colname"));
					tableColType.add(((JSONObject) jsonArray.get(i)).getString("typename"));
					tableColNull.add(((JSONObject) jsonArray.get(i)).getString("nulls"));
				}	
			}
			tableIn.close();
			String colname;
			String coltype;
			primaryKeys.clear();
			foreignKeys.clear();
			
			URL keyUrl = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema + "/"
			+ tableName + "/primaryKeys");
			System.out.println(keyUrl.toString());
			HttpURLConnection keyCon = (HttpURLConnection) keyUrl.openConnection();
			keyCon.setRequestMethod("GET");
			keyCon.setDoOutput(false);
			BufferedReader keyIn = new BufferedReader(new InputStreamReader(keyCon.getInputStream()));
			String keyInputLine;
			
			while ((keyInputLine = keyIn.readLine()) != null) {
				JSONArray jsonArray = new JSONArray(keyInputLine);
				for (int i = 0; i < jsonArray.length(); i++) {
					primaryKeys.add(((JSONObject) jsonArray.get(i)).getString("name"));
				}	
			}
			keyIn.close();
			
				
			// transform db2 entries to GraphQL object types and build runtime wiring
			// each table in database becomes GraphQL object type with column names as fields
			//typeSchemaBuilder.append(" type " + tableName + " { " + "\r\n");
			typeSchemaBuilder.append(" type " + name + "_" + tableName + " { " + "\r\n");
			
			for (int i = 0; i < tableColName.size(); i++) {
				colname = tableColName.get(i);
				colname = colname.replaceAll("\\.", "_");
				colname = colname.replaceAll("\\-", "_");
				if (!Character.isLetter(colname.charAt(0))) {
					colname = "nr_" + colname;
				}
				coltype = tableColType.get(i);
				
				// set type of fields, transforming types of DB2 to GraphQL types
				if (primaryKeys.contains(colname)) {
					typeSchemaBuilder.append(" " + colname.toLowerCase() +  ": ID");
				} else {
					switch (coltype) {
					case "INTEGER":
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": Int");
						break;
					case "SMALLINT":
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": Int");
						break;
					case "BIGINT":
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": BigInteger");
						break;
					case "DECIMAL":
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": Float");
						break;
					case "REAL":
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": Float");
						break;
					case "DECFLOAT":
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": Float");
						break;
					default:
						typeSchemaBuilder.append(" " + colname.toLowerCase() + ": String");
					}
				}
				// check if column can be null and mark accordingly
				if (tableColNull.get(i).equals("N") || tableColNull.get(i).equals("NO")){
					typeSchemaBuilder.append("!" + "\r\n");
				} else {
					typeSchemaBuilder.append("\r\n");
				}
			}
			typeSchemaBuilder.append("} " + "\r\n");
		}
		// add table metadatatype and database metadata
		typeSchemaBuilder.append("type " + name + "_TABLE_METADATA {" + "\r\n" + "schema: String!" + "\r\n" +
				"name: String!" + "\r\n" + "columns: [String]" + "\r\n" + "}");
		
		FileChannel.open(Paths.get(typeSchemaFile), StandardOpenOption.WRITE).truncate(0).close();
	    BufferedWriter writer = new BufferedWriter(new FileWriter(typeSchemaFile, true));
	    writer.append(typeSchemaBuilder.toString());	     
	    writer.close();

	}
	
	/**
	 * Expands runtime wiring builder to process GraphQL request targeting specified database
	 * @param name	name of database
	 * @param databaseSchema	schema of database
	 * @param runtimeWiring	runtime wiring builder which needs to be extended
	 * @return	expanded runtime wiring builder
	 * @throws IOException read error on schema files
	 */
	public static RuntimeWiring.Builder updateRuntimeWiring(String name, String databaseSchema,
			RuntimeWiring.Builder runtimeWiring)
			throws IOException {
		
		RuntimeWiring.Builder newRuntimeWiring = runtimeWiring;
		URL url = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema);
		System.out.println("URL: " + url.toString());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setDoOutput(false);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		List<String> output = new ArrayList<>();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			JSONArray jsonArray = new JSONArray(inputLine);
			for (int i = 0; i < jsonArray.length(); i++) {
				output.add(((JSONObject) jsonArray.get(i)).getString("name"));					
			}	
		}
		in.close();
		// build runtime wiring for the query types of each table
	    for (String tableName: output) {
	    	newRuntimeWiring = newRuntimeWiring.type("Query",
	    			typeWiring -> typeWiring.dataFetcher(name + "_" +tableName.toLowerCase(),
	    					createRESTQueryDataFetcher(name, tableName, databaseSchema)));
	    	newRuntimeWiring = newRuntimeWiring.type("Mutation",
		    		typeWiring -> typeWiring.dataFetcher(name + "_" + tableName.toLowerCase(),
		    				createPutEntryDataFetcher(name, tableName)));
	    	
	    	URL tableUrl = new URL(retrieveRESTURL() + "metadata/" + name + "/" + databaseSchema + "/" + tableName);
			System.out.println(tableUrl.toString());
			HttpURLConnection tableCon = (HttpURLConnection) tableUrl.openConnection();
			tableCon.setRequestMethod("GET");
			tableCon.setDoOutput(false);
			BufferedReader tableIn = new BufferedReader(new InputStreamReader(tableCon.getInputStream()));
			String tableInputLine;
			List<String> tableColName = new ArrayList<>();
			List<String> tableColType = new ArrayList<>();
			List<String> tableColNull = new ArrayList<>();
			
			while ((tableInputLine = tableIn.readLine()) != null) {
				JSONArray jsonArray = new JSONArray(tableInputLine);
				for (int i = 0; i < jsonArray.length(); i++) {
					tableColName.add(((JSONObject) jsonArray.get(i)).getString("colname"));
					tableColType.add(((JSONObject) jsonArray.get(i)).getString("typename"));
					tableColNull.add(((JSONObject) jsonArray.get(i)).getString("nulls"));
				}	
			}
			tableIn.close();
			for (int i = 0; i < tableColName.size(); i++) {
				final String finalname = tableName;
				final String finalcolname = tableColName.get(i);

				// build runtime wiring for GraphQL object types and set data fetchers
				newRuntimeWiring = newRuntimeWiring.type(tableName, typeWiring -> typeWiring
						.dataFetcher(finalcolname.toLowerCase(), 
						createRESTTypeDataFetcher(finalname, finalcolname, databaseSchema)));
			}
	    }
	    
	    // build runtime wiring for metadata on table
	    List<String> metadataFields = new ArrayList<>();
	    metadataFields.add("dbName");
	    metadataFields.add("schema");
	    metadataFields.add("name");
	    metadataFields.add("columns");
	    for (String field: metadataFields) {
	    	newRuntimeWiring = newRuntimeWiring.type(name + "_TABLE_METADATA", typeWiring -> typeWiring
					.dataFetcher(field, 
					createRESTTypeDataFetcher(name + "_TABLE_METADATA", field, databaseSchema)));
	    }
	    
	    newRuntimeWiring = newRuntimeWiring.type("Query", 
	    		typeWiring -> typeWiring.dataFetcher(name + "_database_metadata", createTableNameDataFetcher()));
	    
	    newRuntimeWiring = newRuntimeWiring.type("Query", 
	    		typeWiring -> typeWiring.dataFetcher(name + "_table_metadata", createTableDataFetcher()));
	    	    
	    // build runtime wiring for the mutation types		    
	    newRuntimeWiring = newRuntimeWiring.type("Mutation",
	    		typeWiring -> typeWiring.dataFetcher(name + "_deleteEntry", createDeleteEntryDataFetcher()));
	    
		
		return newRuntimeWiring;
	}
	
	/**
	 * Extract String for a given value from GraphQL input format
	 * @param input	String from which name attribute is extracted
	 * @param name	name of attribute
	 * @return		String value of attribute
	 */
	public String getInputProperty(String input, String name) {
		 int nameIndex = input.indexOf(name);
		 String nameSub = input.substring(nameIndex + 1);
		 int quotesIndex = nameSub.indexOf("\"");
		 String resSub = nameSub.substring(quotesIndex + 1);
		 int subnext = resSub.indexOf("\"");
		 return resSub.substring(0, subnext);
	 }	
	
	/**
	 * sets up datafetcher enabling the creation of database entries via GraphQL
	 * @param dbName	name of database
	 * @param table		name of table to which entry is added
	 * @return	DataFetcher for entry creation
	 */
	private static DataFetcher<Map<String, Object>> createPutEntryDataFetcher(String dbName, String table) {
		return new DataFetcher<Map<String, Object>>() {
			@Override
			public Map<String, Object> get(DataFetchingEnvironment environment) {
				String schema = environment.getArgument("schema");
				String tableName = table;
				String data = environment.getArgument("data");
				String urlString = retrieveRESTURL() + "data/" + dbName + "/" + schema + "/" + tableName;
				String responseData = "";
				
				try {
					URL url = new URL(urlString);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("PUT");
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
					JSONObject json = new JSONObject(data);
					Map<String, Object> map = toMap(json);
						
					in.close();
					System.out.println("Map: " + map.toString());
					return map;
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
	
	/**
	 * sets up datafetcher enabling the deletion of database entries
	 * @return Datafetcher
	 */
	private static DataFetcher<String> createDeleteEntryDataFetcher() {
		return new DataFetcher<String>() {
			@Override
			public String get(DataFetchingEnvironment environment) {
				String dbName = environment.getArgument("dbname");
				String schema = environment.getArgument("schema");
				String tableName = environment.getArgument("tableName");
				String condition = environment.getArgument("condition");
				System.out.println(condition);
				String urlString = retrieveRESTURL() + "data/" + dbName + "/" + schema + "/" + tableName;
				urlString = urlString + "?condition=" + condition;
				String responseData = "";
				
				try {
					URL url = new URL(urlString);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("DELETE");
					con.setRequestProperty("Content-Type", "application/json");
					con.setDoOutput(false);
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
	
	/**
	 * sets up datafetcher for retrieving names of tables in database schema
	 * @return	datafetcher
	 */
	private static DataFetcher<List<String>> createTableNameDataFetcher() {
		return new DataFetcher<List<String>>() {
			@Override
			public List<String> get(DataFetchingEnvironment environment) {
				
				String dbName = environment.getArgument("dbName");
				String dbSchema = environment.getArgument("schema");
				String urlString = retrieveRESTURL() + "metadata/" + dbName + "/" + dbSchema;
				String parameters = "?views=true";
				
				try {
					URL url = new URL(urlString + parameters);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setDoOutput(false);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					List<String> output = new ArrayList<>();
					
					while ((inputLine = in.readLine()) != null) {
						System.out.println(inputLine);
						JSONArray jsonArray = new JSONArray(inputLine);
						for (int i = 0; i < jsonArray.length(); i++) {
							output.add(((JSONObject) jsonArray.get(i)).getString("name"));					
						}	
					}
					in.close();
					return output;
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
	
	/**
	 * sets up datafetcher retrieving information on column in specified table
	 * @return datafetcher
	 */
	private static DataFetcher<List<Map<String, Object>>> createTableDataFetcher() {
		return new DataFetcher<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> get(DataFetchingEnvironment environment) {
				
				String dbName = environment.getArgument("dbName");
				String schema = environment.getArgument("schema");
				String tableName = environment.getArgument("name");
				String urlString = retrieveRESTURL() + "metadata/" + dbName + "/" + schema + "/" + tableName;
				
				try {
					URL url = new URL(urlString);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setDoOutput(false);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();
					Map<String, Object> map = new HashMap<>();
					map.put("name" , tableName);
					map.put("schema", schema);
					List<String> columns = new ArrayList<>();
					
					while ((inputLine = in.readLine()) != null) {
						JSONArray jsonArray = new JSONArray(inputLine);
						for (int i = 0; i < jsonArray.length(); i++) {
							columns.add(((JSONObject) jsonArray.get(i)).getString("colname"));							
						}
					}
					map.put("columns", columns);
					objectList.add(map);
					in.close();
					return objectList;
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
	
	/**
	 * builds new data fetcher for query types
	 * @param 	tableName contains name of the GraphQL query type
	 * @param 	schema contains name of schema of the database
	 * @param	con contains connection to DB2 database
	 * @return	Data fetcher for query type which returns list GraphQL object types as HashMaps, field names as String
	 * 			and their values of Object
	 */
	private static DataFetcher<List<Map<String, Object>>> createRESTQueryDataFetcher(String dbName,
			String tableName, String schema) {
		
		return new DataFetcher<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> get(DataFetchingEnvironment environment) {
				
				// collect subfields from query and transform to SQL columns
				System.out.println("DataFetcher");
				String subfieldSelection = "";
				DataFetchingFieldSelectionSet fields = environment.getSelectionSet();
				List<SelectedField> selectedFields = fields.getFields();
				System.out.println(selectedFields.size() + " size");
				for (int i = 0; i < selectedFields.size(); i++) {
					subfieldSelection = subfieldSelection + selectedFields.get(i).getName().toUpperCase();
					if (i < selectedFields.size() - 1) {
						subfieldSelection = subfieldSelection + ", ";
					}
					
				}
				System.out.println("Subfields: " + subfieldSelection);
				System.out.println(tableName);
				String query = "";
				String urlString = retrieveRESTURL() + "data/";
				urlString = urlString + dbName + "/" + schema + "/" + tableName;
				
				try {
					String parameters = "?";				
					// check if there are any parameters
					if (environment.getArguments() != null && !environment.getArguments().isEmpty()) {
						// transform parameters into SQL conditions for the query
						List<String> keys = new ArrayList<String>(environment.getArguments().keySet());
						for (int i = 0; i < keys.size(); i++) {
							if (i < keys.size() - 1) {
								query = query + keys.get(i).toUpperCase() + "=" + environment.getArgument(keys.get(i)) + " AND ";
							} else {
								query = query + keys.get(i).toUpperCase() + "=" + environment.getArgument(keys.get(i));
							}
						}
						parameters = parameters + "condition=" + query;
					}
					URL url = new URL(urlString + parameters);
					System.out.println(url.toString());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setDoOutput(false);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();
					
					JSONArray jsonArray = new JSONArray();
					while ((inputLine = in.readLine()) != null) {
						System.out.println(inputLine);
						jsonArray = new JSONArray(inputLine);
						System.out.println("jsonArray: " + jsonArray.toString());
						for (int i = 0; i < jsonArray.length(); i++) {
							objectList.add(toMap((JSONObject) jsonArray.get(i)));							
						}
					}
					in.close();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(tableName.toLowerCase(), (Object) jsonArray);
					System.out.println("JSONObject: " + jsonObject.getJSONArray(tableName.toLowerCase()).get(0));
					return objectList;
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
	/**
	 * builds new data fetcher for object types
	 * @param 	tableName contains name of the GraphQL object type
	 * @param 	schema contains name of schema of the database
	 * @param	con contains connection to DB2 database
	 * @return	Data fetcher for object type which returns GraphQL object type as HashMap, field names as String
	 * 			and their values of Object
	 */
	private static DataFetcher<Object> createRESTTypeDataFetcher(String tableName, String colname, String schema) {
		
		return new DataFetcher<Object> () {
			@Override
			public Object get(DataFetchingEnvironment environment) {
				Map<String, Object> output = environment.getSource();
				return DataFetcherResult.newResult().data(output.get(colname.toLowerCase())).build();
			}
		};	
	}
	
	/**
	 * Transforms a LinkedHashMap to a JSONObject
	 * @param map	LinkedHahMap to be transformed to JSON object
	 * @return	JSONObject of LinkedHashMap
	 */
	public JSONObject mapToJSON(LinkedHashMap<Object, Object> map) {
		if (map.isEmpty()) {
			return null;
		}
		JSONObject json = new JSONObject();
		Gson gson = new Gson();
		System.out.println("gson: " + gson.toJson(map, map.getClass()));
		List<Object> keys = new ArrayList<>(map.keySet());
		json.put(keys.get(0).toString(), map.get(keys.get(0)));
		ArrayList<Object> test = (ArrayList<Object>) map.get(keys.get(0));
		for (Object entry: test) {
			System.out.println("Class Test: " + entry.getClass() + " Entry: " + entry.toString());
			if (entry instanceof LinkedHashMap) {
				LinkedHashMap<Object, Object> lhm = (LinkedHashMap<Object, Object>) entry;
				System.out.println("LHM: " + lhm.toString());
				List<Object> secKeys = new ArrayList<>(lhm.keySet());
				for (Object secKey: secKeys) {
					//System.out.println("3: "+ (int)((LinkedHashMap) entry).get("id"));
					System.out.println("2. Class Test: " + lhm.get(secKey).getClass());
					System.out.println("2. Entry: " + lhm.get(secKey).toString());
				}
			}
		}
		System.out.println("Testing: " + map.get(keys.get(0)).getClass());
		System.out.println("Testing JSON: " + new JSONObject(map).toString());	
		System.out.println("Map to JSON: " + json.toString());
		json = new JSONObject(map);
		return json;
	}
	
	/**
	 * Given a connection to a DB2 database, returns list of primary keys of table in the given schema
	 * @param 	tableName contains name of the table of which the primary keys should be returned
	 * @param 	schema contains name of schema of the database
	 * @param 	con contains connection to DB2 database
	 * @return 	list of the primary keys
	 */
	public static List<String> getPrimaryKeys(String tableName, String schema, Connection con) {
		try {		
			Statement stmt = con.createStatement();
			String query = "SELECT NAME " + 
					"FROM SYSIBM.SYSCOLUMNS " + 
					"WHERE TBNAME = '" + tableName + "' " +
					"AND TBCREATOR = '" + schema + "' " +
					"AND KEYSEQ > 0";
			ResultSet keys = stmt.executeQuery(query);
			List<String> primaryKeys = new ArrayList<String>();
			while(keys.next()) {
				primaryKeys.add(keys.getString(1));
			}
			keys.close();
			stmt.close();
			return primaryKeys;
		} catch (SQLException exc) {
		    System.out.println("JDBC/SQL error: " + exc.toString());
		    return null;
		}
	}
	/**
	 * Given a connection to a DB2 database, returns list of foreign keys of table in the given schema
	 * @param 	tableName contains name of the table of which the foreign keys should be returned
	 * @param 	schema contains name of schema of the database
	 * @param 	con contains connection to DB2 database
	 * @return	list of the foreign keys
	 */
	public static List<String> getForeignTables(String tableName, String schema, Connection con) {
		try {		
			Statement stmt = con.createStatement();
			String query = "SELECT ref.tabname as foreign_table, " +
					"ref.reftabname as primary_table, " +
					"ref.constname as fk_constraint_name " +
					"from syscat.references ref where TABNAME='" + tableName + "' " +
					"order by foreign_table, primary_table";
			ResultSet keys = stmt.executeQuery(query);
			List<String> foreignKeys = new ArrayList<String>();
			while(keys.next()) {
				foreignKeys.add(keys.getString(2));
			}
			keys.close();
			stmt.close();
			return foreignKeys;
		} catch (SQLException exc) {
		    System.out.println("JDBC/SQL error: " + exc.toString());
		    return null;
		}
	}

	/**
	 * Transforms JSONObject to a Map
	 * @param object	JSONObject to be transformed
	 * @return	Map of specified JSONObject
	 * @throws JSONException input is not in correct JSON
	 */
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        map.put(key, value);
	    }
	    return map;
	}
	
	/**
	 * Transforms JSONArray to a list of objects
	 * @param array	JSONArray to be transformed
	 * @return List of specified JSONArray
	 * @throws JSONException JSONArray not in correct JSON
	 */
	public static List<Object> toList(JSONArray array) throws JSONException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}
	
	public static String retrieveRESTURL() {
		try {
			InputStream initInput = new FileInputStream("config.properties");
		    Properties prop = new Properties();
			prop.load(initInput);
			String url = prop.getProperty("restAPIURL");
			initInput.close();
			if (url.equals("build")) {
				return "http://localhost:8088/rest/";
			} else {
				return url + "/rest/";
			}
		} catch (IOException exc) {
			System.out.println("IOException" + exc.toString());
			return null;
		}
		
	}
}
