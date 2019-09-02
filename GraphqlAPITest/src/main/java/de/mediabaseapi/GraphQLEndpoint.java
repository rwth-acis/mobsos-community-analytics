/**
 *  @author Clemens Kersjes
 *  GraphQL API for Mediabase
 */

package de.mediabaseapi;
import java.sql.*;
import java.util.*;
//import com.google.common.io.Resources;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLSchema;
import graphql.schema.SelectedField;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;


@Path("/graphql")
public class GraphQLEndpoint{
	
	@Path("nexttest")
	public Response nexttest() {
		String schema = "type BW_ENTRIES {id: ID! project_id: Int! perma_link: String!"
				+ " content_chunk: String title: String trackback_url: String insert_date: String!"
				+ " mood: String content: String author_id: Int commentlink: String"
				+ " est_date: String res_code: Int! word_set: String word_count: Int}"
				+ " type Query{getEntries(id: ID): [BW_ENTRIES]} schema {query: Query}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
        BW_ENTRIES_REPOSITORY repo = new BW_ENTRIES_REPOSITORY();
        
        DataFetcher<DataFetcherResult> datafetcher = new DataFetcher<DataFetcherResult>() {
            @Override
            public DataFetcherResult get(DataFetchingEnvironment environment) {
            	System.out.println("Entry datafetcher");
            	List<Map<String, Object>> objectList = new ArrayList<>();
            	//Map<String, Object> objectMap = new HashMap<>();
            	Map<String, Object> save = new HashMap<>();
            	int count = 0;
            	for (BW_ENTRIES object: repo.getAll()) {
            		Map<String, Object> objectMap = new HashMap<>();
            		if ((""+object.getId()).equals(environment.getArgument("id"))) {
                		objectMap.put("id", object.getId());
                		objectList.add(objectMap);	
            		}
            	}
            	
            	System.out.println("List: " + objectList.toString());
            	for (Map<String, Object> map : objectList) {
            		System.out.println(map.get("id"));
            	}
                return DataFetcherResult.newResult()
                        .data(objectList).localContext(environment.getArguments())
                        .build();
            }
        };

        DataFetcher<Object> datafetcherID = new DataFetcher<Object>() {
            @Override
            public Object get(DataFetchingEnvironment environment) {
            	System.out.println("Entry datafetcherID");
            	System.out.println(environment.getLocalContext().toString());
            	//BW_ENTRIES parent = environment.getSource();
            	Map<String, Object> map = environment.getSource();
                return map.get("id");
            }
        };
        
        Builder runtimeWiring = newRuntimeWiring();
        runtimeWiring = runtimeWiring.type("Query", builder -> builder.dataFetcher("getEntries",  datafetcher));
        runtimeWiring = runtimeWiring.type("BW_ENTRIES", typeWiring -> typeWiring
        		.dataFetcher("id", datafetcherID));

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring.build());

        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{getEntries(id: 915609) {id}}");

