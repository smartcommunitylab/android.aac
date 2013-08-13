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
package eu.trentorise.smartcampus.ac;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Constants and their read methods for security APIs
 * @author raman
 *
 */
public class Constants {

    /**
     * App authority key
     */
	public static final String KEY_AUTHORITY = "eu.trentorise.smartcampus.account.AUTHORITY";
    /**
     * App scope key
     */
	public static final String KEY_SCOPE = "eu.trentorise.smartcampus.account.SCOPE";
	
	/**
	 * Failure result code
	 */
	public static final int RESULT_FAILURE = 2;


	public static final String APP_METADATA_SHARED_PACKAGE = "SHARED_PACKAGE";
	
	// Shared package path
	private static final String SHARED_PACKAGE = "eu.trentorise.smartcampus.launcher";
	
	// Name for preferences
	private static final String COMMON_PREF = "COMMON_PREF";
	
	// Access mode (private to application and other ones with same Shared UID)
	private static final int ACCESS = Context.MODE_PRIVATE|Context.CONTEXT_RESTRICTED;

	private static final String P_AUTH_BASE_URL = "P_AUTH_BASE_URL";
	private static final String DEF_AUTH_BASE_URL = "https://vas-dev.smartcampuslab.it/aac";
	
	private static String baseUrl = null;

	/**
	 * Retrieve the SmartCampus correct redirect URL
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getOkUrl(Context context) {
		return "http://localhost";
	}

	/**
	 * Read the authentication base URL from the shared preferences file
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getAuthUrl(Context context) throws NameNotFoundException {
		if (baseUrl == null) {
			SharedPreferences prefs = Constants.getPrefs(context);
			baseUrl = prefs.getString(P_AUTH_BASE_URL, null);
			// this is temporary: the base url should always be set
			if (baseUrl == null) {
				setAuthUrl(context, DEF_AUTH_BASE_URL);
				prefs = Constants.getPrefs(context);
				baseUrl = prefs.getString(P_AUTH_BASE_URL, null);
			}
		}
		
		return baseUrl;
	}
	
	/**
	 * Write the authentication base URL to the shared preferences file.
	 * @param context
	 * @param url
	 * @throws NameNotFoundException
	 */
	public static void setAuthUrl(Context context, String url) throws NameNotFoundException {
		assert url != null;
		SharedPreferences prefs = Constants.getPrefs(context);
		Editor edit = prefs.edit();
		String newUrl = url.endsWith("/") ? url : (url+"/");
		edit.putString(P_AUTH_BASE_URL, newUrl);
		edit.commit();
		baseUrl = null;
	}
	/**
	 * Read the shared preferences file where common properties are stored
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static SharedPreferences getPrefs(Context context) throws NameNotFoundException {
		Context sharedContext = context.createPackageContext(getSharedPackage(context), ACCESS);
		return sharedContext.getSharedPreferences(COMMON_PREF, ACCESS);
	}

	private static String getSharedPackage(Context ctx) {
		try {
			ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			return info.packageName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return SHARED_PACKAGE;
	}
}
