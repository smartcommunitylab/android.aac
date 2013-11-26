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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import eu.trentorise.smartcampus.ac.account.AccountSCAccessProvider;
import eu.trentorise.smartcampus.ac.embedded.EmbeddedSCAccessProvider;

/**
 * A reference interface for the Smart Campus Access Control library. Defines methods for the 
 * authentication token retrieval or revocation.
 * 
 * @author raman
 *
 */
public abstract class SCAccessProvider {

	public static final int SC_AUTH_ACTIVITY_REQUEST_CODE = 1000;
	
	public static SCAccessProvider getInstance(Context ctx) {
		if (Constants.isAccountBasedAccess(ctx)) return new AccountSCAccessProvider();
		return new EmbeddedSCAccessProvider();
	}

	/**
	 * Verify the user is already logged in.
	 * @param ctx {@link Context} to operate in
	 * 
	 * @return true if the login has already been performed
	 */
	public abstract boolean isLoggedIn(Context ctx) throws AACException;
	
	/**
	 * Verify the user is already logged in and the necessary token have been obtained. Otherwise,
	 * the authentication and authorization procedure is triggered. The calling activity
	 * should implement {@link Activity#onActivityResult} with request code {@link SCAccessProvider#SC_AUTH_ACTIVITY_REQUEST_CODE}
	 *  to handle the possible outcomes.
	 * 
	 * @param activity Activity to be used to call authentication activity.
	 * @param extras additional parameters, may be null. May include the values for {@link Constants#KEY_AUTHORITY}
	 * to explicitly define the authority used to login the user and for {@link Constants#KEY_SCOPE} to explicitly
	 * define the request authorization scope for the app.
	 * 
	 * @return true if the login is performed for the first time, false otherwise
	 */
	public abstract boolean login(Activity activity, Bundle extras) throws AACException;

	/**
	 * Try to read the token stored, or retrieve it from the remote service 
	 * if the refresh token is available. 
	 * @param ctx {@link Context} of the token retrieval.
	 * @return valid token value or null if the valid token is not available (i.e., user not authenticated)
	 */
	public abstract String readToken(Context ctx) throws AACException; 

	/** 
	 * Invalidate the authentication tokens and clear the locally stored data. Not to be called on UI thread
	 * @param ctx
	 * @param clientId clientID of the application.
	 * @param clientSecret client secret of the application.
	 * @return true if the logout was successful
	 */
	public abstract boolean logout(Context ctx) throws AACException;
	
}
