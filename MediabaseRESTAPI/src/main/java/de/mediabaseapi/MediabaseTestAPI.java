/**
 *  RESTful API for Mediabase
 */
package de.mediabaseapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.PATCH;

@Api(value = "MediabaseRESTAPI")
// for las2peer testing
//@ServicePath("/mediabase")
// for API outside of las2peer
@Path("/mediabase")
//public class MediabaseTestAPI extends RESTService{
public class MediabaseTestAPI{
	
	 // API is hosted under http://localhost:8080/MediabaseRESTAPI/rest/mediabase/
	 
	 private static String filePath = "src/main/resources/config.properties";
	 private List<String> nameList = new ArrayList<>();
	 
	 @Path("/database/list")
	 @GET
	 public Response getDatabaseNames() {
		 if (nameList.isEmpty()) {
			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
					 .entity("No databases.").build();
		 } else {
			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
					 .entity(nameList.toString()).build();
		 }
	 }
	 
	 @Path("/database/list/{name}")
	 @POST
	 //@DELETE
	 public Response deleteDatabase(@PathParam("name") String name) {
		 try {
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 prop.remove("db.url_" + name);
			 prop.remove("db.user_" + name);
			 prop.remove("db.password_" + name);
			 prop.remove("db.dbSchema_" + name);
			 OutputStream output = new FileOutputStream(filePath);
			 prop.store(output, null);
			 System.out.println("DELETE");
			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
					 .entity("Deletion successful").build();
		 } catch (IOException exc) {
			 return Response.status(477).header("Access-Control-Allow-Origin", "*").build();
		 }
	 }
	 
	 @POST
	 @Path("/database/{name}")
	 //@Consumes("MediaType.APPLICATION_JSON")
	 //@ApiOperation(value = "Adds database properties, so that API calls can access it")
//	 @ApiResponses(value = { @ApiResponse (code = 200,
//			 							message = "Database added"),
//			 				@ApiResponse(code = 477,
//			 							message = "Input is not in correct JSON format")})
	 public Response addDatabase(@PathParam("name") String name, String properties) {
		 System.out.println("Name: " + name);
		 System.out.println("Properties: " + properties);
		 // .header("Access-Control-Allow-Origin", "*").allow("OPTIONS")
		 //return Response.status(200).entity("Testing the access").header("Access-Control-Allow-Origin", "*").build();
		 try {
			 if (nameList.contains(name)) {
				 return Response.status(460).header("Access-Control-Allow-Origin", "*")
						 .entity("Name of database is already in use.").build();
			 }
			 //nameList.add(name);
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 
			 if (prop.getProperty("db.url_" + name) != null && prop.getProperty("db.user_" + name) != null &&
					 prop.getProperty("db.password_" + name) != null && prop.getProperty("db.dbSchema_") != null) {
				 return Response.status(200).header("Access-Control-Allow-Origin", "*")
						 .entity("Database already added").build();
			 } else {
				 JSONObject json = new JSONObject(properties);
				 OutputStream output = new FileOutputStream(filePath);
	             prop.setProperty("db.url_" + name, json.getString("url"));
	             prop.setProperty("db.user_" + name, json.getString("user"));
	             prop.setProperty("db.password_" + name, json.getString("password"));
	             prop.setProperty("db.dbSchema_" + name, json.getString("dbSchema"));

	            // save properties to project root folder
	            prop.store(output, null);
				return Response.status(200).header("Access-Control-Allow-Origin", "*")
						.entity("Database added.").build();
			 }
		 }
		 catch (JSONException exc) {
			 System.out.println("JSON Exception: " + exc.toString());
			 return Response.status(477).header("Access-Control-Allow-Origin", "*")
					 .entity("Database properties are not provided in correct JSON format").build();
		 } catch (IOException exc) {
			 System.err.println("Input failed: " + exc.toString());
			 return Response.status(588).header("Access-Control-Allow-Origin", "*")
					 .entity("Internal file management error.").build();
		 }
		 
	 }
	 
