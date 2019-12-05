package i5.las2peer.services.mediabaseAPI;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import i5.las2peer.api.ServiceException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


// TODO Describe your own service
/**
 * las2peer-Template-Service
 * 
 * This is a template for a very basic las2peer service that uses the las2peer WebConnector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in the SwaggerDefinition annotation to suit
 * your project. If you do not intend to provide a Swagger documentation of your service API, the entire Api and
 * SwaggerDefinition annotation should be removed.
 * 
 */
// TODO Adjust the following configuration
@Api
@SwaggerDefinition(
		info = @Info(
				title = "MediabaseAPI",
				version = "1.0.0",
				description = "A RESTful API for databases.",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "John Doe",
						url = "provider.com",
						email = "john.doe@provider.com"),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com")))

// for las2peer testing
@ServicePath("/rest")
public class MediabaseAPI extends RESTService{
	
	 @Override
	 public void onStart() throws ServiceException {
		 System.out.println("Testing onStart function.");
	 }
	 
	 private static String filePath = "config.properties";
	 //private List<String> nameList = new ArrayList<>();
	 
//	 @Path("/database/list")
//	 @GET
//	 public Response getDatabaseNames() {
//		 
//		 if (nameList.isEmpty()) {
//			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
//					 .entity("No databases.").build();
//		 } else {
//			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
//					 .entity(nameList.toString()).build();
//		 }
//	 }
	 
	 @Path("/database/{name}")
	 @DELETE
	 @ApiOperation(value = "Deletes database from API.")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful deletion."),
							 @ApiResponse(code = 500, message = "Error when handling properties file.")})
	 public Response deleteDatabase(@PathParam("name") String name) {
		 
		 try {
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 prop.remove("db.url_" + name);
			 prop.remove("db.user_" + name);
			 prop.remove("db.password_" + name);
			 prop.remove("db.dbSchema_" + name);
			 prop.remove("db.dbType_" + name);
			 OutputStream output = new FileOutputStream(filePath);
			 prop.store(output, null);
			 System.out.println("DELETE");
			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
					 .entity("Deletion successful").build();
		 } catch (IOException exc) {
			 return Response.status(500).header("Access-Control-Allow-Origin", "*").build();
		 }
	 }
	 
	 @POST
	 @Path("/database/{name}")
	 @ApiOperation(value = "Adds database with then can be queried.")
	 @ApiResponses(value = { @ApiResponse(code = 210, message = "Database was already registered."),
			 				 @ApiResponse(code = 201, message = "Database was added successfully."),
			 				 @ApiResponse(code = 422, message = "Properties are not provided in required format"),
			 				 @ApiResponse(code = 500, message = "Error when handling properties file.")})
	 public Response addDatabase(@PathParam("name") String name,
			 @ApiParam(value="Properties of database in JSON format") String properties) {
		 
		 System.out.println("Properties: " + properties);
		 try {
//			 if (nameList.contains(name)) {
//				 return Response.status(406).header("Access-Control-Allow-Origin", "*")
//						 .entity("Name of database is already in use.").build();
//			 }
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 
			 if (prop.getProperty("db.url_" + name) != null && prop.getProperty("db.user_" + name) != null &&
					 prop.getProperty("db.password_" + name) != null && prop.getProperty("db.dbSchema_") != null
					 && prop.getProperty("db.dbType_" + name) != null) {
				 return Response.status(210).header("Access-Control-Allow-Origin", "*")
						 .entity("Database already added").build();
			 } else {
				 JSONObject json = new JSONObject(properties);
				 OutputStream output = new FileOutputStream(filePath);
	             prop.setProperty("db.url_" + name, json.getString("url"));
	             prop.setProperty("db.user_" + name, json.getString("user"));
	             prop.setProperty("db.password_" + name, json.getString("password"));
	             prop.setProperty("db.dbSchema_" + name, json.getString("dbSchema"));
	             prop.setProperty("db.dbType_" + name, json.getString("dbType"));

	            // save properties to project root folder
	            prop.store(output, null);
				return Response.status(201).header("Access-Control-Allow-Origin", "*")
						.entity("Database added.").build();
			 }
		 }
		 catch (JSONException exc) {
			 System.out.println("JSON Exception: " + exc.toString());
			 return Response.status(422).header("Access-Control-Allow-Origin", "*")
					 .entity("Database properties are not provided in correct JSON format").build();
		 } catch (IOException exc) {
			 System.err.println("Input failed: " + exc.toString());
			 return Response.status(500).header("Access-Control-Allow-Origin", "*")
					 .entity("Internal file management error.").build();
		 }
		 
	 }
	 
	 @Path("/metadata/{dbName}/{schema}")
	 @GET
	 @ApiOperation(value = "Returns all table names for a given database and schema.")
	 @ApiResponses(value = { @ApiResponse(code = 406, message = "Provided schema has no tables."),
							 @ApiResponse(code = 200, message = "Successful Request."),
							 @ApiResponse(code = 404, message = "Schema not present in database."),
							 @ApiResponse(code = 500, message = "Error when handling properties file.")})
	 public Response getTableNames(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @DefaultValue("false") @QueryParam("views") Boolean views) {
		 
		 try {
			 Connection connection = dbConnection(filePath, dbName);
		     Statement stmt = connection.createStatement();
		     System.out.println("Here!");
		     String query = "";
		     
		     InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 String dbType = prop.getProperty("db.dbType_" + dbName);
			 System.out.println("Type: " + dbType);
			 input.close();
		     
			 if (dbType.equals("MySQL")) {
				 if (!views) {
					 query = "SELECT TABLE_NAME AS NAME FROM information_schema.tables "
					 		+ "where table_schema not in ('information_schema', 'mysql', 'performance_schema')";
				 } else {
					 query = "SELECT TABLE_NAME AS NAME \n" + 
					 		"FROM information_schema.tables \n" + 
					 		"WHERE (TABLE_TYPE LIKE 'VIEW' OR TABLE_TYPE LIKE 'TABLE') AND "
					 		+ "table_schema not in ('information_schema', 'mysql', 'performance_schema');";
				 }
			 }
			 else if (dbType.equals("DB2")) {
				 if (!views) {
			    	 query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE type = 'T' AND CREATOR like '" +  schema + "'";
			     } else {
			    	 query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE (type = 'T' OR type = 'V') AND CREATOR like '"
			    			 +  schema + "'";
			     } 
			 } else {
				 return Response.status(500).entity("Database type is not supported.").build();
			 }
		     
		     ResultSet rs = stmt.executeQuery(query);
		     JSONArray json = resultSetToJSON(rs, false);
		     System.out.println(json.toString());
		     rs.close();
		     stmt.close();
		     connection.close();
		     if (json != null && json.toString().equals("[]")) {
					return Response.status(406).entity("Schema has no tables.").build();
				}
			 return Response.status(200).entity(json.toString()).build();
		 } catch (SQLException exc) {
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(404).entity("Schema is not present in given database.").build();
		 } catch (IOException exc) {
			 System.out.println("IOException: " + exc.toString());
			 return Response.status(500).entity("Error when handling properties file.").build();
		 }
		 
	 }
	 
	 @ApiOperation(value = "Retrieves column information from a given table")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful request."),
			 				 @ApiResponse(code = 406, message = "Structure missing from database."),
			 				 @ApiResponse(code = 422, message = "SQL error."),
			 				 @ApiResponse(code = 500, message = "Error when handling properties file.")})
	 @Path("/metadata/{dbName}/{schema}/{tableName}")
	 @GET
	 public Response getColumnNames(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName) {
		 
			try {
				Connection connection = dbConnection(filePath, dbName);
			    Statement stmt = connection.createStatement();
			    //stmt.execute("SET CURRENT SCHEMA " + schema);
			    
			    InputStream input = new FileInputStream(filePath);
				Properties prop = new Properties();
				prop.load(input);
				String dbType = prop.getProperty("db.dbType_" + dbName);
				input.close();
				String query = "";
			    
			    stmt = connection.createStatement();
			    if (dbType.equals("MySQL")) {
			    	query = "SELECT COLUMN_NAME AS COLNAME, DATA_TYPE AS TYPENAME, IS_NULLABLE AS NULLS"
			    			+ " from INFORMATION_SCHEMA.COLUMNS "
			    			+ " where TABLE_NAME like '" + tableName + "' AND TABLE_SCHEMA like '" + schema +  "';";
			    } else if (dbType.equals("DB2")) {
				    query = "SELECT COLNAME,TYPENAME,NULLS from SYSCAT.COLUMNS where TABNAME='" 
				    + tableName + "' AND TABSCHEMA ='" + schema + "'";
			    } else {
			    	return Response.status(500).entity("Database type is not supported.").build();
			    }

			    ResultSet rs = stmt.executeQuery(query);
			    
				JSONArray json = resultSetToJSON(rs, false);
				if (json != null && json.toString().equals("[]")) {
					return Response.status(406).entity("There are no columns in this table.").build();
				}
				
			    rs.close();
			    stmt.close();
			    connection.close();
			    
			    return Response.status(200).entity(json.toString()).build();
			} catch (SQLException exc) {
				if (exc.getMessage().contains("SQLCODE=-206, SQLSTATE=42703")) {
					return Response.status(406).entity("Table is not present in given schema.").build();
				}
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(422).entity("SQL error.").build();
			} catch (IOException exc) {
				System.out.println("IOException: " + exc.toString());
				return Response.status(500).entity("Error when handling properties file.").build();
			}
	 }
	 
	 @Path("/metadata/{dbName}/{schema}/{tableName}/primaryKeys")
	 @GET
	 @ApiOperation(value = "Returns primary keys of a table")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Request successful."),
			 				 @ApiResponse(code = 422, message = "SQL error."),
			 				 @ApiResponse(code = 500, message = "Error when handling properties file.")})
	 public Response getPrimaryKeys(@PathParam("dbName") String dbName, @PathParam("schema") String schema, 
			 @PathParam("tableName") String tableName) {
			try {
				Connection connection = dbConnection(filePath, dbName);
				Statement stmt = connection.createStatement();
				
				InputStream input = new FileInputStream(filePath);
				Properties prop = new Properties();
				prop.load(input);
				String dbType = prop.getProperty("db.dbType_" + dbName);
				input.close();
				String query = "";
				
				if (dbType.equals("MySQL")) {
					query = "SELECT COLUMN_NAME AS NAME from INFORMATION_SCHEMA.COLUMNS"
			    			+ " where TABLE_NAME like '" + tableName + "' AND TABLE_SCHEMA like '" + schema +  "'"
	    					+ "AND COLUMN_KEY like 'PRI';";
				} else if (dbType.equals("DB2")) {
					query = "SELECT NAME FROM SYSIBM.SYSCOLUMNS " + 
							"WHERE TBNAME = '" + tableName + "'AND TBCREATOR = '" + schema + "' " +
							"AND KEYSEQ > 0";
				} else {
					return Response.status(500).entity("Database type is not supported.").build();
				}
				 
				ResultSet keys = stmt.executeQuery(query);
				JSONArray json = resultSetToJSON(keys, false);

				keys.close();
				stmt.close();
				connection.close();
				
				return Response.status(200).entity(json.toString()).build();
			} catch (SQLException exc) {
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(422).entity("SQL error.").build();
			} catch (IOException exc) {
				System.out.println("IOException: " + exc.toString());
				return Response.status(500).entity("Error when handling properties file.").build();
			}
	 }
	 
	 @Path("/data/{dbName}/{schema}/{tableName}")
	 @GET
	 @ApiOperation(value = "Retrieves entry from database.")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful request."),
							 @ApiResponse(code = 406, message = "Structure is not present in database."),
							 @ApiResponse(code = 422, message = "SQL error.")})
	 public Response getEntry(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName, @QueryParam("condition") String condition,
			 @QueryParam("colname") String colname) {

			try {
			    // set the actual schema
				Connection connection = dbConnection(filePath, dbName);
			    Statement stmt = connection.createStatement();
			    //stmt.execute("SET CURRENT SCHEMA " + schema);
			     
			    stmt = connection.createStatement();
			    String query = "";
			    if (colname != null && !colname.isEmpty()) {
			    	query = "SELECT " + colname + " FROM " + tableName;
			    } else {
			    	query = "SELECT * FROM " + tableName;
			    }
			    
			    if (condition != null && !condition.isEmpty()) {
			    	query = query + " WHERE " + condition;
			    }

			    ResultSet rs = stmt.executeQuery(query);
				JSONArray json = resultSetToJSON(rs, true);
				if (json != null && json.toString().equals("[]")) {
					return Response.status(406).entity("Entry is empty.").build();
				}
			    rs.close();
			    stmt.close();
			    connection.close();
			    
			    return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(json.toString()).build();
			} catch (SQLException exc) {
				if (exc.getMessage().contains("SQLCODE=-206, SQLSTATE=42703")) {
					return Response.status(406).entity("Table does not exits.").build();
				}
				if (exc.getMessage().contains("SQLCODE=-204, SQLSTATE=42704")) {
					return Response.status(406).entity("Schema does not exist.").build();
				}
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(422).entity("SQL error.").build();
			}
	 }
	 
	 @Path("/pattern/{dbName}/{schema}/media/{mediaName}")
	 @GET
	 public Response pattern(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("mediaName") String mediaName) {
		 try {
			 Connection connection = dbConnection(filePath, dbName);
			 Statement stmt = connection.createStatement();
			 //stmt.execute("SET CURRENT SCHEMA " + schema);

			 String query = "SELECT ";
			 
			 switch (mediaName) {
			 	case "message":
			 		query = query + "LW_ENTRIES_TT" + "FW_RSS_ENTRIES" + "FW-ATOM_ENTRIES";
			 		break;
			 	case "thread":
			 		query = query + "";
			 		break;
			 	case "burst":
			 		query = query + "";
			 		break;
			 	case "converation":
			 		query = query + "";
			 		break;
			 	case "blog_entry":
			 		query = query + "BW_ENTRIES";
			 		break;
			 	case "comment":
			 		query = query + "";
			 		break;
			 	case "web_page":
			 		query = query + "";
			 		break;
			 	case "transaction":
			 		query = query + "";
			 		break;
			 	case "feedback":
			 		query = query + "";
			 		break;
			 	default:
			 		return Response.status(460).build();
			 		
			 }
			 
			 ResultSet rs = stmt.executeQuery(query); 
			 JSONArray json = resultSetToJSON(rs, true);
			 if (json != null && json.toString().equals("[]")) {
				return Response.status(455).build();
			 }
			
			 return Response.status(200).entity(json.toString()).build();
			 			 
			 
		 } catch (SQLException exc) {
			 return Response.status(400).build();
			 
		 }
	 }
	 
	 @Path("/data/{dbName}/{schema}/{tableName}")
	 @PUT
	 @ApiOperation(value = "Add entry to database.")
	 @ApiResponses(value = { @ApiResponse(code = 201, message = "Created successfully."),
							 @ApiResponse(code = 415, message = "Input is not in correct JSON format."),
							 @ApiResponse(code = 422, message = "SQL error.")})
	 public Response putEntry(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName, String data) {
		 
		 try {
			// set the actual schema
			Connection connection = dbConnection(filePath, dbName);
		    Statement stmt = connection.createStatement();
		    //stmt.execute("SET CURRENT SCHEMA " + schema);
			    
		    // construct SQL-Query from path parameters 
		    stmt = connection.createStatement();
		    
		    // extract input values from query parameter
		    JSONObject json = new JSONObject(data);
		    Iterator<String> keys = json.keys();
		    List<String> autoGenerated = getAutoGenerated(connection, dbName, schema, tableName);
		    String columns = "(";
		    String values = "(";
		    int lastElement = json.length() - 1;
		    int count = 0;
		    while (keys.hasNext()) {
		    	String key = keys.next();
		    	if (count == lastElement) {
		    		if (autoGenerated.contains(key)) {
		    			columns = columns + ")";
		    			values = values + ")";
		    		} else {
		    			columns = columns + key + ")";
				    	values = values + "'" + json.get(key) + "'" + ")";
		    		}
		    	} else {
		    		if (!autoGenerated.contains(key)) {
		    			columns = columns + key + ", ";
				    	values = values + "'" + json.get(key) + "'" + ", ";
		    		}
		    	}
		    	count++;
		    }
		    String query = "INSERT INTO " + tableName + " " + columns + " VALUES " + values;
		    System.out.println("PUT Query: " + query);

		    // execute the query
		    stmt.execute(query);
		    stmt.close();
		    connection.close();
		    return Response.status(201).entity("Created.").build();
		 } catch (SQLException exc) {
			 System.out.println("JDBC/SQL error: " + exc.toString());
		     return Response.status(422).entity("SQL error.").build();
		 } catch (JSONException exc) {
			 System.out.println("JSON error: " + exc.toString());
			 return Response.status(415).entity("Data is not in valid JSON format").build();
			 
		 }
	 }
	 
	 @Path("/data/{dbName}/{schema}/{tableName}")
	 @DELETE
	 @ApiOperation(value = "Deletes entry from database.")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Deletion successful."),
							 @ApiResponse(code = 422, message = "SQL error.")})
	 public Response deleteEntry(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName, @QueryParam("condition") String condition) {
		 try {

			Connection connection = dbConnection(filePath, dbName);
		    Statement stmt = connection.createStatement();
		    //stmt.execute("SET CURRENT SCHEMA " + schema);
		    String query = "DELETE FROM " + tableName;
		    if (condition != null && !condition.isEmpty()) {
		    	query = query + " WHERE " + condition;
		    }
		    System.out.println("DELETE query: " + query);
		    stmt.execute(query);
		    stmt.close();
		    connection.close();
			return Response.status(200).build();
		 } catch (SQLException exc) {
			 return Response.status(422).entity("SQL error.").build();
		 }
	 }
	 
	 @Path("/data/query/{dbName}/{schema}")
	 @GET
	 @ApiOperation(value = "Executes given query on database.")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful request."),
							 @ApiResponse(code = 406, message = "Structure is not present in database."),
							 @ApiResponse(code = 422, message = "SQL error.")})
	 public Response sqlQuery(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @QueryParam("query") String query) {
		 try {
			 Connection connection = dbConnection(filePath, dbName);
			 System.out.println("SQL Query: " + query);
			 Statement stmt = connection.createStatement();
			 //stmt.execute("SET CURRENT SCHEMA " + schema);
			 ResultSet rs = stmt.executeQuery(query);
			 JSONArray json = resultSetToJSON(rs, false);
			 if (json != null && json.toString().equals("[]")) {
				return Response.status(406).build();
			 }
			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
					 .entity(json.toString()).build();
		 } catch (SQLException exc) {
			 System.out.println("SQLException: " + exc.toString());
			 return Response.status(422).header("Access-Control-Allow-Origin", "*")
					 .entity("SQL error: " + exc.toString()).build();
		 }
	 }
	 
	 @Path("view/{dbName}/{schema}/{viewName}")
	 @PUT
	 @ApiOperation(value = "Create view in database.")
	 @ApiResponses(value = { @ApiResponse(code = 201, message = "Created successfully."),
							 @ApiResponse(code = 422, message = "SQL error.")})
	 public Response createView(@PathParam("schema") String schema, @PathParam("viewName") String viewName,
			 @PathParam("dbName") String dbName, @ApiParam(value = "Column names in SQL format") String sqlStatement) {
		 try {
			 Connection connection = dbConnection(filePath, dbName);
			 Statement stmt = connection.createStatement();
			 String query = "CREATE VIEW " + sqlStatement;
			 stmt.execute(query);
			 stmt.close();
		     connection.close();
			 return Response.status(201).build(); 
		 } catch (SQLException exc) {
			 return Response.status(422).entity("SQL error.").build();
		 }
	 }
	 
	 @Path("listDatabases")
	 @GET
	 @ApiOperation(value = "Get the names of all added databases")
	 @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful request."),
			 				 @ApiResponse(code = 500, message = "Error while reading properties file.")})
	 public Response getDatabases() {
		 try {
			 List<String> names = new ArrayList<>();
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 Set<Object> keys = prop.keySet();
			 for (Object key: keys) {
				 String stringKey = (String) key;
				 if (stringKey.contains("db.url_")) {
					 names.add(stringKey.substring(stringKey.indexOf('_') + 1));
				 }
			 }
			 input.close();
			 return Response.status(200).entity(names.toString()).build();
		 } catch (IOException exc) {
				System.out.println("IOException: " + exc.toString());
				return Response.status(500).entity("Error when handling properties file.").build();
			}
	 }
	 
	 /**
	  * Gives JSON Object for a given Result Set
	  * @param resultSet	basis for JSON Object
	  * @param limit		if true, only the first 16 entries in resultSet are considered
	  * @return				JSON Object of resultSet
	  */
	 private JSONArray resultSetToJSON(ResultSet resultSet, Boolean limit) {
		 try {
				JSONArray json = new JSONArray();
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int count = 0;
				//while(resultSet.next() && (!limit || count < 16)) {
				while(resultSet.next() && (!limit || count < 16)) {
					int numColumns = rsmd.getColumnCount();
					JSONObject obj = new JSONObject();
				    for (int i = 1; i <= numColumns; i++) {
				    	//String columnName = rsmd.getColumnName(i).toLowerCase();
				    	String columnName = rsmd.getColumnLabel(i).toLowerCase();
				    	obj.put(columnName, resultSet.getObject(columnName));
				    }
				    json.put(obj);
				    count++;
				}
			    return json;
		 }
		 catch (SQLException exc) {
			 return null;
		 }

	 }
	 
	 /**
	  * Establish SQL connection
	  * @param filePath	contains the path to the properties file containing url, user and password
	  * @param name		name of database specified in property file or http request
	  * @return		If successful, a SQL connection, else null 
	  */
	 public static Connection dbConnection(String filePath, String name) {
		 try {
			 Class.forName("com.ibm.db2.jcc.DB2Driver");
		 } catch (ClassNotFoundException exc) {
			 System.err.println("Could not load DB2Driver: " + exc.toString());
		 }
		 try {
			 Class.forName("com.mysql.cj.jdbc.Driver");
		 } catch (ClassNotFoundException exc) {
			 System.err.println("Could not load MySQLDriver: " + exc.toString());
		 }
			 
         
		 Connection connection = null;
		 try {
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 String connectType = prop.getProperty("db.connection_" + name);
	         
	         if (connectType != null && connectType.equals("ssh")) {
//				 String sshuser = prop.getProperty("ssh.user");
//		         String sshhost = prop.getProperty("ssh.host");
//		         String sshpassword = prop.getProperty("ssh.password");
//	        	 JSch jsch = new JSch();
//		         session = jsch.getSession(sshuser, sshhost);
//		         int lport = 4321;
//		         String rhost = "localhost";
//		         int rport = 3306;
//		         session.setPassword(sshpassword);
//		         session.setConfig("StrictHostKeyChecking", "no");
//		         System.out.println("Establishing Connection...");
//		         session.connect();
//		         int assinged_port=session.setPortForwardingL(lport, rhost, rport);
//		         System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
//				 System.out.println("Testing: " + prop.getProperty("db.url_" + name));
//				 String url = prop.getProperty("ssh.dburl");
//				 String user = prop.getProperty("ssh.dbuser");
//				 String password = prop.getProperty("ssh.dbpassword");
//				 Class.forName("com.mysql.cj.jdbc.Driver");
//				 connection = DriverManager.getConnection(url, user, password);
	         } else {
        	 	Class.forName("com.ibm.db2.jcc.DB2Driver");
        	 	connection = DriverManager.getConnection(prop.getProperty("db.url_" + name),
	        			prop.getProperty("db.user_" + name), prop.getProperty("db.password_" + name));
	         }
			 input.close();
			 return connection;
		 } catch (SQLException exc) {
			 System.err.println("Connection failed: " + exc.toString());
			 return null;
		 } catch (IOException exc) {
			 System.err.println("Input failed: " + exc.toString());
			 return null;
		 } catch (Exception exc) {
			 System.err.println("Class Exception: " + exc.toString());
			 return null;
		 }
	 }
	 
	 /**
	  * Get all autogenerated columns in a table, these cannot be set
	  * @param	connection contains SQL connection to Mediabase
	  * @param 	name of database
	  * @param	schema contains the selected schema
	  * @param	tableName contains the table of which the autogenerated columns are requested
	  * @return	list of the name of the autogenerated columns
	  */
	 public static List<String> getAutoGenerated(Connection connection, String name, String schema,
			 String tableName) {
		 List<String> columns = new ArrayList<>();
		 try {
			 connection = dbConnection(filePath, name);
		     Statement stmt = connection.createStatement();
		     //stmt.execute("SET CURRENT SCHEMA " + schema);
		     
		     InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 String dbType = prop.getProperty("db.dbType_" + name);
			 input.close();
			 String query = "";
		    
		     // construct SQL-Query from path parameters 
		     stmt = connection.createStatement();
		     if (dbType.equals("MySQL")) {
		    	 query = "SELECT  column_name AS COLNAME \n" + 
		    	 		"FROM INFORMATION_SCHEMA.COLUMNS \n" + 
		    	 		"WHERE table_name = '" + name + "' AND table_schema = '" + schema + "' \n" + 
		    	 		"AND extra = \"auto_increment\";";
		     } else if (dbType.equals("DB2")) {
		    	 query = "SELECT COLNAME from SYSCAT.COLUMNS where TABNAME='" 
						    + tableName + "' AND TABSCHEMA ='" + schema + "' AND GENERATED = 'A'";
		     } else {
		    	 return null;
		     }
		     
		     
		     // execute the query
		     ResultSet rs = stmt.executeQuery(query);
		     while (rs.next()) {
		    	 columns.add(rs.getString(1));
		     }
		     connection.close();
			 return columns;
		 } catch (SQLException exc) {
			 System.err.println("Connection failed:" + exc.toString());
			 return null;
		 } catch (IOException exc) {
			 System.out.println("IOException: " + exc.toString());
			 return null;
		 }
		 
	 }

}

