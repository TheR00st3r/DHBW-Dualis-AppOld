package dhbw.stundenplan.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dhbw.stundenplan.R;
import dhbw.stundenplan.database.TerminDBAdapter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;

public class HomeScreenWidget extends AppWidgetProvider
{
	Context context;
	AppWidgetManager appWidgetManager;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		doStuff();

	}

	public void doStuff()
	{
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

		ComponentName watchWidget = new ComponentName(context, HomeScreenWidget.class);

		// remoteViews.setTextViewText(R.id.widgetTV1, "hallo mach was");

		SimpleDateFormat curDate = new SimpleDateFormat("dd.MM.yyyy-hh:mm");
		Calendar cal = Calendar.getInstance();
		cal.setTime(cal.getTime());
		Date datum = cal.getTime();
		String curTime = curDate.format(datum);
		String splitTime[] = curTime.split("-");

		TerminDBAdapter terminDBAdapter = new TerminDBAdapter(context);
		int id = terminDBAdapter.gibID(splitTime[0]);

		// c.getString(0);//0=Datum 1=Startzeit 2=Endzeit 3=Vorlesung 4=Raum
		// 5=Wochentag
		Cursor c = terminDBAdapter.fetchTermineByDatum(splitTime[0]);
		c.moveToFirst();

		while (c.isLast())
		{
			try
			{
				String tmp = c.getString(0) + "-" + c.getString(1);
				Date startzeit = curDate.parse(tmp);
				tmp = c.getString(0) + "-" + c.getString(2);
				Date endzeit = curDate.parse(tmp);

				if (datum.after(startzeit) || datum.before(endzeit))
				{
					// TODO: Aktuelle Vorlesung!!

					// remoteViews.setTextViewText(R.id.widgetTV1,
					// c.getString(0) + "\n" + c.getString(3) + "\n" +
					// c.getString(1) + "-" + c.getString(2) + "\n" +
					// c.getString(4));
					remoteViews.setTextViewText(R.id.textView3, c.getString(1));
					remoteViews.setTextViewText(R.id.textView7, c.getString(2));
					remoteViews.setTextViewText(R.id.textView8, c.getString(3));
					remoteViews.setTextViewText(R.id.textView9, c.getString(4));

					c.moveToLast();
					id = Integer.parseInt(c.getString(6)) + 1;
					boolean textView2 = true;
					do
					{
						try
						{
							c = terminDBAdapter.fetchTermine(id);
							c.moveToFirst();
						}
						catch (Exception e) // TODO: Richtige Exception
											// eintragen
						{
							// TODO: Nachricht keine weitere vorlesung mehr

							// remoteViews.setTextViewText(R.id.widgetTV2,
							// "Keine weiteren Daten vorhanden. Bitte Aktualisieren.");
							textView2 = false;
							break;
						}
						id++;
					} while (c.getString(3).length() < 2);

					if (textView2)
					{
						// TODO: Naechste Vorlesung!!

						// remoteViews.setTextViewText(R.id.widgetTV2,
						// c.getString(0) + "\n" + c.getString(3) + "\n" +
						// c.getString(1) + "-" + c.getString(2) + "\n" +
						// c.getString(4));
					}
					break;
				}
				else
				{
					c.moveToNext();
				}
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		appWidgetManager.updateAppWidget(watchWidget, remoteViews);
	}
}
