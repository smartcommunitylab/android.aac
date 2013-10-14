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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AuthActivity;
import eu.trentorise.smartcampus.ac.AuthListener;
import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.model.TokenData;

/**
 *  Implementation of the {@link AuthActivity} storing the acquired token
 * in the {@link AccountManager} infrastructure and broadcasting the result event.
 * @author raman
 *
 */
public class AuthenticatorActivity  extends AuthActivity {
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

	@Override
	protected AuthListener getAuthListener() {
		return new AMAuthListener();
	}

	@Override
	protected String getClientId() {
		return Constants.getClientId(getApplicationContext());
	}

	@Override
	protected String getClientSecret() {
		return Constants.getClientSecret(getApplicationContext());
	}

	private class AMAuthListener implements AuthListener {

		@Override
		public void onTokenAcquired(TokenData data) {
			String accountType = null;
			String aTokenType = Constants.getAccountTokenType(getApplicationContext());
			Account account = null;
			try {
				accountType = Constants.getAccountType(getApplicationContext());
				account = new Account(Constants.getAccountName(AuthenticatorActivity.this), accountType);
			} catch (NameNotFoundException e) {
				onAuthFailed(e.getMessage());
				return;
			}
			
			 AccountManager mAccountManager = AccountManager.get(getApplicationContext());
			 Account[] accounts = mAccountManager.getAccountsByType(accountType);
			 if (accounts != null) {
				for (int i = 0; i < accounts.length; i++) {
					mAccountManager.removeAccount(accounts[i], null, null);
				}
			 }
			
			Bundle dataBundle = new Bundle(); 
			dataBundle.putString(Constants.KEY_REFRESH_TOKEN+aTokenType, data.getRefresh_token());
			dataBundle.putString(Constants.KEY_EXPIRES_IN+aTokenType, ""+(System.currentTimeMillis()+1000*data.getExpires_in()));
			mAccountManager.addAccountExplicitly(account, null, dataBundle);
			 
		     mAccountManager.setAuthToken(account, aTokenType, data.getAccess_token());

			 final Intent intent = new Intent();
			 intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
			 intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			 intent.putExtra(AccountManager.KEY_AUTHTOKEN, data.getAccess_token());

			 // this is workaround that is needed on some devices: without it the 
			 // getuserdata may return null. the problem is with the bug in the in-memory account data caching
			 mAccountManager.setUserData(account, Constants.KEY_REFRESH_TOKEN+aTokenType, dataBundle.getString(Constants.KEY_REFRESH_TOKEN+aTokenType));
			 mAccountManager.setUserData(account, Constants.KEY_EXPIRES_IN+aTokenType, dataBundle.getString(Constants.KEY_EXPIRES_IN+aTokenType));
			 
			 setAccountAuthenticatorResult(intent.getExtras());
			 setResult(RESULT_OK, intent);
			 finish();  	    		  
		}

		@Override
		public void onAuthFailed(String error) {
			 final Intent intent = new Intent();
			 try {
				intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Constants.getAccountName(AuthenticatorActivity.this));
				 intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.getAccountType(AuthenticatorActivity.this));
			} catch (NameNotFoundException e) {
				Log.e("authenticator", ""+e.getMessage());
			}
			 intent.putExtra(AccountManager.KEY_AUTH_FAILED_MESSAGE, error);
			 setAccountAuthenticatorResult(intent.getExtras());
			 setResult(Constants.RESULT_FAILURE, intent);
			 finish();  	    		  
		}

		@Override
		public void onAuthCancelled() {
			 final Intent intent = new Intent();
			 try {
				intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, Constants.getAccountName(AuthenticatorActivity.this));
				 intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.getAccountType(AuthenticatorActivity.this));
			} catch (NameNotFoundException e) {
				Log.e("authenticator", ""+e.getMessage());
			}
			 setAccountAuthenticatorResult(intent.getExtras());
			 setResult(RESULT_CANCELED, intent);
			 finish();  	    		  
		}
    	
    }
    
}
