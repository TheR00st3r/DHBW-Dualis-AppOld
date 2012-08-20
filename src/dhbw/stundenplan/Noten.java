package dhbw.stundenplan;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import dhbw.stundenplan.database.ResultsDBAdapter;
import dhbw.stundenplan.database.UserDBAdapter;

/**
 * Zeigt provisorisch die Prüfungsergebnisse an
 * 
 * @author DH10HAH
 */
public class Noten extends OptionActivity
{
	public ProgressDialog progressDialog;
	private Context _Context;
	private WebView _WebView;
	private TableLayout _TableLayout;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
		{
			setContentView( R.layout.noten);
			// tv = (TextView)findViewById(R.id.textView2);
			_WebView = (WebView) findViewById(R.id.webView1);
			_Context = this;
			schreibeErgebnisseAlt();
		}
		else
		{
			setContentView(R.layout.notenneu);
			_TableLayout = (TableLayout)findViewById(R.id.notenTabelle);
			_Context = this;
			schreibeErgebnisseNeu();
		}
	}

	/**
	 * Wird von Button aufgerufen und startet das Aktualisieren von Vorlesungen
	 * 
	 * @param view
	 */
	public void aktualisieren(View view)
	{
		progressDialog = new ProgressDialog(_Context);
		// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Aktualisiere Noten");
		progressDialog.setCancelable(false);
		progressDialog.show();
		new DownloadResults().execute("");
	}

	/**
	 * Vorläufig um die Ergebnisse anzuzeigen
	 * Wird bis Gingerbread benutzt.
	 */
	public void schreibeErgebnisseAlt()
	{
		ResultsDBAdapter resultsDBAdapter = new ResultsDBAdapter(_Context);
		Cursor c = resultsDBAdapter.fetchAll();
		if (c.moveToFirst())
		{
			StringBuilder stringBuilder = new StringBuilder();

			while (!c.isLast())
			{
				stringBuilder.append("<tr>" + "<td>" + c.getString(0) + "</td><td>" + c.getString(1) + "</td><td>" + c.getString(4) + "</td><td>" + c.getString(5) + "</td><td>" + c.getString(6) + "</td>" + "</tr>");
				c.moveToNext();
			}
			String data = "<table border=\"1\"><th>Nr.</th><th>Vorlesung</th><th>Note</th><th>ECTS</th><th>Status</th>" + stringBuilder + "</tbale>";

			_WebView.loadData("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>" + data + "</body></html>", "text/html; charset=UTF-8", null);
		}
		else
		{
			_WebView.loadData("Bitte erst die Datenbank Aktualisieren", "text/html", "UTF-8");
		}
		c.close();
		resultsDBAdapter.close();
	}

	/**
	 * Neuere Anzeige für Ergebnisse
	 * wird ab Honeycomb benutzt.
	 */
	public void schreibeErgebnisseNeu()
	{
		ResultsDBAdapter resultsDBAdapter = new ResultsDBAdapter(_Context);
		Cursor c = resultsDBAdapter.fetchAll();
		if (c.moveToFirst())
		{
			while (!c.isLast())
			{
				TableRow tableRow = new TableRow(_Context);
				TextView tv[] = new TextView[7];
				for(int i=0; i<5; i++)
				{
					tv[i] = new TextView(_Context);
					tv[i].setText(c.getString(i));
					tableRow.addView(tv[i]);
				}
				_TableLayout.addView(tableRow);
				c.moveToNext();
			}
		}
		else
		{
			
		}
		c.close();
		resultsDBAdapter.close();
	}
	
	/**
	 * Startet einen Ladedialog, und läd die Ergebnisse herunter
	 */
	private class DownloadResults extends AsyncTask<String, Integer, Object>
	{
		boolean internetConnection;

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
				// publishProgress(50);
				online.ladeResultsInDB(username, password, _Context);
				// publishProgress(100);
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
			if (internetConnection)
			{
				schreibeErgebnisseAlt();
				if(progressDialog.isShowing())
				{
					progressDialog.dismiss();
				}
			}
			else
			{
				if(progressDialog.isShowing())
				{
					progressDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}
}
