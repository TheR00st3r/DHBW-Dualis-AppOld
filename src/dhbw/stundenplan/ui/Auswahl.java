package dhbw.stundenplan.ui;

import android.os.Bundle;
import android.view.View;
import dhbw.stundenplan.R;
import dhbw.stundenplan.ui.element.OptionActivity;

public class Auswahl extends OptionActivity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auswahl);
		
	}
	
	
	public void showWeekly(View view)
	{
		ladeTermineInHash();
		intent.setClass(this, Wochenansicht.class);
		startActivity(intent);
	}
	
	public void showMarks(View view)
	{
		intent.setClass(this, Noten.class);
		startActivity(intent);
	}
}
