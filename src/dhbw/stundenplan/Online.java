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

import android.app.Activity;
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
	private static final int ANZAHLLOGINVERSUCHE = 5;
	private String args;

	Context context;
	Activity activity;

	/**
	 * Läd Termine aus Dualis in die Datenbank
	 * 
	 * @param username
	 *            Benutzername
	 * @param passwort
	 *            Passwort
	 * @param wieVieleMonate
	 *            Gibt an wieviele Monate heruntergeladen werden sollen
	 * @param context
	 *            Context der Activity auf der Fehler ausgegeben werden sollen
	 * @return
	 */
	public boolean ladeTermineInDB(String username, String passwort, int wieVieleMonate, Context context)
	{
		int i = 0;
		do
		{
			args = login(username, passwort);
			i++;
		} while (args == null && i < ANZAHLLOGINVERSUCHE);
		if (args == null)
		{
			Toast.makeText(context, "Login fehlgeschlagen, überprüfen sie ihre Logindaten", Toast.LENGTH_SHORT).show();
			return false;
		}
		else
		{
			String month = getCalenderMonth(args, wieVieleMonate);
			if (month != null)
			{
				Pattern p1 = Pattern.compile(DUALIS_KALENDER_REGEXP, Pattern.CASE_INSENSITIVE);
				Matcher m1 = p1.matcher(month);
				String datum = "";
				String startzeit = "";
				String endzeit = "";
				String raum = "";
				String vorlesung = "";
				int id = 1;
				boolean datumLetzte = false;
				TerminDBAdapter terminDBAdapter = new TerminDBAdapter(context);
				terminDBAdapter.loescheAlleTermine();
//test 2
				while (m1.find())
				{

					int groupLaenge = m1.group(1).length();
					if (groupLaenge == 10)
					{
						if (datumLetzte)
						{
							startzeit = "";
							endzeit = "";
							vorlesung = "";
							raum = "";
							terminDBAdapter.createTermin(id, datum, startzeit, endzeit, vorlesung, raum);
							id++;
						}
						datum = m1.group(1);
						datumLetzte = true;
					}
					else
					{
						String str[] = m1.group(1).split("/");
						String str2[] = str[0].split("-");
						startzeit = str2[0].trim();
						endzeit = str2[1].trim();
						raum = str[1].trim();
						vorlesung = str[2].trim();
						terminDBAdapter.createTermin(id, datum, startzeit, endzeit, vorlesung, raum);

						id++;

						datumLetzte = false;
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
	 * @param passwort
	 *            Password
	 * @param context
	 *            Context der Activity auf der Fehler ausgegeben werden sollen
	 */
	public void ladeResultsInDB(String username, String passwort, Context context)
	{
		int i = 0;
		do
		{
			args = login(username, passwort);
			i++;
		} while (args == null && i < ANZAHLLOGINVERSUCHE);
		if (args == null)
		{
			Toast.makeText(context, "Login fehlgeschlagen, überprüfen sie ihre Logindaten", Toast.LENGTH_SHORT).show();
			return;
		}
		else
		{
			String results = getResults(args);

			results = results.replaceAll("\\s\\s+", " ");

			Pattern p1 = Pattern.compile(DUALIS_RESULT_REGEXP, Pattern.CASE_INSENSITIVE);
			Matcher m1 = p1.matcher(results);
			/*
			 * Options: 1: Prüfungsnummer 2: Vorlesungsname 3: Datum 4. Credits
			 * 5. Note 6. ECTSnote 7. Status Englisch 8. oder 9. Status Deutsch
			 */

			ResultsDBAdapter resultsDBAdapter = new ResultsDBAdapter(context);
			resultsDBAdapter.loescheDB();
			while (m1.find())
			{
				String pruefungsnummer;
				String vorlesungsname;
				String datum;
				String credits;
				String ectsnote;
				String note;
				String status;

				pruefungsnummer = m1.group(1);
				vorlesungsname = m1.group(2);
				datum = m1.group(3);
				credits = m1.group(4).substring(1, 4);
				note = m1.group(5);
				ectsnote = m1.group(6).substring(6);
				status = m1.group(8);

				if (pruefungsnummer == null)
					pruefungsnummer = " ";
				if (vorlesungsname == null)
					vorlesungsname = " ";
				if (datum == null)
					datum = " ";
				if (ectsnote == null)
					ectsnote = " ";
				if (credits == null)
					credits = " ";
				if (note == null)
					note = " ";
				if (status == null)
					status = " ";

				resultsDBAdapter.createResult(pruefungsnummer, vorlesungsname, datum, credits, note, ectsnote, status);
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
		if (args == null)

		{
			args = "";
		}
		return args;
	}

	/**
	 * Gibt den Header der URLConnection zurück
	 * 
	 * @param con
	 *            URLConnection
	 * @return header
	 */
	private String getHeader(URLConnection con)
	{
		String ret = "";
		for (String field : con.getHeaderFields().keySet())
		{
			ret += field + " : " + con.getHeaderFields().get(field);
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
		String ret = null;
		StringBuilder stringBuilder = new StringBuilder();

		try
		{
			URLConnection conn = connect(DUALIS_APP_URL + DUALIS_RESULTS + args + DUALIS_RESULTS_SUFFIX);

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			while ((line = rd.readLine()) != null)
			{
				stringBuilder.append(line);
				// ret+=line;
			}

			rd.close();
		}
		catch (Exception e)
		{
			ret = null;
			// TODO: richtige exception
		}
		ret = stringBuilder.toString();

		return ret;
	}

	/**
	 * Liefert die Vorlesungstermine aus dem DualisKalender zurück
	 * 
	 * @param args
	 *            Argumente
	 * @param wieVieleMonate
	 *            Gibt an wieviele Monate heruntergeladen werden sollen
	 * @return Liefert die Vorlesungen in einem String
	 */
	private String getCalenderMonth(String args, int wieVieleMonate)
	{
		String ret = null;
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i <= wieVieleMonate; i++)
		{
			try
			{
				String datum = getDate(i - 1);
				URLConnection conn = connect(DUALIS_APP_URL + DUALIS_KALENDAR + args + ",-N000019" + datum + DUALIS_KALENDER_MONATSANSICHT_ARGUMENTS_SUFFIX);
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;

				while ((line = rd.readLine()) != null)
				{
					stringBuilder.append(line);
				}
				rd.close();

			}
			catch (Exception e)
			{
				ret = null;
			}
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
	 * @param passwort
	 *            Passwort
	 * @return die Argumente die nun immer im Get HEADER übergeben werden
	 *         müssen, um eingelogt zu bleiben. null, falls login fehlgeschlagen
	 *         hat (kann verschiedene Gründe haben).
	 */
	public String login(String username, String passwort)
	{

		String cookie = null;

		if (username.contains("loerrach"))
		{
			Map<String, String> data = new HashMap<String, String>();
			data.put("username", username.replace("@dhbw-loerrach.de", ""));
			data.put("password", passwort);
			try
			{
				cookie = doSubmit("https://portal.dhbw-loerrach.de/cas/login?service=https%3A%2F%2Fportal.dhbw-loerrach.de%2Fc%2Fportal%2Flogin", data);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			String data = "usrname=" + username + "&pass=" + passwort + "&APPNAME=CampusNet&PRGNAME=LOGINCHECK&ARGUMENTS=clino%2Cusrname%2Cpass%2Cmenuno%2Cpersno%2Cbrowser%2Cplatform&clino=000000000000001&menuno=000000&persno=00000000&browser=&platform=";

			URLConnection conn;
			try
			{
				conn = connect(DUALIS_LOGIN_URL);
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.close();
				String header = getHeader(conn);

				Pattern p = Pattern.compile(DUALIS_ARGUMENTE_REGEXP, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(header);
				if (m.find())
				{
					cookie = m.group(1);
				}
			}
			catch (MalformedURLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
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
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
		conn.setRequestProperty("User-Agent", "DHBW Dualis App by Andy H. and Michi V.");
		conn.setRequestProperty("Cookie", DUALIS_COOKIE);
		return conn;
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
	 * @param wieVielMonate
	 * @return Gibt das Datum in wieVielMonate zurück
	 */
	private String getDate(int wieVielMonate)
	{
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		Calendar cal = Calendar.getInstance();
		String curMonth = month.format(cal.getTime());
		String curYear = year.format(cal.getTime());
		int curMonthInt = Integer.parseInt(curMonth);
		curMonthInt = curMonthInt + wieVielMonate;
		if (curMonthInt >= 13)
		{
			curMonthInt = curMonthInt - 12;
			int curYearInt = Integer.parseInt(curYear);
			curYearInt = curYearInt + 1;
			curYear = String.valueOf(curYearInt);
		}
		curMonth = String.valueOf(curMonthInt);
		if (curMonthInt <= 9)
		{
			curMonth = "0" + curMonthInt;
		}
		String ret = ",-A01." + curMonth + "." + curYear;
		return ret;
	}

	/**
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