	 // return table names
	 @Path("/metadata/{dbName}/{schema}")
	 @GET
	 public Response getTableNames(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @DefaultValue("false") @QueryParam("views") Boolean views) {
		 try {
			 
		 	 // set the actual schema
			 Connection connection = dbConnection(filePath, dbName);
		     Statement stmt = connection.createStatement();
		     
		     String query;
		     
		     if (!views) {
		    	 query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE type = 'T' AND CREATOR like '" +  schema + "'";
		     } else {
		    	 query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE (type = 'T' OR type = 'V') AND CREATOR like '"
		    			 +  schema + "'";
		     }
		     
		     // execute the query
		     ResultSet rs = stmt.executeQuery(query);
		     JSONArray json = resultSetToJSON(rs, false);
		     System.out.println(json.toString());
		     rs.close();
		     stmt.close();
		     connection.close();
		     if (json != null && json.toString().equals("[]")) {
					return Response.status(454).entity("Schema has no tables.").build();
				}
			 return Response.status(200).entity(json.toString()).build();
		 } catch (SQLException exc) {
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).entity("Schema is not present in given database.").build();
		 }
		 
	 }
	 
	 // return column name for table in schema with type and nullability
	 @Path("/metadata/{dbName}/{schema}/{tableName}")
	 @GET
	 public Response getColumnNames(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName) {
			// see all entries
			try {
			    // set the actual schema
				Connection connection = dbConnection(filePath, dbName);
			    Statement stmt = connection.createStatement();
			    stmt.execute("SET CURRENT SCHEMA " + schema);
			    
			    // construct SQL-Query from path parameters 
			    stmt = connection.createStatement();
			    String query = "SELECT COLNAME,TYPENAME,NULLS from SYSCAT.COLUMNS where TABNAME='" 
			    + tableName + "' AND TABSCHEMA ='" + schema + "'";

			    // execute the query
			    ResultSet rs = stmt.executeQuery(query);
			    
				JSONArray json = resultSetToJSON(rs, false);
				if (json != null && json.toString().equals("[]")) {
					return Response.status(454).entity("There are no columns in this table.").build();
				}
				System.out.println("Autogenerated: " + getAutoGenerated(connection, dbName, schema, "BW_ENTRIES"));
			    rs.close();
			    stmt.close();
			    connection.close();
			    
			    return Response.status(200).entity(json.toString()).build();
			} catch (SQLException exc) {
				if (exc.getMessage().contains("SQLCODE=-206, SQLSTATE=42703")) {
					return Response.status(404).entity("Table is not present in given schema.").build();
				}
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).entity("SQL error.").build();
			}
	 }
	 
	 // return primary keys of a column in table in schema
	 @Path("/metadata/{dbName}/{schema}/{tableName}/primaryKeys")
	 @GET
	 public Response getPrimaryKeys(@PathParam("dbName") String dbName, @PathParam("schema") String schema, 
			 @PathParam("tableName") String tableName) {
			try {
				Connection connection = dbConnection(filePath, dbName);
				Statement stmt = connection.createStatement();
				String query = "SELECT NAME FROM SYSIBM.SYSCOLUMNS " + 
						"WHERE TBNAME = '" + tableName + "'AND TBCREATOR = '" + schema + "' " +
						"AND KEYSEQ > 0";
				ResultSet keys = stmt.executeQuery(query);
				JSONArray json = resultSetToJSON(keys, false);
//				if (json != null && json.toString().equals("[]")) {
//					return Response.status(454).build();
//				}
				keys.close();
				stmt.close();
				connection.close();
				return Response.status(200).entity(json.toString()).build();
			} catch (SQLException exc) {
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).entity("SQL error.").build();
			}
	 }
	 
	 // return entry in column in table in schema
	 @Path("/data/{dbName}/{schema}/{tableName}")
	 @GET
	 public Response getEntry(@PathParam("dbName") String dbName, @PathParam("schema") String schema, @PathParam("tableName") String tableName, 
			 @QueryParam("condition") String condition, @QueryParam("colname") String colname) {
			// see all entries
			try {
			    // set the actual schema
				Connection connection = dbConnection(filePath, dbName);
			    Statement stmt = connection.createStatement();
			    stmt.execute("SET CURRENT SCHEMA " + schema);
			    
			    // construct SQL-Query from path parameters 
			    stmt = connection.createStatement();
			    String query = "";
			    if (colname != null && !colname.isEmpty()) {
			    	query = "SELECT " + colname + " FROM " + tableName;
			    } else {
			    	query = "SELECT * FROM " + tableName;
			    }
			    
			    // apply conditions from query parameters
			    if (condition != null && !condition.isEmpty()) {
			    	query = query + " WHERE " + condition;
			    }

			    // execute the query
			    ResultSet rs = stmt.executeQuery(query);

				JSONArray json = resultSetToJSON(rs, true);
				if (json != null && json.toString().equals("[]")) {
					return Response.status(455).entity("Entry is empty.").build();
				}
			    rs.close();
			    stmt.close();
			    connection.close();
			    
			    return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(json.toString()).build();
			} catch (SQLException exc) {
				if (exc.getMessage().contains("SQLCODE=-206, SQLSTATE=42703")) {
					return Response.status(453).entity("Table does not exits").build();
				}
				if (exc.getMessage().contains("SQLCODE=-204, SQLSTATE=42704")) {
					return Response.status(454).entity("Schema does not exist.").build();
				}
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).entity("SQL error.").build();
			}
	 }
	 
	 @Path("/pattern/{dbName}/{schema}/media/{mediaName}")
	 @GET
	 public Response pattern(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("mediaName") String mediaName) {
		 try {
			 Connection connection = dbConnection(filePath, dbName);
			 Statement stmt = connection.createStatement();
			 stmt.execute("SET CURRENT SCHEMA " + schema);

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
	 public Response putEntry(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName, String data) {
		 try {
			// set the actual schema
			Connection connection = dbConnection(filePath, dbName);
		    Statement stmt = connection.createStatement();
		    stmt.execute("SET CURRENT SCHEMA " + schema);
			    
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
		    return Response.status(200).build();
		 } catch (SQLException exc) {
			 System.out.println("JDBC/SQL error: " + exc.toString());
		     return Response.status(450).entity("SQL error.").build();
		 } catch (JSONException exc) {
			 System.out.println("JSON error: " + exc.toString());
			 return Response.status(480).entity("Data is not in valid JSON format").build();
			 
		 }
	 }
	 
	 @Path("/data/{dbName}/{schema}/{tableName}")
	 @DELETE
	 public Response deleteEntry(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @PathParam("tableName") String tableName, @QueryParam("condition") String condition) {
		 try {
			// set the actual schema
			Connection connection = dbConnection(filePath, dbName);
		    Statement stmt = connection.createStatement();
		    stmt.execute("SET CURRENT SCHEMA " + schema);
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
			 return Response.status(400).entity("SQL error.").build();
		 }
	 }
	 
	 @Path("/data/query/{dbName}/{schema}")
	 @GET
	 public Response sqlQuery(@PathParam("dbName") String dbName, @PathParam("schema") String schema,
			 @QueryParam("query") String query) {
		 try {
			 Connection connection = dbConnection(filePath, dbName);
			 System.out.println("SQL Query: " + query);
			 Statement stmt = connection.createStatement();
			 stmt.execute("SET CURRENT SCHEMA " + schema);
			 ResultSet rs = stmt.executeQuery(query);
			 JSONArray json = resultSetToJSON(rs, false);
			 if (json != null && json.toString().equals("[]")) {
				return Response.status(455).build();
			 }
			 return Response.status(200).header("Access-Control-Allow-Origin", "*")
					 .entity(json.toString()).build();
		 } catch (SQLException exc) {
			 System.out.println("SQLException: " + exc.toString());
			 return Response.status(400).header("Access-Control-Allow-Origin", "*")
					 .entity("SQL error: " + exc.toString()).build();
		 }
	 }
	 
	 @Path("view/{dbName}/{schema}/{viewName}")
	 @PUT
	 public Response createView(@PathParam("schema") String schema, @PathParam("viewName") String viewName,
			 @PathParam("dbName") String dbName, String columns, String tables, String conditions) {
		 try {
			 Connection connection = dbConnection(filePath, dbName);
			 Statement stmt = connection.createStatement();
			 String query = "CREATE VIEW [" + viewName + "]";
			 stmt.execute(query);
			 stmt.close();
		     connection.close();
			 return Response.status(200).build(); 
		 } catch (SQLException exc) {
			 return Response.status(400).entity("SQL error.").build();
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
				while(resultSet.next() && (!limit || count < 16)) {
					int numColumns = rsmd.getColumnCount();
					JSONObject obj = new JSONObject();
				    for (int i = 1; i <= numColumns; i++) {
				    	String columnName = rsmd.getColumnName(i).toLowerCase();
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
	  * @param		filePath contains the path to the properties file containing url, user and password
	  * @return		If successful, a SQL connection, else null 
	  */
	 public static Connection dbConnection(String filePath, String name) {
		 try {
			 Class.forName("com.ibm.db2.jcc.DB2Driver");
		 } catch (ClassNotFoundException exc) {
			 System.err.println("Could not load DB2Driver:" + exc.toString());
		 }
		 Connection connection = null;
		 try {
			 InputStream input = new FileInputStream(filePath);
			 Properties prop = new Properties();
			 prop.load(input);
			 connection = DriverManager.getConnection(prop.getProperty("db.url_" + name),
					 prop.getProperty("db.user_" + name), prop.getProperty("db.password_" + name));
			 input.close();
			 return connection;
		 } catch (SQLException exc) {
			 System.err.println("Connection failed:" + exc.toString());
			 return null;
		 } catch (IOException exc) {
			 System.err.println("Input failed: " + exc.toString());
			 return null;
		 }
	 }
	 
	 /**
	  * Get all autogenerated columns in a table, these cannot be set
	  * @param	connection contains SQL connection to Mediabase
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
		     stmt.execute("SET CURRENT SCHEMA " + schema);
		    
		     // construct SQL-Query from path parameters 
		     stmt = connection.createStatement();
		     String query = "SELECT COLNAME from SYSCAT.COLUMNS where TABNAME='" 
					    + tableName + "' AND TABSCHEMA ='" + schema + "' AND GENERATED = 'A'";
		     
		     // execute the query
		     ResultSet rs = stmt.executeQuery(query);
		     while (rs.next()) {
		    	 columns.add(rs.getString(1));
		     }
			 return columns;
		 } catch (SQLException exc) {
			 System.err.println("Connection failed:" + exc.toString());
			 return null;
		 }
		 
	 }

}
