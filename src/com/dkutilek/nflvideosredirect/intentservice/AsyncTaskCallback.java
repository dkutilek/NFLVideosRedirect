package com.dkutilek.nflvideosredirect.intentservice;

import android.os.AsyncTask;

/**
 * An interface that allows an "anonymous" function callback when an
 * {@link AsyncTask} is complete
 * 
 * @author Drew Kutilek &lt;drew.kutilek@gmail.com&gt;
 *
 * @param <T> The type of the result returned by the task
 */
public interface AsyncTaskCallback<T> {

	/**
	 * Callback function for when the task is complete
	 * @param result The result returned by the task
	 */
	void taskComplete(T result);
}
