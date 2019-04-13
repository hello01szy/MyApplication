package com.example.lenovo.myapplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBUtil {
    private Connection connection;
    private Statement statement;
    private ResultSet rs;
    private final String DBDRIVER = "com.mysql.jdbc..Driver";
    private final String URL = "jdbc:mysql://localhost:3306/szy";
    private final String USERNAME = "root";
    private final String PASSWORD = "MySql123@";
    private boolean getConnection()
    {
        try{
            Class.forName(DBDRIVER);
            connection = DriverManager.getConnection(URL);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //查询
    public ArrayList<User> query(String sql)
    {
        ArrayList<User> arrayList = new ArrayList<User>();
        if(getConnection())
        {
            try {
                statement = connection.createStatement();
                rs = statement.executeQuery(sql);
                while(rs.next())
                {
                    User u = new User();
                    u.setUserName(rs.getString("username"));
                    u.setNickName(rs.getString("nickname"));
                    u.setAvatorPath(rs.getString("avator"));
                    arrayList.add(u);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
            return arrayList;
        }
        else
        {
            return null;
        }
    }
    //修改
    public int update(String sql)
    {
        if(getConnection())
        {
            try{
                statement = connection.createStatement();
                int flag = statement.executeUpdate(sql);
                return flag;
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        else
        {
            return -1;
        }
    }
    //关闭
    public void close()
    {
        try
        {
            if(rs != null)
            {
                rs.close();
            }
            if(statement != null)
            {
                statement.close();
            }
            if(connection != null)
            {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}