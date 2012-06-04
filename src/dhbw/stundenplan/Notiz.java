package dhbw.stundenplan;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import dhbw.stundenplan.database.TerminNotizDBAdapter;

/**
 * Zeigt Notizen an
 * 
 * @author DH10HAH
 */
public class Notiz extends Activity
{
	Context context;
	private EditText editText;
	private TextView textViewVorlesung;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_layout_notiz);
		context = this;

		final String daten = getIntent().getStringExtra("daten");

		Display display = getWindowManager().getDefaultDisplay();

		EditText editTextNotiz = (EditText) findViewById(R.id.editTextNotiz);
		editTextNotiz.setMinHeight(display.getHeight());

		Button speichern = (Button) findViewById(R.id.end_data_send_button);
		textViewVorlesung = (TextView) findViewById(R.id.textViewNotiz);
		editText = (EditText) findViewById(R.id.editTextNotiz);
		if (daten.contains("\n"))
		{
			final String str[] = daten.toString().split("\n");

			textViewVorlesung.setText(str[0]);
			try
			{
				TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
				String vorlesungStr = str[0].toString();
				Cursor c = terminNotizDBAdapter.fetchTerminNotiz(vorlesungStr);
				c.moveToFirst();
				editText.setText(c.getString(1));
				c.close();
				terminNotizDBAdapter.close();
			}
			catch (SQLException se)
			{
				editText.setText("");
			}
			catch (CursorIndexOutOfBoundsException ce)
			{
				editText.setText("");
			}
			speichern.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					try
					{
						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
						if (!(terminNotizDBAdapter.updateTerminNotiz(str[0], editText.getText().toString()))) // Macht
																												// update
																												// und
																												// liefert
																												// true
																												// falls
																												// der
																												// Vorgang
																												// scheitert
						{
							terminNotizDBAdapter.createTerminNotiz(str[0], editText.getText().toString());
						}
						terminNotizDBAdapter.close();
					}
					catch (SQLException se)
					{
						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
						terminNotizDBAdapter.createTerminNotiz(str[0], editText.getText().toString());
						terminNotizDBAdapter.close();
					}

					finish();
				}
			});
		}
		else
		{
			textViewVorlesung.setText(daten);
			try
			{
				TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
				Cursor c = terminNotizDBAdapter.fetchTerminNotiz(daten.toString());
				c.moveToFirst();
				editText.setText(c.getString(1));
				c.close();
				terminNotizDBAdapter.close();
			}
			catch (SQLException se)
			{
				editText.setText("");
			}
			catch (CursorIndexOutOfBoundsException ce)
			{
				editText.setText("");
			}

			editText = (EditText) findViewById(R.id.editTextNotiz);

			speichern.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					try
					{
						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
						if (!(terminNotizDBAdapter.updateTerminNotiz(daten, editText.getText().toString()))) // Macht
																												// update
																												// und
																												// liefert
																												// true
																												// falls
																												// der
																												// Vorgang
																												// scheitert
						{
							terminNotizDBAdapter.createTerminNotiz(daten, editText.getText().toString());
						}
						terminNotizDBAdapter.close();
					}
					catch (SQLException se)
					{
						TerminNotizDBAdapter terminNotizDBAdapter = new TerminNotizDBAdapter(context);
						terminNotizDBAdapter.createTerminNotiz(daten, editText.getText().toString());
						terminNotizDBAdapter.close();
					}

					finish();
				}
			});

		}

		Button cancel = (Button) findViewById(R.id.button1);

		cancel.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});

	}
}
