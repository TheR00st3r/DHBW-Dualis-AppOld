package dhbw.stundenplan;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.os.Bundle;
import android.view.MotionEvent;
import dhbw.stundenplan.database.TerminDBAdapter;
import dhbw.stundenplan.database.UserDBAdapter;

/**
 * @author DH10HAH
 */
public class StundenplanAppActivity extends Activity
{
	StundenplanAppActivity sPlashScreen;
	Intent intent;

	/**
	 * The thread to process splash screen events
	 */
	Thread mSplashThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Splash screen view
		setContentView(R.layout.start);

		sPlashScreen = this;

		// The thread to wait for splash screen events
		mSplashThread = new Thread()
		{
			@Override
			public void run()
			{

				try
				{

					UserDBAdapter userDBAdapter = new UserDBAdapter(sPlashScreen);
					TerminDBAdapter terminDBAdapter = new TerminDBAdapter(sPlashScreen);

					@SuppressWarnings("unused")
					String username = userDBAdapter.getUsername(); // Kontrolle
					@SuppressWarnings("unused")
					String password = userDBAdapter.getPassword(); // ob User Vorhanden ist

					Cursor c = terminDBAdapter.fetchTermine(1); // Kontrole ob Termine vorhanden sind.
					int i = c.getCount();
					c.close();
					if (i != 0)
					{
						intent = new Intent();
						intent.setClass(sPlashScreen, Wochenansicht.class);
						ladeTermineInHash();
						synchronized (this)
						{
							try
							{
								wait(1000);
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						startActivity(intent);
						finish();
					}
					else
					{
						Intent intent = new Intent();
						intent.setClass(sPlashScreen, Login.class);
						startActivity(intent);
						finish();
					}
				}
				catch (SQLException se)
				{
					Intent intent = new Intent();
					intent.setClass(sPlashScreen, Login.class);
					startActivity(intent);
					finish();
				}
				catch (CursorIndexOutOfBoundsException ce)
				{
					Intent intent = new Intent();
					intent.setClass(sPlashScreen, Login.class);
					startActivity(intent);
					finish();
				}
				this.interrupt();

			}
		};

		mSplashThread.start();
	}

	/**
	 * Processes splash screen touch events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		if (evt.getAction() == MotionEvent.ACTION_DOWN)
		{
			synchronized (mSplashThread)
			{
				mSplashThread.notifyAll();
			}
		}
		// TODO: Ursprünglich true, false zum Testen ob der Splashscreen damit übersprungen werden kann
		return false;
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
		int i = 1;
		while (i != dbGroesse)
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
			i++;
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