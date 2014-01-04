package com.dkutilek.nflvideosredirect.intentservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

	private HttpClient client = new DefaultHttpClient();
	private Context context;
	
	/**
	 * Construct a GetMp4UrlService
	 * 
	 * @param context The context to use when writing {@link Toast} error
	 * messages.  If null, no messages are written.
	 */
	public GetMp4UrlService(Context context) {
		this.context = context;
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
				
				// If response is HTTP OK (200), parse mp4 video path from HTML source 
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Scanner scanner = new Scanner(is);
					result[i] = scanner.findWithinHorizon("\\S*?\\.mp4", 0);
				}
				else if (context != null){
					Toast.makeText(context,
							"Received HTTP Status " + response.getStatusLine().getReasonPhrase() +
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
