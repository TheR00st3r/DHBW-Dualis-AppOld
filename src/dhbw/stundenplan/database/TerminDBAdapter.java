package dhbw.stundenplan.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TerminDBAdapter
{
	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ROWID2 = "id";
	public static final String KEY_DATUM = "datum";
	public static final String KEY_STARTZEIT = "startzeit";
	public static final String KEY_ENDZEIT = "endzeit";
	public static final String KEY_VORLESUNG = "vorlesung";
	public static final String KEY_RAUM = "raum";
	public static final String KEY_WOCHENTAG = "wochentag";
	private static final String DB_TABLE = "termine";
	private Context context;
	private SQLiteDatabase db;
	private TerminDBHelper dbHelper;

	public TerminDBAdapter(Context context)
	{
		this.context = context;
	}

	public TerminDBAdapter open() throws SQLException
	{
		dbHelper = new TerminDBHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		dbHelper.close();
	}

	
	public void createTermin(int id, String datum, String startzeit, String endzeit, String vorlesung, String raum)
	{
		// vorlesung= vorlesung.replace("Ä", "\u00C4");
		// vorlesung= vorlesung.replace("ä", "\u00E4");
		// vorlesung= vorlesung.replace("Ö", "\u00D6");
		// vorlesung= vorlesung.replace("ö", "\u00F6");
		// vorlesung= vorlesung.replace("Ü", "\u00DC");
		// vorlesung= vorlesung.replace("ü", "\u00FC");
		// vorlesung= vorlesung.replace("ß", "\u00DF");
		int wochentag = ermittleWochentag(datum);
		ContentValues values = createContentValues(id, datum, startzeit, endzeit, vorlesung, raum, String.valueOf(wochentag));
		open();
		db.insert(DB_TABLE, null, values);
		close();
	}

	/**
	 * Ermittelt aus einem Datum(String) den entsprechenden Wochentag. Der Tag
	 * wird als int zurückgegeben 0: Fehler 1: Montag 2: Dienstag 3: Mittwoch 4:
	 * Donnerstag 5: Freitag 6: Samstag 7: Sonntag
	 */

	/**
	 * Ermittelt aus einem Datum(String) den entsprechenden Wochentag. Der Tag
	 * wird als int zurückgegeben 0: Fehler 1: Montag 2: Dienstag 3: Mittwoch 4:
	 * Donnerstag 5: Freitag 6: Samstag 7: Sonntag
	 * 
	 * @param checkDatum
	 *            Datum welches auf seinen Wochentag kontrolliert werden soll
	 * @return Liefert den etnsprechenden Wochentag als int zurück
	 */
	public int ermittleWochentag(String checkDatum)
	{
		String datumStr = "";
		int datumInt = 0;
		try
		{
			Date datum = new SimpleDateFormat("dd.MM.yyyy").parse(checkDatum);
			datumStr = datum.toString();
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (datumStr.contains("Mon"))
		{
			datumInt = 1;
		} else
		{
			if (datumStr.contains("Tue"))
			{
				datumInt = 2;
			} else
			{
				if (datumStr.contains("Wed"))
				{
					datumInt = 3;
				} else
				{
					if (datumStr.contains("Thu"))
					{
						datumInt = 4;
					} else
					{
						if (datumStr.contains("Fri"))
						{
							datumInt = 5;
						} else
						{
							if (datumStr.contains("Sat"))
							{
								datumInt = 6;
							} else
							{
								if (datumStr.contains("Sun"))
									datumInt = 7;
							}
						}
					}
				}
			}
		}
		return datumInt;

	}
	
	/*
	 * public boolean updateTermin(long rowId, int id, String datum, String
	 * startzeit, String endzeit, String vorlesung, String raum) { ContentValues
	 * values = createContentValues(id, datum, startzeit, endzeit, vorlesung,
	 * raum);
	 * 
	 * return db.update(DB_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0; }
	 */


	public boolean deleteTermin(long rowId)
	{
		return db.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all todo in the database
	 * 
	 * @return Cursor over all notes
	 */
	/*
	 * public Cursor fetchAllTermine() { //db.q open(); String strTmp =
	 * "SELECT " + KEY_DATUM+", " + KEY_STARTZEIT+", " + KEY_ENDZEIT+", " +
	 * KEY_VORLESUNG+", " + KEY_RAUM + " FROM " + DB_TABLE; Cursor cursor =
	 * db.rawQuery(strTmp,null); //Cursor cursor = db.query(DB_TABLE, new
	 * String[] {KEY_DATUM, KEY_STARTZEIT, KEY_ENDZEIT, KEY_VORLESUNG, KEY_RAUM
	 * }, null, null, null, null, null, null); close(); return cursor; }
	 */

	/**
	 * Return a Cursor positioned at the defined todo
	 */
	public Cursor fetchTermine(int rowId) throws SQLException
	{
		open();
		String strTmp = "SELECT " + KEY_DATUM + ", " + KEY_STARTZEIT + ", " + KEY_ENDZEIT + ", " + KEY_VORLESUNG + ", " + KEY_RAUM + ", " + KEY_WOCHENTAG + " FROM " + DB_TABLE + " WHERE " + KEY_ROWID + "=" + rowId;
		Cursor cursor = db.rawQuery(strTmp, null);
		cursor.getCount();
		close();

		return cursor;

	}

	public Cursor fetchVorlesungen() throws SQLException
	{
		open();
		String strTmp = "SELECT " + KEY_DATUM + ", " + KEY_STARTZEIT + ", " + KEY_ENDZEIT + ", " + KEY_VORLESUNG + ", " + KEY_RAUM + ", " + KEY_WOCHENTAG + " FROM " + DB_TABLE;
		Cursor cursor = db.rawQuery(strTmp, null);
		cursor.getCount();
		close();

		return cursor;

	}

	public Cursor fetchTermineByDatum(String datum) throws SQLException
	{
		String[] datumArray =
		{ datum };

		open();
		String strTmp = "SELECT " + KEY_DATUM + ", " + KEY_STARTZEIT + ", " + KEY_ENDZEIT + ", " + KEY_VORLESUNG + ", " + KEY_RAUM + ", " + KEY_WOCHENTAG + ", " + KEY_ROWID2 + " FROM " + DB_TABLE + " WHERE " + KEY_DATUM + "=?";
		Cursor cursor = db.rawQuery(strTmp, datumArray);
		cursor.getCount();
		close();

		return cursor;

	}

	/**
	 * Return a Cursor positioned at the defined todo
	 */
	public Cursor fetchTermineComplete(int rowId) throws SQLException
	{
		open();
		String strTmp = "SELECT " + KEY_ROWID2 + ", " + KEY_DATUM + ", " + KEY_STARTZEIT + ", " + KEY_ENDZEIT + ", " + KEY_VORLESUNG + ", " + KEY_RAUM + ", " + KEY_WOCHENTAG + " FROM " + DB_TABLE + " WHERE " + KEY_ROWID + "=" + rowId;
		Cursor cursor = db.rawQuery(strTmp, null);
		cursor.getCount();
		close();

		return cursor;

	}

	private ContentValues createContentValues(int id, String datum, String startzeit, String endzeit, String vorlesung, String raum, String wochentag)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ROWID2, id);
		values.put(KEY_DATUM, datum);
		values.put(KEY_STARTZEIT, startzeit);
		values.put(KEY_ENDZEIT, endzeit);
		values.put(KEY_VORLESUNG, vorlesung);
		values.put(KEY_RAUM, raum);
		values.put(KEY_WOCHENTAG, wochentag);
		return values;
	}

	public void loescheAlleTermine()
	{
		open();
		db.delete(DB_TABLE, null, null);
		db.delete("sqlite_sequence", "NAME = '" + DB_TABLE + "'", null);
		close();

	}

	public void schreibeTermineInDB(String termine)
	{
		int id = 1;
		dbHelper = new TerminDBHelper(context);
		db = dbHelper.getWritableDatabase();
		String rauteGetrennt[];
		String datum = "";
		String startzeit = "";
		String endzeit = "";
		String raum = "";
		String vorlesung = "";

		rauteGetrennt = termine.split("#");
		int i = 0;
		int x = rauteGetrennt.length;
		while (i < x)
		{
			// SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			// Date parsed = sdf.parse(rauteGetrennt[i], new ParsePosition(0));
			// String test = sdf.format(parsed);

			if (rauteGetrennt[i].length() == 10)
			{
				datum = rauteGetrennt[i];
				int z = i + 1;
				if (z < x && rauteGetrennt[z].length() == 10)
				{
					startzeit = "";
					endzeit = "";
					vorlesung = "";
					raum = "";
					createTermin(id, datum, startzeit, endzeit, vorlesung, raum);
					id++;
				}
			} else
			{
				String str[] = rauteGetrennt[i].split(" / ");
				String str2[] = str[0].split(" - ");
				startzeit = str2[0];
				endzeit = str2[1];
				raum = str[1];
				vorlesung = str[2];
				createTermin(id, datum, startzeit, endzeit, vorlesung, raum);
				id++;
			}

			i++;
		}

		close();
	}

	public int gibDBGroesse()
	{
		open();
		String count = "SELECT count(*) FROM " + DB_TABLE;
		Cursor cursor = db.rawQuery(count, null);
		cursor.moveToFirst();
		int countInt = cursor.getInt(0);
		close();
		return countInt;
	}

	public int gibID(String datum)
	{
		int id;
		open();
		try
		{
			String strTmp = "SELECT " + KEY_ROWID2 + " FROM " + DB_TABLE + " WHERE " + KEY_DATUM + " = ?";
			Cursor c = db.rawQuery(strTmp, new String[]
			{ datum });
			c.moveToFirst();
			id = c.getInt(0);
		} catch (Exception e)
		{
			id = 1;
		}
		close();

		return id;
	}

	public void loescheDB()
	{
		context.deleteDatabase(DB_TABLE);
	}
}
