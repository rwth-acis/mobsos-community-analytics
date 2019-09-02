package de.mediabaseapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BW_ENTRIES_REPOSITORY {
	
	public List<BW_ENTRIES> bw_entries;
	
	public BW_ENTRIES_REPOSITORY() {
		bw_entries = new ArrayList<>();
		
		try {
		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException exc) {
		    System.err.println("Could not load DB2Driver:" + exc.toString());
		    System.exit(1);
		}

		// connect to database
		Connection con = null;
		try {
	       String url = "jdbc:db2://beuys.informatik.rwth-aachen.de:50003/mav_meas";
	      //The user and password have still to be set!!
		   con = DriverManager.getConnection(url, "db2info5","pfidb52ab");
		} catch (SQLException exc) {
		    System.err.println("getConnection failed:" + exc.toString());
		    return;
		}

		// see all entries
		try {
		    // set the actual schema
		    Statement stmt = con.createStatement();
		    stmt.execute("SET CURRENT SCHEMA db2info5");
		    
		    // construct SQL-Query 
		    
		    stmt = con.createStatement();
		    String query = "SELECT * FROM BW_ENTRIES";
		    System.out.println(query);

		    // execute the query
		    ResultSet rs = stmt.executeQuery(query);

		    int count = 0;
		    BW_ENTRIES entry = null;
		    // output resultset 
		    while ( rs.next() && count < 16) {
		    	entry = new BW_ENTRIES();
		    	entry.setId(rs.getInt(1));
		    	entry.setProject_id(rs.getInt(2));
		    	entry.setPerma_link(rs.getString(3));
		    	entry.setContent_chunk(rs.getString(4));
		    	entry.setTitle(rs.getString(5));
		    	entry.setTrackback_url(rs.getString(6));
		    	entry.setMood(rs.getString(7));
		    	entry.setContent(rs.getString(8));
		    	entry.setAuthor_id(rs.getInt(9));
		    	entry.setCommentlink(rs.getString(10));
		    	entry.setEst_date(rs.getString(11));
		    	entry.setRes_code(rs.getString(12));
		    	entry.setWord_set(rs.getString(13));
		    	entry.setWord_count(rs.getString(14));
		    	
		    	bw_entries.add(entry);
				count++;
				
		    }

		    // close resultset
		    rs.close();

		    // close SQL-query
		    stmt.close();

		    // terminate connection
		    con.close();
		} catch (SQLException exc) {
		    System.out.println("JDBC/SQL error: " + exc.toString());
		    return;
		}
	}
	
	public List<BW_ENTRIES> getAll() {
		return bw_entries;
	}
	
	public void setEntry(BW_ENTRIES entry) {
		bw_entries.add(entry);
	}

}
