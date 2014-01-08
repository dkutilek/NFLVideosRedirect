package com.dkutilek.nflvideosredirect.intentservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Given an array of URLs, this asynchronous task will use a
 * {@link DefaultHttpClient} to query each URL and attempt to parse
 * out the .mp4 video's direct URL from the HTML source.
 * 
 * @author Drew Kutilek &lt;drew.kutilek@gmail.com&gt;
 */
public class GetMp4UrlService extends AsyncTask<String, Void, String[]> {

	private DefaultHttpClient client;
	private Context context;
	
	/**
	 * Pattern to search for a string of non-whitespace and non-quotation
	 * ending with ".mp4".  Team websites link to the raw mp4 in a HTML DOM
	 * attribute (content="http://[url].mp4").  nfl.com link to the raw mp4
	 * in a comment above the flash plugin HTML DOM element
	 * (&lt;!--&hellip;&nbsp;Video URL:&nbsp;&nbsp;&nbsp;[url].mp4&nbsp;
	 * &hellip;--&gt;)
	 */
	private static final String pattern = "[^\"\\s]*?\\.mp4";
	
	/**
	 * Construct a GetMp4UrlService
	 * 
	 * @param context The context to use when writing {@link Toast} error
	 * messages.  If null, no messages are written.
	 */
	public GetMp4UrlService(Context context) {
		this.context = context;
		client = new DefaultHttpClient();
		client.setCookieStore(new BasicCookieStore());
	}
	
	@Override
	protected String[] doInBackground(String... params) {
		String[] result = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			String url = params[i];
			result[i] = null;
			
			try {
				// Create and execute HTTP GET on url
				HttpGet request = new HttpGet(url);
				HttpResponse response = client.execute(request);
				InputStream is = response.getEntity().getContent();
				StatusLine statusLine = response.getStatusLine();
				
				// If response is HTTP OK (200), parse mp4 video path from HTML
				// source 
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					Scanner scanner = new Scanner(is);
					result[i] = scanner.findWithinHorizon(pattern, 0);
				}
				else if (context != null) {
					Toast.makeText(context,
						"Received HTTP Status " +
						statusLine.getReasonPhrase() +
						" from url \"" + url + "\"",
						Toast.LENGTH_LONG).show();
				}
				
				try {
					is.close();
				} catch (IOException e) {/* close quietly */}
				
			} catch (ClientProtocolException e) {
				if (context != null)
					Toast.makeText(context,
						"HTTP Request Failed: " + e.getMessage() +
						" for url \"" + url + "\"",
						Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				if (context != null)
					Toast.makeText(context,
						"HTTP Request Failed: " + e.getMessage() +
						" for url \"" + url + "\"",
						Toast.LENGTH_LONG).show();
			}
		}
		
		return result;
	}

}
