package com.dkutilek.nflvideosredirect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

/**
 * The main (and only) UI screen of the application.  It displays information
 * and allows the user to click on a button to send an email to me.
 * @author Drew Kutilek &lt;drew.kutilek@gmail.com&gt;
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void mailto(View v) {
		// Launch a new intent to open a new email message
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] {"drew.kutilek@gmail.com"});
		startActivity(Intent.createChooser(i, "Send Email"));
	}
}
