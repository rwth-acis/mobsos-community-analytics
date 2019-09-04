/**
 *  @author Kersjes
 *  RESTful API for Mediabase
 */
package de.mediabaseapi;
 
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import java.sql.*;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;

// for las2peer testing
@ServicePath("/mediabase")
// for API outside of las2peer
//@Path("/mediabase")
public class MediabaseTestAPI extends RESTService{
	
	 // API is hosted under http://localhost:8080/MediabaseRESTAPI/rest/mediabase/
	 private Connection connection = dbConnection("jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_meas", "db2info5", "pfidb52ab");
	   	 
	 // return table names
	 @Path("/metadata/{schema}")
	 @GET
	 public Response getTableNames(@PathParam("schema") String schema) {
		 try {
		 	 // set the actual schema
		     Statement stmt = connection.createStatement();
		     String query = "SELECT NAME FROM SYSIBM.SYSTABLES WHERE type = 'T' AND CREATOR like '" +  schema + "'";

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
			    
			    return Response.status(200).entity(json.toString()).build();
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
	 
	 public static Connection dbConnection(String url, String username, String password) {
		 try {
			 Class.forName("com.ibm.db2.jcc.DB2Driver");
		 } catch (ClassNotFoundException exc) {
			 System.err.println("Could not load DB2Driver:" + exc.toString());
		 }
		 Connection connection = null;
		 try {
			 connection = DriverManager.getConnection(url, username, password);
			 return connection;
		 } catch (SQLException exc) {
			 System.err.println("Connection failed:" + exc.toString());
			 return null;
		 }
	 }
}
