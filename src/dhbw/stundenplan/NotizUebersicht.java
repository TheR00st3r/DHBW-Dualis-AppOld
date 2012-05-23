package dhbw.stundenplan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import dhbw.stundenplan.database.TerminNotizDBAdapter;

/**
 * Zeigt Notizüberischt an
 * 
 * @author DH10HAH
 *
 */
public class NotizUebersicht extends Activity
{
	Intent intent = new Intent();
	final private int anzahlMoeglicherTextViews = 1000;
	final TextView[] tvNotizen = new TextView[this.anzahlMoeglicherTextViews];
	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		super.setTheme(R.style.Theme_Notizen);
		setContentView(R.layout.popup_layout_notizuebersicht);
		context = this;

		TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
		Cursor c = terminNotizDBAdapter.fetchAll();

		int i = 1;
		LinearLayout ll = (LinearLayout) findViewById(R.id.notizContainer);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 8, 0, 6);
		params2.setMargins(0, 0, 0, 0);

		if (c.moveToFirst())
		{
			for (int y = 0; y < c.getCount(); y++)
			{
				String str = c.getString(0);
				String str2 = c.getString(1);
				if (str2.length() > 2)
				{
					tvNotizen[i] = new TextView(context);
					tvNotizen[i].setText(str);
					tvNotizen[i].setTextSize(22);
					tvNotizen[i].setPadding(10, 0, 0, -2);
					tvNotizen[i].setLayoutParams(params);
					tvNotizen[i].setTypeface(Typeface.SERIF, Typeface.BOLD);
					tvNotizen[i].setTextColor(Color.BLACK);
					tvNotizen[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.background_notiz));
					ll.addView(tvNotizen[i]);
					final int x1 = i;
					tvNotizen[i].setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							Bundle bundle = new Bundle();
							;
							bundle.putString("daten", tvNotizen[x1].getText().toString());
							intent.putExtras(bundle);
							intent.setClass(context, Notiz.class);
							startActivity(intent);
							finish();
						}
					});
					i++;
					
					tvNotizen[i] = new TextView(context);
					tvNotizen[i].setText(str2);
					tvNotizen[i].setTextSize(18);
					tvNotizen[i].setMaxLines(3);
					tvNotizen[i].setPadding(10, 0, 25, 10);
					tvNotizen[i].setTextColor(Color.BLACK);
					tvNotizen[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.background_repeat));
					tvNotizen[i].setLayoutParams(params2);
					ll.addView(tvNotizen[i]);
					tvNotizen[i].setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							String str = tvNotizen[x1].getText().toString();
							intent.putExtra("daten", str);
							intent.setClass(context, Notiz.class);
							startActivity(intent);
							finish();
						}
					});

					i++;
				}
				c.moveToNext();
			}
			if (tvNotizen[1] == null)
			{
				this.finish();// TODO: Funktioniert dass so?

				Toast.makeText(getApplicationContext(), "Keine Notizen vorhanden. Klicke eine Vorlesung an um eine Notiz zu erstellen", Toast.LENGTH_LONG).show();
			}
		} else
		{
			this.finish();// TODO: Funktioniert dass so?
			Toast.makeText(getApplicationContext(), "Keine Notizen vorhanden. Klicke eine Vorlesung an um eine Notiz zu erstellen", Toast.LENGTH_LONG).show();
		}
		c.close();
		tvNotizen[0] = new TextView(context);
		Display display = getWindowManager().getDefaultDisplay();
		tvNotizen[0].setMinHeight(display.getHeight());
		tvNotizen[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.background_repeat));
		ll.addView(tvNotizen[0]);
	}
}
