package me.cvenomz.OwnBlocks;

import java.sql.SQLException;

public class MysqlTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        MysqlDatabase db = new MysqlDatabase(null, "localhost", "ob", "user", "resu");
        MysqlBlock mb = new MysqlBlock("world", 98, 23, -13, "cvenomz", null, null, null);
        try {
            db.establishConnection();
            db.CheckOBTable();
            db.addBlock(mb);
            db.closeConnection();
            System.out.println("connection closed");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