        System.out.println(executionResult.getData().toString());
        return Response.status(200).build();
	}
	
	List<String> primaryKeyValues;

	@Path("test")
	public Response test() {
				
		// database url
        String url = "jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_meas";
        String dbSchema = "DB2INFO5";
        Connection con = null;
        
        // load JDBC-driver
        try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		    return null;
		}
        try {
 	       // TODO get username and password from outside source
 		   con = DriverManager.getConnection(url, "db2info5","pfidb52ab");
 	        
 	        //GraphQLSchema graphQLSchema = builderRuntimeWiring.generateSchema(con);
 		    // build GraphQL API form schema
 	        GraphQLSchema graphQLSchema = generateSchema(con, dbSchema);
 	        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
 	        
 	        String graphqlQuery = "query { bw_entries(id: 915609){ perma_link, insert_date}}";
 	        //graphqlQuery = "query {bw_bursts { project_id, word}}";
 	        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(graphqlQuery).build();
 	        ExecutionResult executionResult = build.execute(executionInput);
 	        Object data = executionResult.getData();
 	        //ExecutionResult executionResult = build.execute("{bw_entries(id0: 915609){ perma_link }}");
 	        System.out.println("Errors: " + executionResult.getErrors().toString());
 	        
 	        con.close();
 	        if (!executionResult.isDataPresent()) {
 	        	System.out.println("Error before execution!");
 	        	return Response.status(400).build();
 	        } else if (data == null) {
 	        	System.out.println("Error during execution!");
 	        	return Response.status(400).build();
 	        } else {
 	        	System.out.println(data.toString());
 	            return Response.status(200).build();
 	        }
 		} catch (SQLException exc) {
 		    System.err.println("getConnection failed:" + exc.toString());
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
			String querySchema = "schema { query: Query} type Query { ";
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
						querySchema = querySchema + "): [" + name + "] ";
					} else {
						querySchema = querySchema + ": [" + name + "] ";
					}
					
					// transform db2 entries to GraphQL object types and build runtime wiring
					// each table in database becomes GraphQL object type with column names as fields
					schema = schema + " type " + name + " { ";
					do {
						colname = rs.getString(2);
						type = rs.getString(3);
						final String finalname = name;
						final String finalcolname = colname;

						// build runtime wiring for GraphQL object types and set data fetchers
						runtimeWiring = runtimeWiring.type(name, typeWiring -> typeWiring
								.dataFetcher(finalcolname.toLowerCase(), 
								createTypeDataFetcher(finalname, finalcolname, dbSchema, con)));
						
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
							schema = schema + "!";
						}

					} while (rs.next());
					schema = schema + "} ";
				}
			}
			
			querySchema = querySchema + "} ";
			//querySchema = "schema { query: Query} type Query { bw_entries(id: ID): [BW_ENTRIES] } ";
			schema = querySchema + schema;
		    // close resultset
		    rs.close();
		    
		    System.out.println(schema);
		    // build runtime wiring for the query types
		    for (String name: tableNames) {
		    	runtimeWiring = runtimeWiring.type("Query",
		    			typeWiring -> typeWiring.dataFetcher(name.toLowerCase(), createQueryDataFetcher(name, dbSchema, con)));
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
	private DataFetcher<List<Map<String, Object>>> createQueryDataFetcher(String tableName, String schema, Connection con) {
		
		return new DataFetcher<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> get(DataFetchingEnvironment environment) {
				
				// collect subfields from query and transform to SQL columns
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
				String query;
				
				// apply fields if they have been specified
				if (subfieldSelection.equals("")) {
					query = "SELECT * FROM " + tableName;
				} else {
					query = "SELECT " + subfieldSelection + " FROM " + tableName;
				}
				// check if there are any parameters
				if (environment.getArguments() != null && environment.getArguments().isEmpty()) {
					// transform parameters into SQL conditions for the query
					List<String> parameter = new ArrayList<String>();
					List<String> keys = new ArrayList<String>(environment.getArguments().keySet());
					for (int i = 0; i < keys.size(); i++) {
						//parameter.add(environment.getArgument(keys.get(i)));	
						if (i == 0) {
							query = query + " WHERE ";
						}
						if (i < keys.size() - 1) {
							query = query + keys.get(i).toUpperCase() + " = " + environment.getArgument(keys.get(i)) + " AND ";
						} else {
							query = query + keys.get(i).toUpperCase() + " = " + environment.getArgument(keys.get(i));
						}
					}
//					query = query + " WHERE ";
//					if (primaryKeys.size() == parameter.size()) {
//						System.out.println("size check");
//						for (int i = 0; i < primaryKeys.size(); i++) {
//							if (i < primaryKeys.size() - 1) {
//								query = query + primaryKeys.get(i) + " = " + parameter.get(i) + " AND ";
//							} else {
//								query = query + primaryKeys.get(i) + " = " + parameter.get(i);
//							}
//							
//						}
//					}
//					else {
//						return null;
//					}
					primaryKeyValues = parameter;
				}
				
				try {
					System.out.println(query);
					// execute query for GraphQL query type
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(query);
					
					// build list of GraphQL objects as list of HashMaps, each HashMap entry contains the field name
					// and the field value
					List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();
					ResultSetMetaData rsmd = rs.getMetaData();
					// only limited number of entries are queries for testing purposes
					int count = 0; 
					while(rs.next() && count < 16) {
						Map<String, Object> object = new HashMap<>();
						int numColumns = rsmd.getColumnCount();
					    for (int i = 1; i <= numColumns; i++) {
					    	String column_name = rsmd.getColumnName(i).toLowerCase();
					    	object.put(column_name.toLowerCase(), rs.getObject(column_name));
					    }
					    objectList.add(object);
					    count++;
					}
					System.out.println(objectList.toString());
					rs.close();
					stmt.close();
					return objectList;
				} catch (SQLException exc) {
				    System.out.println("JDBC/SQL error: " + exc.toString());
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
	private DataFetcher<Object> createTypeDataFetcher(String tableName, String colname, String schema, Connection con) {
		
		return new DataFetcher<Object> () {
			@Override
			public Object get(DataFetchingEnvironment environment) {
				try {
//					System.out.println("Type Data Fetcher");
//					Object objectTest = environment.getSource();
//					Object rootTest = environment.getExecutionStepInfo();
//					//System.out.println("Object: " + objectTest.toString());
//					//System.out.println("Info: " + rootTest.toString());
//					System.out.println(environment.getSource().toString());
					Map<String, Object> output = environment.getSource();
//					System.out.println("Output: " + output.get(colname.toLowerCase()));
//					int count = 0;
					Statement stmt = con.createStatement();
//					List<String> primaryKeys = getPrimaryKeys(tableName, schema, con);
//					int primaryKeysLength = primaryKeys.size();
//					List<Object> data = new ArrayList<Object>();
//					String query = "SELECT " + colname + " FROM " + tableName;
//					//  primaryKeyValues are null
//					if (primaryKeyValues != null && primaryKeyValues.size() == primaryKeysLength) {
//						for (int i = 0; i < primaryKeysLength; i++) {
//							if (i == 0) {
//								query = query + " WHERE ";
//							}
//							query = query + primaryKeys.get(i) + " = " + primaryKeyValues.get(i);
//							if (i < primaryKeysLength - 1) {
//								query = query + " AND ";
//							}
//						}
//					}
//					System.out.println(query);
//					ResultSet rs = stmt.executeQuery(query);
//					
//					List<ArrayList<Object>> dataList = new ArrayList<ArrayList<Object>>();
//					ArrayList<Object> entryList = new ArrayList<Object>();
//					JSONArray json = new JSONArray();
//					ResultSetMetaData rsmd = rs.getMetaData();
//					Map<String, Object> test = new HashMap<>();
//					while(rs.next() && count < 16) {
//						entryList.clear();
//						int numColumns = rsmd.getColumnCount();
//						JSONObject obj = new JSONObject();
//					    for (int i = 1; i <= numColumns; i++) {
//					    	String column_name = rsmd.getColumnName(i).toLowerCase();
//					    	test.put(column_name.toLowerCase(), rs.getObject(column_name));
//					    	obj.put(column_name, rs.getObject(column_name));
//					    	entryList.add(rs.getObject(column_name));
//					    }
//					    dataList.add(entryList);
//					    json.put(obj);
//					    count++;
//					}
//					stmt.close();
//					rs.close();
					//return DataFetcherResult.newResult().data(dataList).build();
					return DataFetcherResult.newResult().data(output.get(colname.toLowerCase())).build();
				} catch (SQLException exc) {
					System.out.println("JDBC/SQL error: " + exc.toString());
					return null;
				}
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
	protected static List<String> getPrimaryKeys(String tableName, String schema, Connection con) {
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
	protected static List<String> getForeignTables(String tableName, String schema, Connection con) {
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
}
