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
package eu.trentorise.smartcampus.ac.account;

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
 * @author raman
 * 
 */
public final class Preferences {
	
	public static final String APP_METADATA_SHARED_PACKAGE = "SHARED_PACKAGE";
	// Shared package path
	private static final String SHARED_PACKAGE = "it.smartcampuslab.launcher";

	// Name for preferences
	private static final String COMMON_PREF = "COMMON_PREF";
	// Access mode (private to application and other ones with same Shared UID)
	private static final int ACCESS = Context.MODE_PRIVATE|Context.CONTEXT_RESTRICTED;
	public static final String KEY_CLIENT_ID = "CLIENT_ID";
	public static final String KEY_CLIENT_SECRET = "CLIENT_SECRET";

	// ======================================================================= //
	// GETTERS & SETTERS
	// ======================================================================= //

	static void writeValue(Context ctx, String key, String value) throws NameNotFoundException {
		SharedPreferences prefs = getPrefs(ctx);
		Editor edit = prefs.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	static String readValue(Context ctx, String key) throws NameNotFoundException {
		SharedPreferences prefs = getPrefs(ctx);
		return prefs.getString(key, null);
	} 
	/**
	 * Clears all stored preferences
	 * @throws NameNotFoundException 
	 */
	static void clear(Context context) throws NameNotFoundException{
		SharedPreferences prefs = getPrefs(context);
		Editor edit = prefs.edit();
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

	private static String getSharedPackage(Context ctx) {
		try {
			ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			if (info != null && info.metaData != null && info.metaData.containsKey(APP_METADATA_SHARED_PACKAGE)) 
				return info.metaData.getString(APP_METADATA_SHARED_PACKAGE);
		} catch (NameNotFoundException e) {
		}
		return SHARED_PACKAGE;
	}

}
