package dhbw.stundenplan.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import dhbw.stundenplan.Online;
import dhbw.stundenplan.R;
import dhbw.stundenplan.database.ResultsDBAdapter;
import dhbw.stundenplan.database.UserDBAdapter;
import dhbw.stundenplan.ui.element.OptionActivity;
import dhbw.stundenplan.ui.element.TableCell;

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
	private Boolean _Neu;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
		{
			setContentView( R.layout.noten);
			// tv = (TextView)findViewById(R.id.textView2);
			_WebView = (WebView) findViewById(R.id.webView1);
			_Context = this;
			_Neu = false;
			schreibeErgebnisseAlt();
		}
		else
		{
			setContentView(R.layout.notenneu);
			_TableLayout = (TableLayout)findViewById(R.id.notenTabelle);
			_Context = this;
			_Neu = true;
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
		_TableLayout.removeAllViews();
		ResultsDBAdapter resultsDBAdapter = new ResultsDBAdapter(_Context);
		
		TableRow tableHead = new TableRow(_Context);
		for(String tableHeadStr : getResources().getStringArray(R.array.notenTableHead))
		{
			TableCell tableCellHead = new TableCell(_Context);
			tableCellHead.setPadding(8, 8, 8, 8);
			tableCellHead.setTextAppearance(_Context, android.R.style.TextAppearance_Medium);
			tableCellHead.setTextColor(Color.BLACK);
			tableCellHead.setText(tableHeadStr);
			
			
			tableHead.addView(tableCellHead);
		}
		_TableLayout.addView(tableHead);
		
		Cursor c = resultsDBAdapter.fetchAll();
		if (c.moveToFirst())
		{
			
			while (!c.isLast())
			{
				TableRow tableRow = new TableRow(_Context);
				TableCell tv[] = new TableCell[7];
				for(int i=1; i<=4; i++)
				{
					if(i!=2)
					{
						tv[i] = new TableCell(_Context);
						
						tv[i].setPadding(5, 5, 5, 5);
						tv[i].setTextColor(Color.BLACK);
						String data = c.getString(i);
						if(data.toLowerCase().contains("nbs"))
						{
							data="";
						}
						tv[i].setText(data);
						tableRow.addView(tv[i]);
					}
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
				online.saveResultsToDB(username, password, _Context);
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
				if(_Neu)
				{
					schreibeErgebnisseNeu();
				}
				else
				{
					schreibeErgebnisseAlt();
				}
				
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
