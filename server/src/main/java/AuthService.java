import io.netty.channel.Channel;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    HashMap<Channel, String> activeUserList = new HashMap<>();

    public AuthService() {
        connect();
    }

    public boolean loginUser(Channel cn, String userName, String password) throws SQLException{

        String request = String.format("select user_name from users where " +
                "user_name = '%s' and password = '%s'", userName, DigestUtils.md5Hex(password));

        ResultSet rs = stmt.executeQuery(request);
        if (rs.next()){
            activeUserList.put(cn, userName);
            return true;
        }
        return false;
    }

    public ArrayList<String> getUserFiles(Channel cn) throws SQLException{

        ArrayList<String> files = new ArrayList<>();
        String request = String.format("select file_name, file_size from user_files where user = '%s'", activeUserList.get(cn));

        ResultSet rs = stmt.executeQuery(request);
        while (rs.next()){
            files.add(rs.getString("file_name")+"/"+ rs.getLong("file_size"));
        }

        return files;
    }

    public void addFile(Channel cn, String file_name, long file_size) throws SQLException{
        String request = String.format("insert into user_files(user, file_name, file_size) values ('%s', '%s', %s)",
                activeUserList.get(cn), file_name, file_size);
        stmt.executeUpdate(request);
    }

    public void deleteFile(Channel cn, String file_name) throws SQLException{
        String request = String.format("delete from user_files where user = '%s' and file_name = '%s'", activeUserList.get(cn), file_name);
        stmt.executeUpdate(request);
    }

    public boolean isAuthorized(Channel cn){
        return activeUserList.containsKey(cn);
    }


    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:MainDB.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}