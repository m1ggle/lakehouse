package com.ronglian.lakehouse.main.util;

import com.ronglian.lakehouse.main.common.GmallConfig;

import java.sql.*;
import java.util.Properties;

//org.apache.phoenix.queryserver.client.Driver
public class PhoenixJDBCExample {
    static final String JDBC_DRIVER = GmallConfig.PHOENIX_DRIVER;

    static final String DB_URL = GmallConfig.PHOENIX_SERVER;

    public static void main(String[] args) {

        Properties props = new Properties();
        props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
        props.setProperty("phoenix.schema.mapSystemTablesToNamespace", "true");


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
            st.executeUpdate("create table test_main (main_key integer not null primary key, main_column varchar)");
            st.executeUpdate("upsert into test_main values (1,'Hello')");

            // Query for table
            ps = conn.prepareStatement("select * from test_main");
            rs = ps.executeQuery();
            System.out.println("Table Values");

            while(rs.next()) {
                int mainKey = rs.getInt(1);
                String mainColumn = rs.getString(2);
                System.out.println("\tRow: " + mainKey + " = " + mainColumn);
            }

            rs.close();
            st.close();
            conn.close();

        } catch (Exception se) {
            se.printStackTrace();
        }// Handle errors for Class.forName
        finally {
            // finally block used to close resources
            try {
                if (st != null)
                    st.close();
            } catch (SQLException ignored) {
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
