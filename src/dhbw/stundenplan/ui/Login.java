package dhbw.stundenplan.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import dhbw.stundenplan.Online;
import dhbw.stundenplan.R;
import dhbw.stundenplan.Settings;
import dhbw.stundenplan.database.TerminDBAdapter;
import dhbw.stundenplan.database.UserDBAdapter;
import dhbw.stundenplan.google.GoogleKalender;
import dhbw.stundenplan.google.OAuthManager;
import dhbw.stundenplan.ui.element.OptionActivity;

/**
 * TestTest
 * 
 * @author DH10HAH
 */
public class Login extends OptionActivity
{
	private Context _Context;
	Button button1, button2;

	// Info zu DUALIS_KALENDER_REGEXP: In der ersten Klammer steht der Tag und
	// in der zweiten Klammer ( Uhrzeit / Ort / Vorlesungsfach )
	public static final String DUALIS_KALENDER_REGEXP = "<div class=\"tbMonthDay\"[^<]+<[^<]+<div class=\"tbsubhead\">[^<]+<a title=\"([^\"]+)\"[^<]+</a>[^<]+</div>[^<]+<div class=\"appMonth\">[^<]+<a title=\"([^\"]+)\"";
	public ProgressDialog progressDialog;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		_Context = this;
		intent.setClass(this, Wochenansicht.class);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_Context);
		Spinner spinner = (Spinner) findViewById(R.id.hochschulauswahl);
		spinner.setSelection(Integer.parseInt(sharedPrefs.getString("lastSpinnerValue", "0")), true);
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox1);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			EditText passwort = (EditText) findViewById(R.id.editText2);

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					int end = passwort.getSelectionStart();
					int start = passwort.getSelectionStart();
					passwort.setTransformationMethod(null);
					passwort.setSelection(start, end);
				}
				else
				{
					int end = passwort.getSelectionStart();
					int start = passwort.getSelectionStart();
					passwort.setTransformationMethod(new PasswordTransformationMethod());
					passwort.setSelection(start, end);
				}

			}

		});
	}

	Online online;

	/**
	 * Login auf dem Dualis Server Wenn keine Vorlesungen vorhanden sind wird
	 * die Datenbank aktualisiert Sonst wird die Vorlesungsansicht geï¿½ffnet
	 * Wenn keine Internetverbindung besteht wird eine Fehlermeldung ausgegeben
	 * und man verbleibt auf dem LoginScreen
	 * 
	 * @param view
	 */
	public void login(final View view)
	{
		final EditText username = (EditText) findViewById(R.id.editText1);
		final EditText passwort = (EditText) findViewById(R.id.editText2);
		Spinner spinner = (Spinner) findViewById(R.id.hochschulauswahl);
		final TerminDBAdapter terminDBAdapter = new TerminDBAdapter(view.getContext()); // Für Kontrolle der Daten
		final UserDBAdapter userDBAdapter = new UserDBAdapter(view.getContext());
		userDBAdapter.newUser(username.getText().toString() + "@" + getResources().getStringArray(R.array.hochschuldomain)[spinner.getSelectedItemPosition()], passwort.getText().toString());
		userDBAdapter.close();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_Context);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("lastSpinnerValue", String.valueOf(spinner.getSelectedItemPosition()));
		editor.commit();
		if (this.checkInternetConnection())
		{
			if (terminDBAdapter.gibDBGroesse() <= 0)
			{
				showDialog("Aktualisiere Termine");
				new DownloadTermine().execute("");

			}
			else
			{
				showDialog("Login");
				new CheckLogin().execute("");

			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
		}
	}

	private void showDialog(String text)
	{
		if (progressDialog == null)
		{
			progressDialog = new ProgressDialog(_Context);
		}
		// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(text);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	private void closeDialog()
	{
		if (progressDialog.isShowing())
		{
			progressDialog.dismiss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.loginmenu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item3:
				intent.setClass(this, Settings.class);
				startActivity(intent);
				break;

		}
		return true;
	}

	/**
	 * Startet einen Ladedialog, und aktualisiert die TerminDB
	 * DownloadTermine().execute("");
	 * 
	 * @author DH10HAH
	 */
	private class DownloadTermine extends AsyncTask<String, Integer, Object>
	{

		private boolean loginKorrekt;

		@Override
		protected Object doInBackground(String... arg)
		{
			Looper.prepare();
			if (checkInternetConnection())
			{

				final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_Context);
				final String anzahlMonate = sharedPrefs.getString("anzahlmonate", "2");

				UserDBAdapter userDBAdapter = new UserDBAdapter(_Context);
				final String password = userDBAdapter.getPassword();
				final String username = userDBAdapter.getUsername();
				userDBAdapter.close();

				Online online = new Online();
				if (online.saveAppointmentToDB(username, password, Integer.parseInt(anzahlMonate), _Context))
				{
					ladeTermineInHash();
					loginKorrekt = true;
				}
				else
				{
					loginKorrekt = false;
				}
			}
			else
			{
				Toast.makeText(_Context, "No Internet Connection", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		public void onPostExecute(Object result)
		{
			if (loginKorrekt)
			{
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_Context);
				if (sharedPreferences.getBoolean("googleCalendarSync", false))
				{
					showDialog("Lade Termine in Googlekalender");

					OAuthManager.getInstance().doLogin(true,(Activity) _Context, new OAuthManager.AuthHandler()
					{

						public void handleAuth(Account accountU, String authTokenU)
						{
							account = accountU;
							authToken = authTokenU;
							new TermineInKalender().execute("");

						}
					});

				}
				else
				{
					intent.setClass(_Context, Wochenansicht.class);
					startActivityForResult(intent, 0);
					finish();
				}
			}
			else
			{

				Toast.makeText(_Context, "Passwort, Username oder DH-Standort falsch. Bitte nocheinmal ï¿½berprï¿½fen.", Toast.LENGTH_LONG).show();
				closeDialog();

			}
		}

	}

	/**
	 * Kontrolliert ob die Logindaten Korrekt sind Wenn sie Korrekt sind wird
	 * die Vorlesungsansicht geï¿½ffnet Wenn sie nicht Korrekt sind wird eine
	 * Fehlermeldung ausgegeben
	 * 
	 * @author DH10HAH
	 */
	private class CheckLogin extends AsyncTask<String, Integer, Object>
	{

		private boolean loginKorrekt;

		@Override
		protected Object doInBackground(String... arg)
		{
			Looper.prepare();
			if (checkInternetConnection())
			{
				UserDBAdapter userDBAdapter = new UserDBAdapter(_Context);
				final String password = userDBAdapter.getPassword();
				final String username = userDBAdapter.getUsername();
				userDBAdapter.close();

				Online online = new Online();
				if (online.login(username, password) != null)
				{
					ladeTermineInHash();
					loginKorrekt = true;
				}
				else
				{
					loginKorrekt = false;
					userDBAdapter.deleteUserDB();
				}
			}
			else
			{
				Toast.makeText(_Context, "No Internet Connection", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		public void onPostExecute(Object result)
		{
			if (loginKorrekt)
			{
				ladeTermineInHash();
				intent.setClass(_Context, Wochenansicht.class);
				startActivity(intent);
				closeDialog();
				finish();
			}
			else
			{
				closeDialog();
				Toast.makeText(_Context, "Passwort, Username oder DH-Standort falsch. Bitte nocheinmal ï¿½berprï¿½fen.", Toast.LENGTH_LONG).show();
			}
		}

	}

	protected Account account;
	protected String authToken;

	ProgressDialog progressBar = null;

	/**
	 * Lï¿½d die Vorlesungstermine in den Google-Kalender
	 * 
	 * @author DH10HAH
	 */
	private class TermineInKalender extends AsyncTask<String, Integer, Object>
	{
		boolean internetConnection;
		GoogleKalender googleKalender;

		@Override
		protected Object doInBackground(String... arg)
		{

			Looper.prepare();
			if (checkInternetConnection())
			{
				googleKalender = new GoogleKalender(_Context);
				googleKalender.writeAppointmentsToGoogleCalendar(account, authToken);// schreibeTermineInKalender();

				internetConnection = true;
			}
			else
			{
				internetConnection = false;
			}
			return null;
		}

		public void onPostExecute(Object result)
		{
			while (!googleKalender.ready)
			{

			}
			if (internetConnection)
			{
				intent.setClass(_Context, Wochenansicht.class);
				startActivityForResult(intent, 0);
				closeDialog();
				finish();
			}
			else
			{
				closeDialog();
				Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
			}
		}
	}
}