package com.ronglian.lakehouse.main.util;

import java.sql.*;
import java.util.Properties;

//org.apache.phoenix.queryserver.client.Driver
public class PhoenixJDBCExample {
    static final String JDBC_DRIVER = "org.apache.phoenix.jdbc.PhoenixDriver";
    static final String IP = "10.41.5.218";
    static final String PORT = "2181";
    static final String DB_URL = "jdbc:phoenix:" + IP + ":" + PORT + "/";

    public static void main(String[] args) {

        Properties props = new Properties();
//        props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
//        props.setProperty("phoenix.schema.mapSystemTablesToNamespace", "true");
        props.setProperty("hbase.table.sanity.checks", "true");

        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            Class.forName(JDBC_DRIVER);

            System.out.println("Connecting to database..");

            conn = DriverManager.getConnection(DB_URL,props);

            System.out.println("Creating statement...");

            st = conn.createStatement();
            // Execute our statements
//            st.executeUpdate("create table javatest (mykey integer not null primary key, mycolumn varchar)");
//            st.executeUpdate("upsert into javatest values (1,'Hello')");
//          Hello  st.executeUpdate("upsert into javatest values (2,'Java Application')");

            // Query for table
            ps = conn.prepareStatement("select * from javatest");
            rs = ps.executeQuery();
            System.out.println("Table Values");

            while(rs.next()) {
                Integer myKey = rs.getInt(1);
                String myColumn = rs.getString(2);
                System.out.println("\tRow: " + myKey + " = " + myColumn);
            }

            rs.close();
            st.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // finally block used to close resources
            try {
                if (st != null)
                    st.close();
            } catch (SQLException se2) {
            } // nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            } // end finally try
        } // end try
        System.out.println("Goodbye!");
    }
}
