package dhbw.stundenplan.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TermineDB
{
	private static final String DATABASE_CREATE = "create table termine " + "(_id integer primary key autoincrement, " + "id integer not null, " + "datum text not null, " + "startzeit text not null, " + "endzeit text not null, " + "vorlesung text not null, " + "raum text not null, " + "wochentag text not null);";

	public static void onCreate(SQLiteDatabase termine)
	{
		termine.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		Log.w(TermineDB.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}

}
