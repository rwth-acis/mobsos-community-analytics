package de.mediabaseapi;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.DataFetcherResult;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLSchema;
import graphql.schema.SelectedField;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.SimpleGraphQLServlet;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import graphql.schema.idl.RuntimeWiring.Builder;


@Path("/graphqlrest")
@ServicePath("/graphqlrest")
// extends RESTService
public class GraphQLREST {

	private String restAPI = "http://localhost:8080/MediabaseRESTAPI/rest/mediabase/";
	private String propertyFile = "src/main/resources/config.properties";
	private String schemaFile = "src/main/resources/schema.graphqls";
	
	@Context ServletContext context;

	@Path("/graphql")
	@GET
	public Response queryExecute(@QueryParam("input") String input) {
		
		if (input == null) {
			return Response.status(460).entity("No graphQL call present in request").build();
		}
		GraphQL graphQL = (GraphQL) context.getAttribute("graphqlBuild");
		ExecutionResult executionResult = graphQL.execute(input);
		return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(executionResult.getData().toString()).build();
	}
	
	public GraphQLSchema buildSchema() {
		Connection con = null;
	    
	    // load JDBC-driver
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
			System.err.println("Could not load DB2Driver:" + exc.toString());
			return null;
		}
		try {
			InputStream input = new FileInputStream(propertyFile);
			Properties prop = new Properties();
			prop.load(input);
			    con = DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
		        //GraphQLSchema graphQLSchema = builderRuntimeWiring.generateSchema(con);
			    // build GraphQL API form schema
			   System.out.println("testing");
		       GraphQLSchema graphQLSchema = generateSchema(con, "DB2INFO5");
		       return graphQLSchema;
		} catch (SQLException exc) {
			    System.err.println("getConnection failed: " + exc.toString());
			    return null;
			} catch (IOException exc) {
					System.err.println("Input failed: " + exc.toString());
					return null;
				}
	}
	
	/**
	 * Given a connection to a DB2 database and a schema of said database, returns an executable GraphQL schema
	 * @param 	con contains connection to Mediabase
	 * @param 	dbSchema contains the name of the selected schema of the database
	 * @return 	executable GraphQL schema with complete runtime wiring
	 */
	private GraphQLSchema generateSchema(Connection con, String dbSchema) {
		
		try {
		    // set the actual schema
		    Statement stmt = con.createStatement();
		    String query = "SET CURRENT SCHEMA '" + dbSchema + "'";
		    stmt.execute(query);
				
		    // construct SQL-Query
		    stmt = con.createStatement();
			// get all table names from selected schema
		    query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE type = 'T' AND CREATOR like '" +  dbSchema + "'";

		    // execute the query
		    ResultSet rs = stmt.executeQuery(query);

			List<String> tableNames = new ArrayList<String>();
		    while (rs.next()) {
				tableNames.add(rs.getString(1));
		    }
			
			String type = "";
			String colname;
			String schema = "";
			List<String> primaryKeys = new ArrayList<String>();
			List<String> foreignKeys = new ArrayList<String>();
			int primaryKeyCount = 0;
			int foreignKeyCount = 0;
			String querySchema = "schema {" + "\r\n" + "query: Query" + "\r\n" + "}" + "\r\n" + "type Query { " + "\r\n";
			RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();
			
			
			for (String name: tableNames) {

				stmt = con.createStatement();
				// get table name, column name, type and nullability for each column in each table
				query = "SELECT TABNAME,COLNAME,TYPENAME,NULLS from SYSCAT.COLUMNS where TABNAME='" + name + "'";
				rs = stmt.executeQuery(query);
				
				primaryKeys.clear();
				foreignKeys.clear();
				foreignKeyCount = 0;
				primaryKeyCount = 0;
				primaryKeys = getPrimaryKeys(name, dbSchema, con);
				foreignKeys = getForeignTables(name, dbSchema, con);
				
				// check if resultset is not empty
				if (rs.next()) {
					
					// build query type definition for each table, such that it returns array of table types
					// e.g. query {tableName: [TABLENAME]}
					querySchema = querySchema + name.toLowerCase();
					if (!primaryKeys.isEmpty()) {
						for (String primaryKey: primaryKeys) {
							if (primaryKeyCount == 0) {
								querySchema = querySchema + "(" + primaryKey.toLowerCase() + ": ID";
							} else {
								querySchema = querySchema + ", " + primaryKey.toLowerCase() + ": ID";
							}
							primaryKeyCount++;
						}
						querySchema = querySchema + "): [" + name + "] " + "\r\n";
					} else {
						querySchema = querySchema + ": [" + name + "] " + "\r\n";
					}
					
					// transform db2 entries to GraphQL object types and build runtime wiring
					// each table in database becomes GraphQL object type with column names as fields
					schema = schema + " type " + name + " { " + "\r\n";
					do {
						colname = rs.getString(2);
						type = rs.getString(3);
						final String finalname = name;
						final String finalcolname = colname;

						// build runtime wiring for GraphQL object types and set data fetchers
						runtimeWiring = runtimeWiring.type(name, typeWiring -> typeWiring
								.dataFetcher(finalcolname.toLowerCase(), 
								createTypeDataFetcher(finalname, finalcolname, dbSchema)));
						
						// set type of fields, transforming types of DB2 to GraphQL types
						if (primaryKeys.contains(colname)) {
							schema = schema + " " + colname.toLowerCase() +  ": ID";
						} else {
							switch (type) {
							case "INTEGER":
								schema = schema + " " + colname.toLowerCase() + ": Int";
								break;
							case "SMALLINT":
								schema = schema + " " + colname.toLowerCase() + ": Int";
								break;
							case "BIGINT":
								schema = schema + " " + colname.toLowerCase() + ": BigInteger";
								break;
							case "DECIMAL":
								schema = schema + " " + colname.toLowerCase() + ": Float";
								break;
							case "REAL":
								schema = schema + " " + colname.toLowerCase() + ": Float";
								break;
							case "DECFLOAT":
								schema = schema + " " + colname.toLowerCase() + ": Float";
								break;
							default:
								schema = schema + " " + colname.toLowerCase() + ": String";
							}
						}
						// check if column is nullable
						if (rs.getString(4).equals("N")){
							schema = schema + "!" + "\r\n";
						}
						else {
							schema = schema + "\r\n";
						}

					} while (rs.next());
					schema = schema + "} " + "\r\n";
				}
			}
			
			querySchema = querySchema + "} " + "\r\n";
			//querySchema = "schema { query: Query} type Query { bw_entries(id: ID): [BW_ENTRIES] } ";
			schema = querySchema + schema;
		    // close resultset
		    rs.close();
		    
		    // write schema to file if it is not already the case
		    BufferedReader br = new BufferedReader(new FileReader(schemaFile));
		    if (br.readLine() == null) {
		    	FileOutputStream out = new FileOutputStream(schemaFile);
			    DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(out));
			    outStream.writeUTF(schema);
			    outStream.close();
		    }
		    br.close();
		    
		    // build runtime wiring for the query types
		    for (String name: tableNames) {
		    	runtimeWiring = runtimeWiring.type("Query",
		    			typeWiring -> typeWiring.dataFetcher(name.toLowerCase(), createQueryDataFetcher(name, dbSchema)));
		    }
		    // close SQL-query
		    stmt.close();
		    
		    // use schema and runtime wiring to create executable GraphQL schema
		    SchemaParser schemaParser = new SchemaParser();
	        SchemaGenerator schemaGenerator = new SchemaGenerator();
	        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
	        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring.build());
	        return graphQLSchema;
			
		} catch (SQLException exc) {
		    System.out.println("JDBC/SQL error: " + exc.toString());
		    return null;
		} catch (IOException exc) {
			System.out.println("IOException: " + exc.toString());
			return null;
		}
	}
	
	/**
	 * builds new data fetcher for query types
	 * @param 	tableName contains name of the GraphQL query type
	 * @param 	schema contains name of schema of the database
	 * @param	con contains connection to DB2 database
	 * @return	Data fetcher for query type which returns list GraphQL object types as HashMaps, field names as String
	 * 			and their values of Object
	 */
	private DataFetcher<List<Map<String, Object>>> createQueryDataFetcher(String tableName, String schema) {
		
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
				String urlString = restAPI + "data/";
				urlString = urlString + schema + "/" + tableName;
				
				try {
					String parameters = "?";				
					// check if there are any parameters
					if (environment.getArguments() != null && !environment.getArguments().isEmpty()) {
						// transform parameters into SQL conditions for the query
						List<String> keys = new ArrayList<String>(environment.getArguments().keySet());
						for (int i = 0; i < keys.size(); i++) {
							//parameter.add(environment.getArgument(keys.get(i)));
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
					
					while ((inputLine = in.readLine()) != null) {
						System.out.println(inputLine);
						JSONArray jsonArray = new JSONArray(inputLine);
						for (int i = 0; i < jsonArray.length(); i++) {
							objectList.add(toMap((JSONObject) jsonArray.get(i)));							
						}
						
					}
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
	 * builds new data fetcher for object types
	 * @param 	tableName contains name of the GraphQL object type
	 * @param 	schema contains name of schema of the database
	 * @param	con contains connection to DB2 database
	 * @return	Data fetcher for object type which returns GraphQL object type as HashMap, field names as String
	 * 			and their values of Object
	 */
	private DataFetcher<Object> createTypeDataFetcher(String tableName, String colname, String schema) {
		
		return new DataFetcher<Object> () {
			@Override
			public Object get(DataFetchingEnvironment environment) {
					Map<String, Object> output = environment.getSource();
					return DataFetcherResult.newResult().data(output.get(colname.toLowerCase())).build();
			}
		};	
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
}