// public class MediabaseAPI extends RESTService {
//
//	/**
//	 * Template of a get function.
//	 * 
//	 * @return Returns an HTTP response with the username as string content.
//	 */
//	@GET
//	@Path("/get")
//	@Produces(MediaType.TEXT_PLAIN)
//	@ApiOperation(
//			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
//			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
//	@ApiResponses(
//			value = { @ApiResponse(
//					code = HttpURLConnection.HTTP_OK,
//					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
//	public Response getTemplate() {
//		UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
//		String name = userAgent.getLoginName();
//		return Response.ok().entity(name).build();
//	}
//
//	/**
//	 * Template of a post function.
//	 * 
//	 * @param myInput The post input the user will provide.
//	 * @return Returns an HTTP response with plain text string content derived from the path input param.
//	 */
//	@POST
//	@Path("/post/{input}")
//	@Produces(MediaType.TEXT_PLAIN)
//	@ApiResponses(
//			value = { @ApiResponse(
//					code = HttpURLConnection.HTTP_OK,
//					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
//	@ApiOperation(
//			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
//			notes = "Example method that returns a phrase containing the received input.")
//	public Response postTemplate(@PathParam("input") String myInput) {
//		String returnString = "";
//		returnString += "Input " + myInput;
//		return Response.ok().entity(returnString).build();
//	}
//
//	// TODO your own service methods, e. g. for RMI
//
//}
