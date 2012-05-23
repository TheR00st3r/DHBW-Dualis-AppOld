package dhbw.stundenplan.database;




import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

public class TerminFarbeDBAdapter 
{
	// Database fields
		public static final String KEY_ROWID = "_id";
		public static final String KEY_VORLESUNG = "vorlesung";
		public static final String KEY_FARBE = "farbe";
		private static final String DB_TABLE = "terminFarbe";
		//private static final String KEY_StandardFarbe = "default";
		//private static final String StandardFarbe = "#FF000000";
		private Context context;
		private SQLiteDatabase db;
		private TerminFarbeDBHelper dbHelper;

		public TerminFarbeDBAdapter(Context context) 
		{
			this.context = context;
			//this.addVorlesungsFarbe(KEY_StandardFarbe, StandardFarbe);
		}

		public TerminFarbeDBAdapter open() throws SQLException 
		{
			dbHelper = new TerminFarbeDBHelper(context);
			db = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() 
		{
			dbHelper.close();
		}

		/**
		 * Create a new todo If the todo is successfully created return the new
		 * rowId for that note, otherwise return a -1 to indicate failure.
		 */
		public long addVorlesungsFarbe(String vorlesung, String farbe) 
		{
			open();
			ContentValues values = createContentValues(vorlesung, farbe);
			db.insert(DB_TABLE, null, values);
			close();
			return 0;
		}

		/**
		 * Update the todo
		 */
		public boolean updateVorlesungsFarbe(long rowId, String vorlesung, String farbe) 
		{
			ContentValues values = createContentValues(vorlesung, farbe);

			return db.update(DB_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
		}

		/**
		 * Deletes todo
		 */
		public boolean deleteVorlesungsFarbe(long rowId) 
		{
			return db.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
		}

		
		/**
		 * Return a Cursor positioned at the defined todo
		 */
		public Cursor fetchVorlesungsFarbe(int rowId) throws SQLException 
		{
			open();
			String strTmp = "SELECT "
                    + KEY_VORLESUNG + ", "
                    + KEY_FARBE                    
                    + " FROM " + DB_TABLE
                    + " WHERE " + KEY_ROWID + "=" + rowId;
			Cursor cursor = db.rawQuery(strTmp,null);
			cursor.getCount();
			close();
			
			return cursor;
		}
		
		public int gibVorelsungsFarbe(String vorlesung)
		{
			int farbe = 0 ;
			
			
			open();
			try
			{
				String strTmp = "SELECT "
						+ KEY_FARBE
						+ " FROM " + DB_TABLE
						+ " WHERE " + KEY_VORLESUNG + "=?";// + vorlesung; //TODO
				String[]  selectionargs = { vorlesung };
				Cursor cursor = db.rawQuery(strTmp, selectionargs);
				if(cursor.moveToFirst())
				{
					farbe = Integer.parseInt(cursor.getString(0));
				}
				cursor.close();
			}
			catch (SQLException se) 
			{
			}
			if (farbe == 0)
			{
				farbe = neueFarbe();
				addVorlesungsFarbe(vorlesung, String.valueOf(farbe));
			}
			
			close();
			return  farbe;
		}

		private ContentValues createContentValues(String vorlesung, String farbe) 
		{
			ContentValues values = new ContentValues();
			
			values.put(KEY_VORLESUNG, vorlesung);
			values.put(KEY_FARBE, farbe);
			return values;
		}
		
		
		private int neueFarbe()
		{
			int alpha = 255;
			int rot = 255;
			int gruen = 30;
			int blau = 30;
			
			int db;
			
			try
			{
				db = gibDBGroesse();
			}
			catch (SQLException se)
			{
				db = 0;
			}
			/*rot = rot + 20 + (31 * db);
			while (rot > 255)
			{
				rot = rot - 255;
			}*/
			gruen = gruen + 30 + (37 * db);
			while (gruen > 255)
			{
				gruen = gruen - 255;
			}
			blau = blau + 10 + (43 * db);
			while (blau > 255)
			{
				blau = blau - 255;
			}
			
			int farbe = Color.argb(alpha, rot, gruen, blau);
			return farbe;
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
		
		public void loescheDB()
		{
			context.deleteDatabase(DB_TABLE);
		}
	}
