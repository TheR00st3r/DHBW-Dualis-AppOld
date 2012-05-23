package dhbw.stundenplan.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ResultsDB 
{
	private static final String DATABASE_CREATE = "create table Results " + "(_id integer primary key autoincrement, " 
																		+ "pruefungsnummer text not null, " 
																		+ "vorlesungsname text not null, " 
																		+ "datum text not null, " 
																		+ "credits text not null, "	
																		+ "note text not null, " 
																		+ "ectsnote text not null, "
																		+ "status text not null);";

	public static void onCreate(SQLiteDatabase results) 
	{
		results.execSQL(DATABASE_CREATE);
	}
	
	
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) 
	{
		Log.w(TermineDB.class.getName(), "Upgrading database from version "	+ oldVersion + " to " + newVersion	+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
	

}
