package dhbw.stundenplan.google;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import dhbw.stundenplan.database.TerminDBAdapter;

/**
 * @author DH10HAH
 */
public class GoogleKalender
{
	private Context _Context;

	public GoogleKalender(Context context)
	{
		this._Context = context;
	}

	public Boolean ready = false;

	/**
	 * Schreibt die Termine in den in den Googlekalender
	 * 
	 * @param account Account in welchem die Termine geschrieben werden sollen
	 * @param authToken
	 */
	public void writeAppointmentsToGoogleCalendar(Account account, String authToken)
	{
		ready = false;
		TerminDBAdapter terminDBAdapter = new TerminDBAdapter(_Context);
		Cursor cursor = terminDBAdapter.fetchVorlesungen();
		if (cursor.moveToFirst())
		{
			try
			{
				loescheTermine(account, authToken);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			do
			{
				if (cursor.getString(3).length() > 2)
				{
					System.out.println(cursor.getString(3));
					String place = "Raum: " + cursor.getString(4);

					String[] date = cursor.getString(0).split("\\.");
					String[] startTime = cursor.getString(1).split(":");
					Date startDate = new Date();
					
					// Monat(date) - 1 da sonst falsches ergebnis.
					startDate = new GregorianCalendar(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]), Integer.parseInt(startTime[0]), Integer.parseInt(startTime[1])).getTime();
					Date currentDate = new GregorianCalendar().getTime();
					if (startDate.after(currentDate))
					{
						//										 2 Wochen in ms + 1Woche in ms
						Date lastDate = new Date(currentDate.getTime() + 1209600000 + 1209600000 / 2);

						if (startDate.before(lastDate))
						{
							String[] endTime = cursor.getString(2).split(":");

							Date endDate = new Date();
							
							 // Monat - 1 da sonst falsches ergebnis. 
							endDate = new GregorianCalendar(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]), Integer.parseInt(endTime[0]), Integer.parseInt(endTime[1])).getTime();// (date[2]
							try
							{
								createAppointment(account, authToken, cursor.getString(3), place, startDate, endDate);
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		terminDBAdapter.close();
		ready = true;
	}

	/**
	 * Erstellt einen Termin
	 * 
	 * @param account
	 * @param authToken
	 * @param title
	 * @param place
	 * @param startDate
	 * @param endDate
	 * @throws IOException
	 */
	private void createAppointment(Account account, String authToken, String title, String place, Date startDate, Date endDate) throws IOException
	{
		Calendar calendar = CalendarServiceBuilder.build(authToken);
		Event event = new Event();

		event.setSummary("Vorlesung: " + title);
		event.setLocation(place);

		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));

		//Event createdEvent = calendar.events().insert(gibDHBWKalenderID(), event).execute();
		calendar.events().insert(gibDHBWKalenderID(), event).execute();
	}

	/**
	 * Läd alle Verfügbaren Kalender in dem verbundenen Google Account
	 * 
	 * @return
	 * @throws IOException
	 */
	private String gibDHBWKalenderID() throws IOException
	{
		Calendar service = CalendarServiceBuilder.build(OAuthManager.getInstance().getAuthToken());
		
		//* Listet alle Verfügbaren Kalender auf
		com.google.api.services.calendar.model.CalendarList calendarList = service.calendarList().list().execute();
		while (true)
		{
			for (CalendarListEntry calendarListEntry : calendarList.getItems())
			{
				if (calendarListEntry.getSummary().contains("DHBW Dualis"))
				{
					return calendarListEntry.getId();
				}
			}
			return createCalendar("DHBW Dualis");
		}
	}

	/**
	 * Erstellt einen neuen Kalender
	 * 
	 * @param calendarTitle Name des neuerstelleten Kalenders
	 * @return
	 * @throws IOException
	 */
	private String createCalendar(String calendarTitle) throws IOException
	{
		Calendar service = CalendarServiceBuilder.build(OAuthManager.getInstance().getAuthToken());
		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();

		calendar.setSummary(calendarTitle);
		calendar.setTimeZone("Europe/Berlin");

		com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();

		return createdCalendar.getId();
	}

	/**
	 * Löscht alle Termine mit einem Titel der "Vorlesung" enthält
	 * @param account
	 * @param authToken
	 * @throws IOException
	 */
	private void loescheTermine(Account account, String authToken) throws IOException
	{
		String dhbwCalendarID = gibDHBWKalenderID();
		Calendar service = CalendarServiceBuilder.build(OAuthManager.getInstance().getAuthToken());
		Events events = service.events().list(dhbwCalendarID).execute();
		Date curDate = new GregorianCalendar().getTime();
		if (events.getItems() != null)
		{
			for (Event event : events.getItems())
			{
				if (curDate.before(new Date(event.getStart().getDateTime().getValue())))
				{
					if (event.getSummary().contains("Vorlesung"))
					{
						service.events().delete(dhbwCalendarID, event.getId()).execute();
					}
				}
			}
		}
	}
}
