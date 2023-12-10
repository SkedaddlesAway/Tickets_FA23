//Author: Gabi Bartolo
//

package javaapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.text.*;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
		
	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		// variables for SQL Query table creations
		final String createTicketsTable =
				"CREATE TABLE g_bart_tickets2(" + 
				"ticket_id INT AUTO_INCREMENT PRIMARY KEY, "+
				"ticket_issuer VARCHAR(30), "+
				"ticket_description VARCHAR(200), "+
				"status VARCHAR(10), "+ //pending, open, closed?
				"start_date DATE, "+
				"end_date DATE)"; 
		final String createUsersTable = 
				"CREATE TABLE g_bart_users1(uid INT AUTO_INCREMENT PRIMARY KEY, " +
				"uname VARCHAR(30), " +
				"upass VARCHAR(30), " + 
				"admin int)";
		try {
			// execute queries to create tables
			statement = getConnection().createStatement();
			
			statement.executeUpdate(createTicketsTable);
			statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");
			// end create table
			// close connection/statement object
			statement.close();
			connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB
			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into g_bart_users1(uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String username, String ticketDesc) {
		int id = 0;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
			Date date = new Date();
			String time = formatter.format(date);
			statement = getConnection().createStatement();
			statement.executeUpdate("Insert into g_bart_tickets1(ticket_issuer, ticket_description, status, start_date) values(" + " '"
					+ username + "','" + ticketDesc + "', '"+"Pending"+"', '"+ time +"')", 
					Statement.RETURN_GENERATED_KEYS);
			// set auto status as pending
			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;

	}
	
	// Always check if admin is accessing or not
	public ResultSet readRecords(String username, boolean adm) {
		ResultSet results = null;
		try {
			statement = connect.createStatement();
			if(adm)
				results = statement.executeQuery("SELECT * FROM g_bart_tickets1");
			else
				results = statement.executeQuery("SELECT * FROM g_bart_tickets1 WHERE ticket_issuer = '"+username+"'");
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	//Look up tickets based on ID and making sure that normal users can only see their own tickets 
	public ResultSet ticketLookup(boolean adm, String username, int ticketId) {
		ResultSet results = null;
		try {
			statement = getConnection().createStatement();
			if (adm )
				results = statement.executeQuery("SELECT * FROM g_bart_tickets1 where ticket_id = "+ticketId);
			else
				results = statement.executeQuery("SELECT * FROM g_bart_tickets1 where ticket_id = "+ticketId + " and ticket_issuer = '"+username+"'");
			
			//Make sure the ticket is real before moving on
			if (!results.isBeforeFirst() )
				System.out.println("Could not find ticket #"+ ticketId);
			else 
				System.out.println("Ticket #"+ ticketId + " was found!");
				//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	// continue coding for updateRecords implementation
	// only let admin update tickets
	// set up error messages for normal users
	public void updateRecords( boolean adm, String username, int ticketId, String ticketDesc, String status) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		Date date = new Date();
		String time = format.format(date);
		try {
			statement = getConnection().createStatement();
			int update;
			//check if ticket is closed or not
			if (adm) {
				update = statement.executeUpdate("update g_bart_tickets1 set ticket_description = '"+ticketDesc+"', status = '"+status+" where ticket_id = "+ticketId);
				if(status.equalsIgnoreCase("closed")) 
					statement.executeUpdate("update g_bart_tickets1 set end_time = '"+time+"' where ticket_id = "+ticketId);
			}
			else {
				update = statement.executeUpdate("update g_bart_tickets1 set ticket_description = '"+ticketDesc+"'" + " where ticket_issuer = '"+username+"'" + " and ticket_id = "+ticketId);
				if(status.equalsIgnoreCase("closed")) 
					statement.executeUpdate("update g_bart_tickets1 set end_time = '"+time+"' where ticket_id = "+ticketId);
			}
			
			//check that update happened because ticket was real and user was verified
			if (update !=0) 
				System.out.println("Ticket #"+ ticketId + " has been updated!");
			else 
				System.out.println("Could not find ticket #"+ ticketId);
			//connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	// continue coding for deleteRecords implementation
	public void deleteRecords(int ticketId) {
		try {
			statement = getConnection().createStatement();
			int delete = statement.executeUpdate("delete from g_bart_tickets1 where ticket_id = "+ticketId); 
			//making sure delete can happen and wont result in error
			//There is a double check
			if (delete !=0)
				System.out.println("Ticket "+ ticketId + " has been deleted!");
			else 
				System.out.println("Could not find ticket #"+ ticketId);
			//connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
