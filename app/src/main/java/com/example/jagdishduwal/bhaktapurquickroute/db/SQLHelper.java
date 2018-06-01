package com.example.jagdishduwal.bhaktapurquickroute.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

//http://cariprogram.blogspot.com

public class SQLHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "bqr.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static String DB_PATH = "/data/data/com.example.jagdishduwal.bhaktapurquickroute/databases/";
	//private static String DB_PATH = "/storage/self/primary/databases";

	private Context myContext;

	// Contacts table name
	public static final String TABLE_NODES= "nodes";
	public static final String TABLE_EDGES = "edges";



	public final String COLUMN_LATITUDE = "latitude";
	public final String COLUMN_LONGITUDE = "longitude";

	// Contacts Table Columns names
	public static final String KEY_ID = "id";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";


	public static final String KEY_START_NODE ="start_node";
	public static final String KEY_END_NODE = "end_node";
	public static final String KEY_PATH = "path";
	public static final String KEY_WEIGHT ="weight";
	
	public SQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		myContext=context;
	}
	
	public void createDataBase() throws IOException {

 
    }
	
	private boolean DataBaseisExist(){
    	SQLiteDatabase checkDB = null;
    	try{
    		String myPath = DB_PATH + DATABASE_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	if(checkDB != null )return true ;else return false;

//		File dbFile = myContext.getDatabasePath(DATABASE_NAME);
//		return dbFile.exists();
    }
	
	private void copyDataBase() throws IOException {
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DATABASE_NAME;
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
     	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		if(DataBaseisExist()){
			//do nothing - database already exist
			Toast.makeText(myContext, "Existing Database", Toast.LENGTH_LONG).show();
		}
		else{
			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try {
				copyDataBase();
				Toast.makeText(myContext, "Database Successfully Imported From Assets", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

//	public List<Node> getAllNodes() {
//		List<Node> nodeList = new ArrayList<Node>();
//		// Select All Query
//		String selectQuery = "SELECT  * FROM " + TABLE_NODES;
//
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(selectQuery, null);
//
//		// looping through all rows and adding to list
//		if (cursor.moveToFirst()) {
//			do {
//				Node node = new Node();
//				//  node.setID(Integer.parseInt(cursor.getString(0)));
//				node.setID(cursor.getInt(0));
//				node.setLatitude(cursor.getString(1));
//				node.setLongitude(cursor.getString(2));
//
//
//				// Adding node to list
//				nodeList.add(node);
//			} while (cursor.moveToNext());
//		}
//
//		// return node list
//		return nodeList;
//	}
}