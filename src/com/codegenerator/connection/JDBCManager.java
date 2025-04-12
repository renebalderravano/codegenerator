package com.codegenerator.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.codegenerator.util.Column;
import com.codegenerator.util.PropertiesReading;

public class JDBCManager {

	String host = "";
	String port = "";
	String username = ""; // MySQL credentials
	String password = "";
	String server = "";
	private Connection con;

	public JDBCManager(String server, String host, String port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.server = server;

	}

	public boolean connect() {
		String url = "jdbc:"; // table details

		try {
			String prop = server.trim() + ".datasource.driver-class-name";

			String driver = PropertiesReading.getProperty(prop);
			StringBuilder urlDB = new StringBuilder(PropertiesReading.getProperty(server + ".datasource.url"));

			url = urlDB.toString().replace("?1", host).replace("?2", port);

			Class.forName(driver);
			// Driver name
			setConnection(DriverManager.getConnection(url, username, password));

			System.out.println("Connection successfull !!!!");
			return true;

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public List<String> getDataBases() {

		List<String> dbs = new ArrayList<String>();
		Statement st;
		try {
			st = getConnection().createStatement();
			ResultSet rs = null;

			String query = PropertiesReading.getProperty(server + ".query.database");

			rs = st.executeQuery(query);

			while (rs.next()) {
				String name = rs.getString("Database"); // Retrieve name from db
				dbs.add(name);
			}

			st.close(); // close statement
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbs;
	}

	public List<String> getTableFromDataBase(String database) {
		List<String> tables = new ArrayList<String>();
		Statement st;
		try {

			String query = PropertiesReading.getProperty(server + ".query.tables");
			
			query = query.replace("?1", database);
//			if (server.equals("MySQL")) {
//				query = "SELECT * FROM information_schema.tables " + "WHERE table_schema = '" + database + "'";
//
//			} else {
//
//				query = "use " + database + ";  SELECT * FROM information_schema.tables; ";
//			}
			st = getConnection().createStatement();
			ResultSet rs = st.executeQuery(query); // Execute query
			while (rs.next()) {
				String name = rs.getString("table_name"); // Retrieve name from db
				tables.add(name);
			}

			st.close(); // close statement
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tables;
	}

	public List<Column> getColumnsByTable(String database, String tableName) {
		List<Column> columns = new ArrayList<Column>();
		Statement st;
		try {
			String query = PropertiesReading.getProperty(server + ".query.columns");
			
			query = query.replace("?1", database);
			query = query.replace("?2", tableName);
			st = getConnection().createStatement();
			ResultSet rs = st.executeQuery(query); // Execute query
			while (rs.next()) {
				Column column = new Column();
				column.setName(rs.getString("column_name"));
				column.setDataType(rs.getString("data_type"));
				column.setIsNullable(rs.getString("is_nullable").equals("YES"));
				column.setIsPrimaryKey(rs.getString("column_key").equals("PRI"));
				column.setIsForeignKey(rs.getString("column_key").equals("MUL"));
				columns.add(column);
			}

			st.close(); // close statement
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return columns;
	}

	/**
	 * @return the con
	 */
	public Connection getConnection() {
		return con;
	}

	/**
	 * @param con the con to set
	 */
	public void setConnection(Connection con) {
		this.con = con;
	}

}
