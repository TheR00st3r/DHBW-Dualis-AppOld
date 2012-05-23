package dhbw.stundenplan.database;

import dhbw.stundenplan.security.SimpleCrypto;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserDBAdapter {

	// Database fields
			public static final String KEY_ROWID = "_id";
			public static final String KEY_USERNAME = "username";
			public static final String KEY_PASSWORD = "password";
			private static final String DB_TABLE = "User";
			
			private Context context;
			private SQLiteDatabase db;
			private UserDBHelper dbHelper;

			public UserDBAdapter(Context context) 
			{
				this.context = context;
			}

			public UserDBAdapter open() throws SQLException 
			{
				dbHelper = new UserDBHelper(context);
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
			public long newUser(String username, String passwordClear) 
			{
				String passwordCrypted = "";
				try 
				{
					passwordClear= passwordClear.replace("&", "%26");
					passwordCrypted = SimpleCrypto.encrypt("masterpassword", passwordClear);
					open();
					db.delete(DB_TABLE, null, null);	
					db.delete("sqlite_sequence", "NAME = '" + DB_TABLE + "'", null);
					ContentValues values = createContentValues(username, passwordCrypted);
					db.insert(DB_TABLE, null, values);
					close();
				} 
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}

			public void deleteUserDB()
			{
				open();
				db.delete(DB_TABLE, null, null);
				//db.delete("sqlite_sequence", "NAME = '" + DB_TABLE + "'", null);
				close();
			}
			
			
			/**
			 * Return a Cursor positioned at the defined todo
			 */
			public Cursor fetchUser(int rowId) throws SQLException 
			{
				open();
				String strTmp = "SELECT "
	                    + KEY_USERNAME + ", "
	                    + KEY_PASSWORD                    
	                    + " FROM " + DB_TABLE
	                    + " WHERE " + KEY_ROWID + "=" + rowId;
				Cursor cursor = db.rawQuery(strTmp,null);
				cursor.getCount();
				//cursor.close();
				close();
				
				return cursor;
			}
			
			public String getPassword()
			{
				Cursor c = fetchUser(1);
				c.moveToFirst();
				String password = "";
				try 
				{
					password = SimpleCrypto.decrypt("masterpassword", c.getString(1));
				} 
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				c.close();
				//password = password.replace("dasisteinund", "&");
				return password;
			}
		
			public String getUsername()
			{
				Cursor c = fetchUser(1);
				c.moveToFirst();
				String username = c.getString(0);
				c.close();
				return username;
			}
			
			private ContentValues createContentValues(String username, String password) 
			{
				ContentValues values = new ContentValues();
				
				values.put(KEY_USERNAME, username);
				values.put(KEY_PASSWORD, password);
				
				return values;
			}
			
			public void loescheDB()
			{
				context.deleteDatabase(DB_TABLE);
			}
			
		}

