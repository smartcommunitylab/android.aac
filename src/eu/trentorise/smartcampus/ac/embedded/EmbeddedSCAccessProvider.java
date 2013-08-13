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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.AuthActivity;
import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.ac.model.TokenData;
import eu.trentorise.smartcampus.ac.network.RemoteConnector;
/**
 * Implementation of the {@link SCAccessProvider} interface relying on the token
 * stored in shared preferences and using the {@link EmbeddedAuthActivity} activity.
 * @author raman
 *
 */
public class EmbeddedSCAccessProvider implements SCAccessProvider{

	@Override
	public boolean login(Activity activity, String clientId, String clientSecret, Bundle extras) throws AACException  {
		assert clientId != null;
		assert clientSecret != null;
		
		String token;
		try {
			token = Preferences.getAccessToken(activity);
		} catch (NameNotFoundException e1) {
			throw new AACException(e1.getMessage(),e1);
		} 
		if (token == null) {
			Intent i = new Intent(activity, EmbeddedAuthActivity.class);
			if (extras != null) {
				i.putExtras(extras);
			}
			i.putExtra(AuthActivity.CLIENT_ID, clientId);
			i.putExtra(AuthActivity.CLIENT_SECRET, clientSecret);
			
			activity.startActivityForResult(i, SC_AUTH_ACTIVITY_REQUEST_CODE);
			return true;
		}
		return false;
	}

	@Override
	public String readToken(Context ctx, String clientId, String clientSecret) throws AACException {
		try {
			String token = Preferences.getAccessToken(ctx);
			Long expTime = Preferences.getExpirationTime(ctx);
			// have a margine of 1 min
			if (expTime > System.currentTimeMillis() + 60*1000) {
				return token;
			} else {
				TokenData data = RemoteConnector.refreshToken(Constants.getAuthUrl(ctx), Preferences.getRefreshToken(ctx), clientId, clientSecret);
				Preferences.setAccessToken(ctx, data.getAccess_token());
				if (data.getRefresh_token() != null && data.getRefresh_token().length() > 0) {
					Preferences.setRefreshToken(ctx, data.getRefresh_token());
				}
				Preferences.setExpirationTime(ctx, data.getExpires_in());
				return data.getAccess_token();
			}
		} catch (NameNotFoundException e1) {
			throw new AACException(e1.getMessage(),e1);
		} 
	}

	@Override
	public void logout(final Context ctx) throws AACException {
		final String token;
		try {
			token = Preferences.getAccessToken(ctx);
			Preferences.clear(ctx);
		} catch (NameNotFoundException e) {
			throw new AACException(e.getMessage(),e);
		}
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					RemoteConnector.revokeToken(Constants.getAuthUrl(ctx), token);
				} catch (NameNotFoundException e) {
					Log.e(EmbeddedSCAccessProvider.class.getName(), ""+e.getMessage());
				} catch (AACException e) {
					Log.e(EmbeddedSCAccessProvider.class.getName(), ""+e.getMessage());
				}
				return null;
			}
		};
	}
	
}