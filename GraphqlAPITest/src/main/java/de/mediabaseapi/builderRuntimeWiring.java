package de.mediabaseapi;
//import com.coxautodev.graphql.tools.SchemaParser;
import java.sql.*;
import java.util.*;
import java.io.*;
//import com.google.common.io.Resources;

import graphql.*;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import javax.servlet.annotation.WebServlet;
import graphql.servlet.SimpleGraphQLServlet;

// dev branch test
public class builderRuntimeWiring {
	
	public static GraphQLSchema generateSchema(String url) {
		
		try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		    System.exit(1);
		}

		// connect to database
		final Connection con;
		try {
	      //The user and password have still to be set!!
		    con = DriverManager.getConnection(url, "db2info5","pfidb52ab");
		} catch (SQLException exc) {
		    System.err.println("getConnection failed:" + exc.toString());
		    return null;
		}

		// see all entries
		try {
		    // set the actual schema
			String dbSchema = "DB2INFO5";
		    Statement stmt = con.createStatement();
		    stmt.execute("SET CURRENT SCHEMA db2info5");
				
		    // construct SQL-Query
		    stmt = con.createStatement();
			// get all table names
		    String query = "SELECT * FROM SYSIBM.SYSTABLES WHERE type = 'T' AND CREATOR like 'DB2INFO5'";

		    // execute the query
		    ResultSet rs = stmt.executeQuery(query);

			List<String> tableNames = new ArrayList<String>();
		    // output resultset 
		    while (rs.next()) {
				tableNames.add(rs.getString(1));
		    }
			
			ResultSet keys = null;
			String type = "";
			String colname;
			String schema = "";
			List<String> foreignKeys = new ArrayList<String>();
			int foreignKeyCount = 0;
			String querySchema = "schema { query: Query} type Query { ";
			RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();
			
			for (String name: tableNames) {
				stmt = con.createStatement();
				// get table name, column name, type and nullability for each column in each table
				query = "SELECT TABNAME,COLNAME,TYPENAME,NULLS from SYSCAT.COLUMNS where TABNAME='" + name + "'";
				rs = stmt.executeQuery(query);
				
				stmt = con.createStatement();
				// get foreign and primary keys for each table
				// where ref.tabschema == current schema
				query = "SELECT ref.tabname as foreign_table, " +
						"ref.reftabname as primary_table, " +
						"ref.constname as fk_constraint_name " +
						"from syscat.references ref where TABNAME='" + name + "' " +
						"order by foreign_table, primary_table";
				keys = stmt.executeQuery(query); 
				
				
				// check if resultset is not empty
				if (rs.next()) {
					querySchema = querySchema + name.toLowerCase() + ": " + name + " ";
					schema = schema + " type " + name + " { ";
					
					
					// transform db2 type to GraphQL type
					do {
						type = rs.getString(3);
						colname = rs.getString(2);
						type = rs.getString(3);
						final String testname = name;
						final String testcolname = colname;
						final String testtype = type;
						runtimeWiring = runtimeWiring.type(name, typeWiring -> typeWiring
								.dataFetcher(testcolname.toLowerCase(), createDataFetcher(testname, testcolname, testtype, con)));
						switch (type) {
						case "INTEGER":
							schema = schema + " " + rs.getString(2).toLowerCase() + ": Int";
							break;
						case "SMALLINT":
							schema = schema + " " + rs.getString(2).toLowerCase() + ": Int";
							break;
						case "BIGINT":
							schema = schema + " " + rs.getString(2).toLowerCase() + ": BigInteger";
							break;
						case "DECIMAL":
							schema = schema + " " + rs.getString(2).toLowerCase() + ": Float";
							break;
						case "REAL":
							schema = schema + " " + rs.getString(2).toLowerCase() + ": Float";
							break;
						case "DECFLOAT":
							schema = schema + " " + rs.getString(2).toLowerCase() + ": Float";
							break;
						default:
							schema = schema + " " + rs.getString(2).toLowerCase() + ": String";
						}
						// check if nullable
						if (rs.getString(4).equals("N")){
							schema = schema + "!";
						}
					} while (rs.next());
					// transform foreign keys as references to other GraphQL object types
					foreignKeys.clear();
					foreignKeyCount = 0;
					while (keys.next()) {
						// handling foreign keys referencing same table, avoiding duplicate of GraphQL object types
						if (foreignKeys.contains(keys.getString(2))) {
							schema = schema + " " + keys.getString(2).toLowerCase() + String.valueOf(foreignKeyCount) + ":" + keys.getString(2) + "! ";
						} else {
							foreignKeys.add(keys.getString(2));
							schema = schema + " " + keys.getString(2).toLowerCase() + ":" + keys.getString(2) + "! ";
							foreignKeyCount++;
						}
						
					}
					schema = schema + "} ";
				}
			}
			
			querySchema = querySchema + "} ";
			schema = querySchema + schema;
		    // close resultset
		    rs.close();

		    // close SQL-query
		    stmt.close();

		    // terminate connection
		    con.close();
		    
		    System.out.println(schema);
		    SchemaParser schemaParser = new SchemaParser();
	        SchemaGenerator schemaGenerator = new SchemaGenerator();
	        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
	        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring.build());
	        return graphQLSchema;
			
		} catch (SQLException exc) {
		    System.out.println("JDBC/SQL error: " + exc.toString());
		    return null;
		}
	}
	
	private static DataFetcher<List<String>> createDataFetcher(String tableName, String columnName, String type, Connection con) {
		
		DataFetcher<List<String>> dataFetcher = new DataFetcher<List<String>>() {
			@Override
			public List<String> get(DataFetchingEnvironment environment) {
				List<String> data = new ArrayList<String>();
				String query = "SELECT '" + columnName + "' FROM '" + tableName + "'";
				try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					data.add(rs.getString(1));
				}
				for (String element: data) {
					System.out.println(element);
				}
				return data;
				} catch (SQLException exc) {
				    System.out.println("JDBC/SQL error: " + exc.toString());
				    return null;
				}
			}
		};
			
		return dataFetcher;
	}

}
