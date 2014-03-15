package com.repositorysentry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class HttpGetTask extends AsyncTask<String, Void, String>{

	@Override
	protected String doInBackground(String... arg0) {
		HttpURLConnection urlConnection = null;
		URL url = null;
		StringBuffer response = new StringBuffer();
		
		try {
			url = new URL(arg0[0]);
			urlConnection = (HttpURLConnection) url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();
		}
			
		return response.toString();
	}
}
