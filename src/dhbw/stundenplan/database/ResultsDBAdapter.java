package dhbw.stundenplan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ResultsDBAdapter 
{
	/*
	 * Options:
	 * 1: Prüfungsnummer
	 * 2: Vorlesungsname
	 * 3: Datum
	 * 4. Credits
	 * 5. Note
	 * 6. ECTSnote
	 * 7. Status Englisch
	 * 8. oder 9. Status Deutsch
	 */
	// Database fields
			public static final String KEY_ROWID = "_id";
			public static final String KEY_PRUEFUNGSNUMMER = "pruefungsnummer";
			public static final String KEY_VORLESUNGSNAME = "vorlesungsname";
			public static final String KEY_DATUM = "datum";
			public static final String KEY_CREDITS = "credits";
			public static final String KEY_NOTE = "note";
			public static final String KEY_ECTSNOTE = "ectsnote";
			public static final String KEY_STATUS = "status";
			private static final String DB_TABLE = "Results";
			private Context context;
			private SQLiteDatabase db;
			private ResultsDBHelper dbHelper;

			public ResultsDBAdapter(Context context) 
			{
				this.context = context;
			}

			public ResultsDBAdapter open() throws SQLException 
			{
				dbHelper = new ResultsDBHelper(context);
				db = dbHelper.getWritableDatabase();
				return this;
			}

			public void close() {
				dbHelper.close();
			}

			public void createResult(String pruefungsnummer, String vorlesungsname, String datum, String credits, String note, String ectsnote, String status) 
			{
//				vorlesungsname= vorlesungsname.replace("Ä", "&Auml");
//				vorlesungsname= vorlesungsname.replace("ä", "&auml");
//				vorlesungsname= vorlesungsname.replace("Ö", "&Ouml");
//				vorlesungsname= vorlesungsname.replace("ö", "&ouml");
//				vorlesungsname= vorlesungsname.replace("Ü", "&Uuml");
//				vorlesungsname= vorlesungsname.replace("ü", "&uuml");
//				vorlesungsname= vorlesungsname.replace("ß", "&szlig");
				ContentValues values = createContentValues(pruefungsnummer, vorlesungsname, datum, credits, note, ectsnote, status);
				open();
				db.insert(DB_TABLE, null, values);
				close();
			}

			
			public boolean deleteResult(long rowId) 
			{
				return db.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
			}

			public Cursor fetchResult(int rowId) throws SQLException 
			{
				open();
				String strTmp = "SELECT "
	                    + KEY_PRUEFUNGSNUMMER + ", "
	                    + KEY_VORLESUNGSNAME + ", "
	                    + KEY_DATUM + ", "
	                    + KEY_CREDITS  + ", "
	                    + KEY_NOTE  + ", "
   	                    + KEY_ECTSNOTE + ", "
	                    + KEY_STATUS
	                    + " FROM " + DB_TABLE
	                    + " WHERE " + KEY_ROWID + "=" + rowId;
				Cursor cursor = db.rawQuery(strTmp,null);
				cursor.getCount();
				close();
				
				return cursor;
				
			}
			
			public Cursor fetchAll() throws SQLException
			{
				open();
				String strTmp= "SELECT "
	                    + KEY_PRUEFUNGSNUMMER + ", "
	                    + KEY_VORLESUNGSNAME + ", "
	                    + KEY_DATUM + ", "
	                    + KEY_CREDITS  + ", "
	                    + KEY_NOTE  + ", "
   	                    + KEY_ECTSNOTE + ", "
	                    + KEY_STATUS
	                    + " FROM " + DB_TABLE
	                    + " ORDER BY " + KEY_PRUEFUNGSNUMMER;
				Cursor cursor = db.rawQuery(strTmp,null);
				return cursor;
			}
			
			private ContentValues createContentValues(String pruefungsnummer, String vorlesungsname, String datum, String credits, String note, String ectsnote, String status) 
			{
				ContentValues values = new ContentValues();
				values.put(KEY_PRUEFUNGSNUMMER, pruefungsnummer);
				values.put(KEY_VORLESUNGSNAME, vorlesungsname);
				values.put(KEY_DATUM, datum);
				values.put(KEY_CREDITS, credits);
				values.put(KEY_NOTE, note);
				values.put(KEY_ECTSNOTE, ectsnote);
				values.put(KEY_STATUS, status);
				return values;
			}
			
			public void loescheAlleResults()
			{
				open();
				db.delete(DB_TABLE, null, null);	
				db.delete("sqlite_sequence", "NAME = '" + DB_TABLE + "'", null);
				close();
				
			}
			
			public void loescheDB()
			{
				context.deleteDatabase(DB_TABLE);
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
			
}
