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

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AuthActivity;
import eu.trentorise.smartcampus.ac.AuthListener;
import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.model.TokenData;

/**
 * Implementation of the {@link AuthActivity} storing the acquired token
 * in the shared preferences and broadcasting the result event.
 * @author raman
 *
 */
public class EmbeddedAuthActivity extends AuthActivity {
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent.getStringExtra(AccountManager.KEY_AUTHTOKEN) != null) {
			 final Intent res = new Intent();
			 res.putExtra(AccountManager.KEY_AUTHTOKEN, intent.getStringExtra(AccountManager.KEY_AUTHTOKEN));
			 setResult(RESULT_OK, res);
			 finish();
        } 
		super.onCreate(savedInstanceState);
	}

    @Override
	protected String getClientId() {
    	return getIntent().getStringExtra(CLIENT_ID);
	}


	@Override
	protected String getClientSecret() {
    	return getIntent().getStringExtra(CLIENT_SECRET);
	}


	@Override
	protected AuthListener getAuthListener() {
		return new AMAuthListener();
	}
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
	private class AMAuthListener implements AuthListener {

		@Override
		public void onTokenAcquired(TokenData data) {
			 final Intent intent = new Intent();
			 try {
				Preferences.setAccessToken(EmbeddedAuthActivity.this, data.getAccess_token());
				Preferences.setAccessToken(EmbeddedAuthActivity.this, data.getAccess_token());
				Preferences.setRefreshToken(EmbeddedAuthActivity.this, data.getRefresh_token());
				Preferences.setExpirationTime(EmbeddedAuthActivity.this, data.getExpires_in());
			} catch (NameNotFoundException e1) {
				Log.e(EmbeddedAuthActivity.class.getName(),""+e1.getMessage());
			}
			 //getSharedPreferences(Constants.ACCOUNT_TYPE,Context.MODE_PRIVATE).edit().putString(request.getStringExtra(Constants.KEY_AUTHORITY), token).commit();
			 intent.putExtra(AccountManager.KEY_AUTHTOKEN, data.getAccess_token());
			 setResult(RESULT_OK, intent);
			 finish();  	    		  
		}

		@Override
		public void onAuthFailed(String error) {
			 final Intent intent = new Intent();
			 intent.putExtra(AccountManager.KEY_AUTH_FAILED_MESSAGE, error);
			 setResult(Constants.RESULT_FAILURE, intent);
			 finish();  	    		  
		}

		@Override
		public void onAuthCancelled() {
			 final Intent intent = new Intent();
			 setResult(RESULT_CANCELED, intent);
			 finish();  	    		  
		}
    	
    }

}
