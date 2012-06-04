package dhbw.stundenplan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TerminNotizDBAdapter
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_VORLESUNG = "vorlesung";
	public static final String KEY_NOTIZ = "notiz";
	private static final String DB_TABLE = "TerminNotiz";

	private Context context;
	private SQLiteDatabase db;
	private TerminNotizDBHelper dbHelper;

	public TerminNotizDBAdapter(Context context)
	{
		this.context = context;
	}

	public TerminNotizDBAdapter open() throws SQLException
	{
		dbHelper = new TerminNotizDBHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		dbHelper.close();
	}

	public boolean updateTerminNotiz(String vorlesung, String notiz)
	{
		String[] str = { vorlesung };

		open();
		ContentValues values = createContentValues(vorlesung, notiz);
		// insert(DB_TABLE, null, values);
		boolean zurueck = (db.update(DB_TABLE, values, KEY_VORLESUNG + "=?", str)) > 0;
		close();
		return zurueck;
	}

	public long createTerminNotiz(String vorlesung, String notiz)
	{

		open();
		ContentValues values = createContentValues(vorlesung, notiz);
		db.insert(DB_TABLE, null, values);// insert(DB_TABLE, null, values);
		return 0;
	}

	public void deleteTerminNotizDB()
	{
		open();
		db.delete(DB_TABLE, null, null);
		// db.delete("sqlite_sequence", "NAME = '" + DB_TABLE + "'", null);
		close();
	}

	public Cursor fetchAll() throws SQLException
	{
		open();
		String strTmp = "SELECT " + KEY_VORLESUNG + ", " + KEY_NOTIZ + " FROM " + DB_TABLE;
		Cursor cursor = db.rawQuery(strTmp, null);
		cursor.getCount();
		close();

		return cursor;
	}

	public Cursor fetchTerminNotiz(int rowId) throws SQLException
	{
		open();
		String strTmp = "SELECT " + KEY_VORLESUNG + ", " + KEY_NOTIZ + " FROM " + DB_TABLE + " WHERE " + KEY_ROWID + "=" + rowId;
		Cursor cursor = db.rawQuery(strTmp, null);
		cursor.getCount();
		// cursor.close();
		close();

		return cursor;
	}

	public Cursor fetchTerminNotiz(String vorlesung) throws SQLException
	{
		open();
		String strTmp = "SELECT " + KEY_VORLESUNG + ", " + KEY_NOTIZ + " FROM " + DB_TABLE + " WHERE " + KEY_VORLESUNG + "=?";
		String[] selectionargs = { vorlesung };
		Cursor cursor = db.rawQuery(strTmp, selectionargs);
		cursor.getCount();
		// cursor.close();
		close();

		return cursor;
	}

	private ContentValues createContentValues(String vorlesung, String notiz)
	{
		ContentValues values = new ContentValues();

		values.put(KEY_VORLESUNG, vorlesung);
		values.put(KEY_NOTIZ, notiz);

		return values;
	}

	public void loescheDB()
	{
		context.deleteDatabase(DB_TABLE);
	}
}
