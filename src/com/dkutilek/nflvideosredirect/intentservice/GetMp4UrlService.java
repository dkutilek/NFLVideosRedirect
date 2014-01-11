package com.dkutilek.nflvideosredirect.intentservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Given an array of URLs, this asynchronous task will use a
 * {@link DefaultHttpClient} to query each URL and attempt to parse
 * out the .mp4 video's direct URL from the HTML source.
 * 
 * @author Drew Kutilek &lt;drew.kutilek@gmail.com&gt;
 */
public class GetMp4UrlService extends AsyncTask<String, Integer, String[]> {

	private DefaultHttpClient client;
	private ProgressBar pb;
	private int progress;
	private Context context;
	private AsyncTaskCallback<String[]> callback;
	private String error = null;
	
	/**
	 * Pattern to search for a string of non-whitespace and non-quotation
	 * ending with ".mp4".  Team websites link to the raw mp4 in a HTML DOM
	 * attribute (content="http://[url].mp4").  nfl.com link to the raw mp4
	 * in a comment above the flash plugin HTML DOM element
	 * (&lt;!--&hellip;&nbsp;Video URL:&nbsp;&nbsp;&nbsp;[url].mp4&nbsp;
	 * &hellip;--&gt;)
	 */
	private static final String pattern = ".*?([^\"\\s]*?\\.mp4).*?";
	
	private static final Pattern p = Pattern.compile(pattern);
	
	/**
	 * Construct a GetMp4UrlService
	 * 
	 * @param context The context to use when writing {@link Toast} error
	 * messages.  If null, no messages are written.
	 * @param callback The callback interface to call the function
	 * {@link AsyncTaskCallback#taskComplete(Object) taskComplete} on when
	 * {@link GetMp4UrlService#onPostExecute(String[]) onPostExecute} is
	 * called.  If null, no functions are called.
	 */
	public GetMp4UrlService(ProgressBar pb, Context context,
			AsyncTaskCallback<String[]> callback) {
		this.pb = pb;
		this.progress = 0;
		this.context = context;
		this.callback = callback;
		client = new DefaultHttpClient();
		client.setCookieStore(new BasicCookieStore());
		CookieSpecFactory csf = new CookieSpecFactory() {
			public CookieSpec newInstance(HttpParams params) {
				return new BrowserCompatSpec() {
					@Override
					public void validate(Cookie cookie, CookieOrigin origin)
							throws MalformedCookieException {
						// allow all cookies
					}
				};
			}
		};
		client.getCookieSpecs().register("easy", csf);
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
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
					long length = response.getEntity().getContentLength();
					long count = 0;
					// read in content one line at a time
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String line = br.readLine();
					while (line != null) {
						count += line.length();
						int progress = Math.round((((float) count) /
								((float) length) * ((float) pb.getMax())));
						if (this.progress < progress) {
							this.progress = progress;
							publishProgress(progress);
						}
						
						// Occasionally a line comes in thats way too large
						// and attempting to process a regular expression on it
						// freezes the task, so we'll skip those for now until
						// I find a better way to handle it
						if (line.length() < 5000) {
							// attempt to match the line
							Matcher m = p.matcher(line);
							if (m.matches()) {
								result[i] = m.group(1);
								break;
							}
						}
						
						// read another line
						line = br.readLine();
					}
				}
				else if (context != null) {
					error = "Received HTTP Status " +
						statusLine.getReasonPhrase() +
						" from url \"" + url + "\"";
					return null;
				}
				
				try {
					is.close();
				} catch (IOException e) {/* close quietly */}
				
			} catch (ClientProtocolException e) {
				error = "HTTP Request Failed: " + e.getMessage() +
						" for url \"" + url + "\"";
				return null;
			} catch (IOException e) {
				error = "HTTP Request Failed: " + e.getMessage() +
						" for url \"" + url + "\"";
				return null;
			}
		}
		return result;
	}
	
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (values.length > 0)
			pb.setProgress(values[0]);
	}
	
    protected void onPostExecute(String[] result) {
    	super.onPostExecute(result);
    	pb.setProgress(pb.getMax());
    	
    	if (context != null && error != null)
			Toast.makeText(context, error, Toast.LENGTH_LONG).show();
    	if (callback != null)
    		callback.taskComplete(result);
    }
}
