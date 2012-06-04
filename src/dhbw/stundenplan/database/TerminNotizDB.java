package dhbw.stundenplan.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TerminNotizDB
{

	private static final String DATABASE_CREATE = "create table TerminNotiz " + "(_id integer primary key autoincrement, " + "vorlesung text not null, " + "notiz text not null);";

	public static void onCreate(SQLiteDatabase TerminNotizDB)
	{
		TerminNotizDB.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		Log.w(TerminNotizDB.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
}
