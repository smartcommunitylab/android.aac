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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

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
	
	// Name for preferences
	private static final String COMMON_PREF = "COMMON_PREF";
	// Access mode (private to application and other ones with same Shared UID)
	private static final int ACCESS = Context.MODE_PRIVATE|Context.CONTEXT_RESTRICTED;

	// ======================================================================= //
	// GETTERS & SETTERS
	// ======================================================================= //

	/**
	 * Retrieves access token.
	 * @throws NameNotFoundException 
	 */
	static String getAccessToken(Context context) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		return prefs.getString(ACCESS_TOKEN, null);
	}
	
	/**
	 * Stores a passed authentication token.
	 * @throws NameNotFoundException 
	 */
	static void setAccessToken(Context context, String token) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_TOKEN, token);
		edit.commit();
	}

	/**
	 * Retrieves refresh token.
	 * @throws NameNotFoundException 
	 */
	static String getRefreshToken(Context context) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		return prefs.getString(REFRESH_TOKEN, null);
	}
	
	/**
	 * Stores a passed refresh token.
	 * @throws NameNotFoundException 
	 */
	static void setRefreshToken(Context context, String token) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		edit.putString(REFRESH_TOKEN, token);
		edit.commit();
	}

	/**
	 * Retrieves expiration time.
	 * @throws NameNotFoundException 
	 */
	static Long getExpirationTime(Context context) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		return prefs.getLong(EXPIRES_IN, 0);
	}
	
	/**
	 * Augments the expiration time by the specified value (in seconds).
	 * @throws NameNotFoundException 
	 */
	static void setExpirationTime(Context context, int expires_in) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		 // temporal patch: expires in 1 hour
		 long expTime = System.currentTimeMillis()+1000L*60*60;
		 // long expTime = System.currentTimeMillis()+1000*expires_in;

		edit.putLong(EXPIRES_IN, expTime);
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
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
		edit.remove(ACCESS_TOKEN);
		edit.remove(REFRESH_TOKEN);
		edit.remove(EXPIRES_IN);
		edit.commit();
	}

	/**
	 * Read the shared preferences file where common properties are stored
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	static SharedPreferences getPrefs(Context context) throws NameNotFoundException {
		Context sharedContext = context.createPackageContext(getSharedPackage(context), ACCESS);
		return sharedContext.getSharedPreferences(COMMON_PREF, ACCESS);
	}

	private static String getSharedPackage(Context ctx) throws NameNotFoundException {
		ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
		return info.packageName;
	}


}
