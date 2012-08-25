package dhbw.stundenplan;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.widget.Toast;
import dhbw.stundenplan.database.ResultsDBAdapter;
import dhbw.stundenplan.database.TerminDBAdapter;

/**
 * Bearbeitet alle verbindungen mit dem Dualis system
 * 
 * @author DH10HAH
 */
public class Online
{
	public static final String DUALIS_URL = "https://dualis.dhbw.de/";
	public static final String DUALIS_LOGIN_URL = DUALIS_URL + "scripts/mgrqcgi";
	public static final String DUALIS_COOKIE = "cnsc=" + generateCookie();
	public static final String DUALIS_ARGUMENTE_REGEXP = "ARGUMENTS=([^,]+),";
	public static final String DUALIS_APP_URL = DUALIS_LOGIN_URL + "?APPNAME=CampusNet&PRGNAME=";
	public static final String DUALIS_KALENDAR = "MONTH&ARGUMENTS=";
	public static final String DUALIS_KALENDER_MONATSANSICHT_ARGUMENTS_SUFFIX = ",-N000031,-A";// ",-A,-A,-N1";
	public static final String DUALIS_RESULTS = "STUDENT_RESULT&ARGUMENTS=";
	public static final String DUALIS_RESULTS_SUFFIX = ",-N000310,";
	public static final String HEADER_LOC_NAME = "location";
	public static final String CHARSET = "UTF-8";
	public static final String DUALIS_KALENDER_REGEXP = "<a title=\"([^\"]+)\"";
	public static final String DUALIS_RESULT_REGEXP = "<tr> <td class=\"tbdata\">([^\"]*)</td> <td class=\"tbdata\">([^\"]*)</td> <td class=\"tbdata\" style=\"text-align:right;\">([^\"]*)</td> <td class=\"tbdata\" style=\"text-align:right;\">([^\"]*)</td> <td class=\"tbdata\" style=\"text-align:right;\">([^\"]*)</td> <td class=\"tbdata\" style=\"text-align:center;\">([^\"]*)</td> <td class=\"tbdata\" style=\"text-align:center;\"><img src=\"/img/individual/([^\"]*).gif\" alt=\"([^\"]*)\" title=\"([^\"]*)\" /></td> </tr>";
	private static final int MAX_LOGIN = 5;

	private String _Args;

