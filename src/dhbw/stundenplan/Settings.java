package dhbw.stundenplan;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author DH10HAH
 */
public class Settings extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

}
