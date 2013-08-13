/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.ac.embedded;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import eu.trentorise.smartcampus.ac.Constants;

/**
 * Utility class that allows Smart Campus applications to have a common cloud
 * where sharing data.
 * 
 * @author Simone Casagranda
 * 
 */
public final class Preferences {
	
	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
	private static final String EXPIRES_IN = "EXPIRES_IN";
	
	// ======================================================================= //
	// GETTERS & SETTERS
	// ======================================================================= //

	/**
	 * Retrieves access token.
	 * @throws NameNotFoundException 
	 */
	static String getAccessToken(Context context) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		return prefs.getString(ACCESS_TOKEN, null);
	}
	
	/**
	 * Stores a passed authentication token.
	 * @throws NameNotFoundException 
	 */
	static void setAccessToken(Context context, String token) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_TOKEN, token);
		edit.commit();
	}

	/**
	 * Retrieves refresh token.
	 * @throws NameNotFoundException 
	 */
	static String getRefreshToken(Context context) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		return prefs.getString(REFRESH_TOKEN, null);
	}
	
	/**
	 * Stores a passed refresh token.
	 * @throws NameNotFoundException 
	 */
	static void setRefreshToken(Context context, String token) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		Editor edit = prefs.edit();
		edit.putString(REFRESH_TOKEN, token);
		edit.commit();
	}

	/**
	 * Retrieves expiration time.
	 * @throws NameNotFoundException 
	 */
	static Long getExpirationTime(Context context) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		return prefs.getLong(EXPIRES_IN, 0);
	}
	
	/**
	 * Augments the expiration time by the specified value (in seconds).
	 * @throws NameNotFoundException 
	 */
	static void setExpirationTime(Context context, int expires_in) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		Editor edit = prefs.edit();
		edit.putLong(EXPIRES_IN, System.currentTimeMillis()+1000*expires_in);
		edit.commit();
	}


	
	// ======================================================================= //
	// OTHERS
	// ======================================================================= //
	
	/**
	 * Clears all stored preferences
	 * @throws NameNotFoundException 
	 */
	static void clear(Context context) throws NameNotFoundException{
		SharedPreferences prefs = Constants.getPrefs(context);
		Editor edit = prefs.edit();
		edit.remove(ACCESS_TOKEN);
		edit.remove(REFRESH_TOKEN);
		edit.remove(EXPIRES_IN);
		edit.commit();
	}
	
}