	/**
	 * Läd Termine aus Dualis in die Datenbank
	 * 
	 * @param username
	 *            Benutzername
	 * @param password
	 *            Passwort
	 * @param months
	 *            Gibt an wieviele Monate heruntergeladen werden sollen
	 * @param context
	 *            Context der Activity auf der Fehler ausgegeben werden sollen
	 * @return
	 */
	public boolean saveAppointmentToDB(String username, String password, int months, Context context)
	{
		int tryNumber = 0;
		while (_Args == null && tryNumber < MAX_LOGIN)
		{
			_Args = login(username, password);
			tryNumber++;
		}
		if (_Args == null)
		{
			Toast.makeText(context, "Login fehlgeschlagen, überprüfen sie ihre Logindaten", Toast.LENGTH_SHORT).show();
			return false;
		} else
		{
			String month = getMonth(_Args, months);
			if (month != null)
			{
				Pattern pattern = Pattern.compile(DUALIS_KALENDER_REGEXP, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(month);
				String date = "";
				String starttime = "";
				String endtime = "";
				String room = "";
				String course = "";
				int id = 1;
				boolean lastWasDate = false;
				TerminDBAdapter terminDBAdapter = new TerminDBAdapter(context);
				terminDBAdapter.loescheAlleTermine();

				while (matcher.find())
				{
					int dataStringLength = matcher.group(1).length();
					if (dataStringLength == 10)
					{
						if (lastWasDate)
						{
							starttime = "";
							endtime = "";
							course = "";
							room = "";
							terminDBAdapter.createTermin(id, date, starttime, endtime, course, room);
							id++;
						}
						date = matcher.group(1);
						lastWasDate = true;
					} else
					{
						String strData[] = matcher.group(1).split("/");
						String strTime[] = strData[0].split("-");
						room = strData[1].trim();
						course = strData[2].trim();
						starttime = strTime[0].trim();
						endtime = strTime[1].trim();
						terminDBAdapter.createTermin(id, date, starttime, endtime, course, room);

						id++;
						lastWasDate = false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Läd die prüfungsergebnise aus dem Dualis in die Datenbank
	 * 
	 * @param username
	 *            Benutzername
	 * @param password
	 *            Password
	 * @param context
	 *            Context der Activity auf der Fehler ausgegeben werden sollen
	 */
	public void saveResultsToDB(String username, String password, Context context)
	{
		int tryNumber = 0;
		while (_Args == null && tryNumber < MAX_LOGIN)
		{
			_Args = login(username, password);
			tryNumber++;
		}
		if (_Args == null)
		{
			Toast.makeText(context, "Login fehlgeschlagen, überprüfen sie ihre Logindaten", Toast.LENGTH_SHORT).show();
			return;
		} else
		{
			String results = getResults(_Args);

			results = results.replaceAll("\\s\\s+", " ");

			Pattern pattern = Pattern.compile(DUALIS_RESULT_REGEXP, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(results);
			/*
			 * Options: 1: Prüfungsnummer 2: Vorlesungsname 3: Datum 4. Credits
			 * 5. Note 6. ECTSnote 7. Status Englisch 8. oder 9. Status Deutsch
			 */

			ResultsDBAdapter resultsDBAdapter = new ResultsDBAdapter(context);
			resultsDBAdapter.loescheDB();
			while (matcher.find())
			{
				String number;
				String course;
				String date;
				String credits;
				String ectsnote;
				String mark;
				String state;

				number = matcher.group(1);
				course = matcher.group(2);
				date = matcher.group(3);
				credits = matcher.group(4).substring(1, 4);
				mark = matcher.group(5);
				ectsnote = matcher.group(6).substring(6);
				state = matcher.group(8);

				if (number == null)
					number = " ";
				if (course == null)
					course = " ";
				if (date == null)
					date = " ";
				if (ectsnote == null)
					ectsnote = " ";
				if (credits == null)
					credits = " ";
				if (mark == null)
					mark = " ";
				if (state == null)
					state = " ";

				resultsDBAdapter.createResult(number, course, date, credits, mark, ectsnote, state);
			}
			resultsDBAdapter.close();
		}
	}

	/**
	 * Gibt die Argumente zurück
	 * 
	 * @return args
	 */
	public String getArgs()
	{
		if (_Args == null)
		{
			_Args = "";
		}
		return _Args;
	}

	/**
	 * Gibt den Header der URLConnection zurück
	 * 
	 * @param connection
	 *            URLConnection
	 * @return header
	 */
	private String getHeader(URLConnection connection)
	{
		String ret = "";
		for (String field : connection.getHeaderFields().keySet())
		{
			ret += field + " : " + connection.getHeaderFields().get(field);
		}
		return ret;
	}

	/**
	 * Methode um die Prüfungsergebnise herunterzuladen
	 * 
	 * @param args
	 *            Argumente der Verbindung
	 * @return Liefert die Ergebnisse in einem String zurück
	 */
	private String getResults(String args)
	{
		return getHTMLPage(DUALIS_APP_URL + DUALIS_RESULTS + args + DUALIS_RESULTS_SUFFIX);
	}

	/**
	 * Liefert die Vorlesungstermine aus dem DualisKalender zurück
	 * 
	 * @param args
	 *            Argumente
	 * @param months
	 *            Gibt an wieviele Monate heruntergeladen werden sollen
	 * @return Liefert die Vorlesungen in einem String
	 */
	private String getMonth(String args, int months)
	{
		String ret = null;
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i <= months; i++)
		{
			String datum = getDate(i - 1);
			getHTMLPage(DUALIS_APP_URL + DUALIS_KALENDAR + args + ",-N000019" + datum + DUALIS_KALENDER_MONATSANSICHT_ARGUMENTS_SUFFIX);
		}
		ret = stringBuilder.toString();
		return ret;
	}

	/**
	 * Läd den HTML code der Seite unter dem angegebenen Link herunter
	 * 
	 * @param url
	 *            Link der zu ladenden Seite
	 * @return HTML Quelltext der Seite
	 */
	private String getHTMLPage(String url)
	{
		String ret = "";
		StringBuilder stringBuilder = new StringBuilder();
		try
		{
			URLConnection connection = connect(url);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				stringBuilder.append(line);
			}
			bufferedReader.close();
		} catch (Exception e)
		{
			ret = null;
		}
		ret = stringBuilder.toString();
		return ret;
	}

	/**
	 * Login-Funktion. Meldet sich per URLConnection am Dualis Server an. Dabei
	 * werden folgende Variablen verwendet: DUALIS_LOGIN_URL --> dies ist die
	 * URL zu der der POST Request geschickt wird DUALIS_COOKIE --> Unser
	 * Dauercookie. Es sieht so aus .... als wäre dualis scheiße!
	 * DUALIS_ARGUMENTE_REGEXP --> dieser Reguläre Ausdruck filter die Argumente
	 * für den Return Wert aus dem Header.
	 * 
	 * @param username
	 *            Benutzername
	 * @param password
	 *            Passwort
	 * @return die Argumente die nun immer im Get HEADER übergeben werden
	 *         müssen, um eingelogt zu bleiben. null, falls login fehlgeschlagen
	 *         hat (kann verschiedene Gründe haben).
	 */
	public String login(String username, String password)
	{
		String cookie = null;

		if (username.contains("loerrach"))
		{
			Map<String, String> data = new HashMap<String, String>();
			data.put("username", username.replace("@dhbw-loerrach.de", ""));
			data.put("password", password);
			try
			{
				cookie = doSubmit("https://portal.dhbw-loerrach.de/cas/login?service=https%3A%2F%2Fportal.dhbw-loerrach.de%2Fc%2Fportal%2Flogin", data);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		} else
		{
			String data = "usrname=" + username + "&pass=" + password + "&APPNAME=CampusNet&PRGNAME=LOGINCHECK&ARGUMENTS=clino%2Cusrname%2Cpass%2Cmenuno%2Cpersno%2Cbrowser%2Cplatform&clino=000000000000001&menuno=000000&persno=00000000&browser=&platform=";

			URLConnection connection;
			try
			{
				connection = connect(DUALIS_LOGIN_URL);
				connection.setDoOutput(true);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
				outputStreamWriter.write(data);
				outputStreamWriter.close();
				String header = getHeader(connection);

				Pattern pattern = Pattern.compile(DUALIS_ARGUMENTE_REGEXP, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(header);
				if (matcher.find())
				{
					cookie = matcher.group(1);
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return cookie; // unser Argument String
	}

	/**
	 * @param surl
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private URLConnection connect(String surl) throws MalformedURLException, IOException
	{
		URL url = new URL(surl);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
		connection.setRequestProperty("User-Agent", "DHBW Dualis App by Andy H. and Michi V.");
		connection.setRequestProperty("Cookie", DUALIS_COOKIE);
		return connection;
	}

	/**
	 * Generiert einen zufälligen cookie
	 * 
	 * @return Cookie
	 */
	private static String generateCookie()
	{
		String cookie = "";
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 16; i++)
		{
			stringBuilder.append(new Double(Math.random() * 99).intValue());
		}
		cookie = stringBuilder.toString();
		return cookie;
	}

	/**
	 * Liefert das Datum datum für den entsprechenden Monat um die richtige
	 * Ansicht im Dualis zu bekommen
	 * 
	 * @param months
	 * @return Gibt das Datum in wieVielMonate zurück
	 */
	private String getDate(int months)
	{
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		Calendar calendar = Calendar.getInstance();
		String currentMonth = month.format(calendar.getTime());
		String curYearStr = year.format(calendar.getTime());
		int currentMonthInt = Integer.parseInt(currentMonth);
		currentMonthInt = currentMonthInt + months;
		if (currentMonthInt > 12)
		{
			currentMonthInt = currentMonthInt - 12;
			int curYearInt = Integer.parseInt(curYearStr);
			curYearInt = curYearInt + 1;
			curYearStr = String.valueOf(curYearInt);
		}
		currentMonth = String.valueOf(currentMonthInt);
		if (currentMonthInt <= 9)
		{
			currentMonth = "0" + currentMonthInt;
		}
		String ret = ",-A01." + currentMonth + "." + curYearStr;
		return ret;
	}

	/**
	 * ----TESTWEISE IMPLEMENTIERT------
	 * 
	 * Login DHBW Lörrach
	 * 
	 * @param url
	 * @param data
	 * @throws Exception
	 */
	public String doSubmit(String url, Map<String, String> data) throws Exception
	{
		URL siteUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
		conn.setRequestProperty("Cookie", "cookiename1=cookievalue1; cookiename2=cookiename2");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		DataOutputStream out = new DataOutputStream(conn.getOutputStream());

		Set<String> keys = data.keySet();
		Iterator<String> keyIter = keys.iterator();
		String content = "";
		for (int i = 0; keyIter.hasNext(); i++)
		{
			Object key = keyIter.next();
			if (i != 0)
			{
				content += "&";
			}
			content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
		}
		System.out.println(content);
		out.writeBytes(content);
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = "";
		while ((line = in.readLine()) != null)
		{
			System.out.println(line);
		}
		in.close();

		return conn.getRequestProperty("Cookie");
	}

}
