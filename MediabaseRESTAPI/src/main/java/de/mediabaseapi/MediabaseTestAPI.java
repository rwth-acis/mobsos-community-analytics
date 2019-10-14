/**
 *  RESTful API for Mediabase
 */
package de.mediabaseapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;

// for las2peer testing
//@ServicePath("/mediabase")
// for API outside of las2peer
@Path("/mediabase")
//public class MediabaseTestAPI extends RESTService{
public class MediabaseTestAPI{
	
	 // API is hosted under http://localhost:8080/MediabaseRESTAPI/rest/mediabase/
	 
	 private static String filePath = "src/main/resources/config.properties";
	 
	 // return table names
	 @Path("/metadata/{schema}")
	 @GET
	 public Response getTableNames(@PathParam("schema") String schema, @DefaultValue("false") @QueryParam("views") Boolean views) {
		 try {
		 	 // set the actual schema
			 Connection connection = dbConnection(filePath);
		     Statement stmt = connection.createStatement();
		     String query;
		     
		     if (!views) {
		    	 query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE type = 'T' AND CREATOR like '" +  schema + "'";
		     } else {
		    	 query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE (type = 'T' OR type = 'V') AND CREATOR like '" +  schema + "'";
		     }
		     
		     // execute the query
		     ResultSet rs = stmt.executeQuery(query);
		     JSONArray json = resultSetToJSON(rs);
		     rs.close();
		     stmt.close();
		     connection.close();
		     if (json != null && json.toString().equals("[]")) {
					return Response.status(454).build();
				}
			 return Response.status(200).entity(json.toString()).build();
		 } catch (SQLException exc) {
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).build();
			}
		 
	 }
	 
	 // return column name for table in schema
	 @Path("/metadata/{schema}/{tableName}")
	 @GET
	 public Response getColumnNames(@PathParam("schema") String schema, @PathParam("tableName") String tableName) {
			// see all entries
			try {
			    // set the actual schema
				Connection connection = dbConnection(filePath);
			    Statement stmt = connection.createStatement();
			    stmt.execute("SET CURRENT SCHEMA " + schema);
			    
			    // construct SQL-Query from path parameters 
			    stmt = connection.createStatement();
			    String query = "SELECT COLNAME from SYSCAT.COLUMNS where TABNAME='" 
			    + tableName + "' AND TABSCHEMA ='" + schema + "'";

			    // execute the query
			    ResultSet rs = stmt.executeQuery(query);
			    
				JSONArray json = resultSetToJSON(rs);
				if (json != null && json.toString().equals("[]")) {
					return Response.status(454).build();
				}
			    rs.close();
			    stmt.close();
			    connection.close();
			    
			    return Response.status(200).entity(json.toString()).build();
			} catch (SQLException exc) {
				if (exc.getMessage().contains("SQLCODE=-206, SQLSTATE=42703")) {
					return Response.status(404).build();
				}
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).build();
			}
	 }
	 
	 // return entry in column in table in schema
	 @Path("/data/{schema}/{tableName}")
	 @GET
	 public Response getEntry(@PathParam("schema") String schema, @PathParam("tableName") String tableName, 
			 @QueryParam("condition") String condition, @QueryParam("colname") String colname) {
			// see all entries
			try {
			    // set the actual schema
				Connection connection = dbConnection(filePath);
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

				JSONArray json = resultSetToJSON(rs);
				if (json != null && json.toString().equals("[]")) {
					return Response.status(455).build();
				}
			    rs.close();
			    stmt.close();
			    connection.close();
			    
			    return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(json.toString()).build();
			} catch (SQLException exc) {
				if (exc.getMessage().contains("SQLCODE=-206, SQLSTATE=42703")) {
					return Response.status(453).build();
				}
				if (exc.getMessage().contains("SQLCODE=-204, SQLSTATE=42704")) {
					return Response.status(454).build();
				}
			    System.out.println("JDBC/SQL error: " + exc.toString());
			    return Response.status(400).build();
			}
	 }
	 
	 @Path("/pattern/{schema}/media/{mediaName}")
	 @GET
	 public Response pattern(@PathParam("schema") String schema, @PathParam("mediaName") String mediaName) {
		 try {
			 Connection connection = dbConnection(filePath);
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
			 
			 JSONArray json = resultSetToJSON(rs);
			 if (json != null && json.toString().equals("[]")) {
				return Response.status(455).build();
			 }
			
			 return Response.status(200).entity(json.toString()).build();
			 			 
			 
		 } catch (SQLException exc) {
			 return Response.status(400).build();
			 
		 }
	 }
	 
	 @Path("/data/{schema}/{tableName}")
	 @PUT
	 public Response putEntry(@PathParam("schema") String schema, @PathParam("tableName") String tableName, String data) {
		 try {
			// set the actual schema
			Connection connection = dbConnection(filePath);
		    Statement stmt = connection.createStatement();
		    stmt.execute("SET CURRENT SCHEMA " + schema);
			    
		    // construct SQL-Query from path parameters 
		    stmt = connection.createStatement();
		    
		    // extract input values from query parameter
		    JSONObject json = new JSONObject(data);
		    Iterator<String> keys = json.keys();
		    List<String> autoGenerated = getAutoGenerated(connection, schema, tableName);
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
		     return Response.status(450).build();
		 } catch (JSONException exc) {
			 System.out.println("JSON error: " + exc.toString());
			 return Response.status(480).build();
			 
		 }
	 }
	 
	 @Path("/data/{schema}/{tableName}")
	 @DELETE
	 public Response deleteEntry(@PathParam("schema") String schema, @PathParam("tableName") String tableName,
			 @QueryParam("condition") String condition) {
		 try {
			// set the actual schema
			Connection connection = dbConnection(filePath);
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
			 System.out.println();
			 return Response.status(400).build();
		 }
	 }
	 
	 private JSONArray resultSetToJSON(ResultSet resultSet) {
		 try {
				JSONArray json = new JSONArray();
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int count = 0;
				while(resultSet.next() && count < 16) {
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
	 public static Connection dbConnection(String filePath) {
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
			 connection = DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
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
	 public static List<String> getAutoGenerated(Connection connection, String schema, String tableName) {
		 List<String> columns = new ArrayList<>();
		 try {
			 connection = dbConnection(filePath);
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
