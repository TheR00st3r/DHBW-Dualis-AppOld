package dhbw.stundenplan.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserDB
{
	private static final String DATABASE_CREATE = "create table User " + "(_id integer primary key autoincrement, " + "username text not null, " + "password text not null);";

	public static void onCreate(SQLiteDatabase UserDB)
	{
		UserDB.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		Log.w(UserDB.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}

}
