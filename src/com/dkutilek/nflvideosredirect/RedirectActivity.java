package com.dkutilek.nflvideosredirect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dkutilek.nflvideosredirect.intentservice.AsyncTaskCallback;
import com.dkutilek.nflvideosredirect.intentservice.GetMp4UrlService;

/**
 * A simple extension of {@link Activity} that is created on the intent filter:
 * 
 * <pre>{@code
 * <intent-filter>
 *    <action android:name="android.intent.action.VIEW" />
 *    <category android:name="android.intent.category.DEFAULT" />
 *    <category android:name="android.intent.category.BROWSABLE" />
 *    <data android:host="www.nfl.com" android:pathPrefix="/videos"
 *        android:scheme="http" />
 * </intent-filter>
 * }</pre>
 * 
 * URLs that are opened that match "<b>http://www.nfl.com/videos*</b>" are
 * eligible to be handled by this activity.  If this activity is
 * launched via an NFL videos URL it uses the {@link GetMp4UrlService}
 * to get the direct URL to the mp4 video.  It then fires off a new
 * intent to view the video given by the direct URL and closes.<br>
 * <br>
 * This activity also fires off for all of the 32 team websites as well.
 * @author Drew Kutilek &lt;drew.kutilek@gmail.com&gt;
 */
public class RedirectActivity extends Activity {
	
	private ProgressBar pb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_redirect);
		pb = (ProgressBar) this.findViewById(R.id.progressBar);
		pb.setProgress(0);
		
		// Get url from intent
		final String url = getIntent().getDataString();
		
		new GetMp4UrlService(pb, this, new AsyncTaskCallback<String[]>() {
			@Override
			public void taskComplete(String[] videoUrls) {
				if (videoUrls == null)
					return;
				
				String videoUrl = videoUrls[0];
				if (videoUrl != null) {
					// Launch a new intent to view the parsed url
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					
					// Raw video urls from the team websites include the complete
					// url starting with "http://"
					if (videoUrl.startsWith("http://")) {
						i.setDataAndType(Uri.parse(videoUrl), "video/*");
					}
					// Urls from nfl.com need to include the prefix
					// "http://ll.video.nfl.com"
					else {
						i.setDataAndType(Uri.parse("http://ll.video.nfl.com" +
											videoUrl), "video/*");
					}
					startActivity(i);
				}
				else {
					Toast.makeText(getApplicationContext(),
							"No mp4 video path in HTML source of the url \"" +
							url + "\"", Toast.LENGTH_LONG).show();
				}
				
				// This call ensures that nothing is drawn for this activity
				// and the UI is entirely non-existent.  Without this call
				// there is a transparent activity placed over the UI after the
				// Intent is fired.  Works with
				// android:theme="@android:style/Theme.Translucent.NoTitleBar"
				// to turn this activity into a "service" that can be launched
				// via an intent-filter.
				finish();
			}
		}).execute(url);
	}
}
