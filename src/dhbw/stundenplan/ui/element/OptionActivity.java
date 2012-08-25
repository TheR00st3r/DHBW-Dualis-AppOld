package dhbw.stundenplan.ui.element;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import dhbw.stundenplan.R;
import dhbw.stundenplan.database.TerminDBAdapter;

/**
 * Activity mit einem Optionsmenü
 * 
 * @author DH10HAH
 */
public abstract class OptionActivity extends Activity
{
	protected Intent intent = new Intent();
	protected Context _Context;


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Kontrolliert ob eine Internetverbindung besteht
	 * 
	 * @return Liefert true, wenn eine Verbindung besteht. Wenn nicht dann
	 *         flase.
	 */
	public boolean checkInternetConnection()
	{
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Läd die Termine in eine HashMap zum Übergeben der Termine von einer in
	 * die andere Activity Schnellerer zugriff auf Termine
	 * 
	 * @return
	 */
	public boolean ladeTermineInHash()
	{
		TerminDBAdapter terminDBAdapter = new TerminDBAdapter(this);

		HashMap<String, String> vorlesung = new HashMap<String, String>();
		HashMap<String, String> datum = new HashMap<String, String>();
		HashMap<String, String> startzeit = new HashMap<String, String>();
		HashMap<String, String> endzeit = new HashMap<String, String>();
		HashMap<String, String> raum = new HashMap<String, String>();
		HashMap<String, String> wochentag = new HashMap<String, String>();

		int dbGroesse = terminDBAdapter.gibDBGroesse();
		
		for(int i = 1;i != dbGroesse;i++)
		{
			Cursor c = terminDBAdapter.fetchTermineComplete(i);
			if (c.moveToFirst())
			{
				vorlesung.put(c.getString(0), c.getString(4));
				datum.put(c.getString(0), c.getString(1));
				startzeit.put(c.getString(0), c.getString(2));
				endzeit.put(c.getString(0), c.getString(3));
				raum.put(c.getString(0), c.getString(5));
				wochentag.put(c.getString(0), c.getString(6));
			}
		}

		terminDBAdapter.close();

		intent.putExtra("vorlesung", vorlesung);
		intent.putExtra("datum", datum);
		intent.putExtra("startzeit", startzeit);
		intent.putExtra("endzeit", endzeit);
		intent.putExtra("raum", raum);
		intent.putExtra("wochentag", wochentag);

		return true;
	}
}
