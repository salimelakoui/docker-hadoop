package org.nway.spark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestHive {
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		// replace "hive" here with the name of the user the queries should run as
		Connection con = DriverManager.getConnection("jdbc:hive2://localhost:10000/default", "root", "");
		Statement stmt = con.createStatement();
		String tableName = "testHiveDriverTable";
		stmt.execute("drop table if exists " + tableName);
		stmt.execute("create table " + tableName + " (key int, value string)");

		stmt.execute("drop table if exists courses");
		stmt.execute("create table courses(course_id int,course_name string,students_enrolled int)");
		stmt.execute("describe courses");
		stmt.execute("INSERT INTO TABLE courses VALUES (2,'spark',3500)");

		// show tables
		// String sql = "show tables '" + tableName + "'";
		String sql = ("show tables");
		ResultSet res = stmt.executeQuery(sql);
		if (res.next()) {
			System.out.println(res.getString(1));
		}
	}
}