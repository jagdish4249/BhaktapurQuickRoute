package com.example.jagdishduwal.bhaktapurquickroute.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class NodeGateway {
    private SQLiteDatabase db;
    private SQLHelper dbHelper;

    public NodeGateway(Context context) {
        dbHelper = new SQLHelper(context);
    }

    public void open() {
        try {
            db = dbHelper.getReadableDatabase();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
    }

    public void close() {
        db.close();
    }

    public ArrayList<Node> getAllNodes() {
        ArrayList<Node> nodeList = new ArrayList<Node>();


        try {
            open();
            String selectQuery = "SELECT  * FROM " + SQLHelper.TABLE_NODES;
            Cursor cursor = db.rawQuery(selectQuery, null);


            if (cursor.moveToFirst()) {
                do {
                    Node node = new Node();
                    node.setID(cursor.getInt(0));
                    node.setLongitude(cursor.getString(1));
                    node.setLatitude(cursor.getString(2));
                    nodeList.add(node);

                } while (cursor.moveToNext());
            }

            cursor.close();
            close();

        } catch (Exception e) {
                Log.i("error while returning: ", e.getMessage());
        }

        return nodeList;
    }

}
