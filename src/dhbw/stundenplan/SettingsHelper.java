package dhbw.stundenplan;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 * Nur zum Optionen auszulesen und auszugeben FÜr Programm nicht benötigt
 * 
 * @author DH10HAH
 */
public class SettingsHelper extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		StringBuilder builder = new StringBuilder();

		builder.append("\n" + sharedPrefs.getString("anzahlmonate", "2"));
		TextView settingsTextView = (TextView) findViewById(R.id.settingsTextView);
		settingsTextView.setText(builder.toString());

	}
}
