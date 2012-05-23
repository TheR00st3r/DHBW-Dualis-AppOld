package dhbw.stundenplan.database;



import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



public class TerminFarbeDB
{
	private static final String DATABASE_CREATE = "create table terminFarbe " + "(_id integer primary key autoincrement, " + "vorlesung text not null, " + "farbe text not null);";

	public static void onCreate(SQLiteDatabase terminFarbe) 
	{
		terminFarbe.execSQL(DATABASE_CREATE);
	}
	
	
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) 
	{
		Log.w(TerminFarbeDB.class.getName(), "Upgrading database from version "	+ oldVersion + " to " + newVersion	+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
	
	
	
	
	
}

