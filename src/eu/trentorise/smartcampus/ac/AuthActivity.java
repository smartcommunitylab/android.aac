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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import android.accounts.AccountAuthenticatorActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import eu.trentorise.smartcampus.ac.authorities.AuthorityHandler;
import eu.trentorise.smartcampus.ac.authorities.AuthorityHelper;

/**
 * Abstract android activity to handle the authentication interactions. 
 * Defines an embedded Web browser (WebView) where the authentication interactions
 * take place. Upon result obtained the token is retrieved from the WebView context.
 * The result is passed to the {@link AuthListener} instance that the concrete subclasses
 * should define.
 * @author raman
 *
 */
public abstract class AuthActivity extends AccountAuthenticatorActivity {

	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret"; 

    static final LinearLayout.LayoutParams WRAP =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);

    static {
    	WRAP.setMargins(10, 10, 10, 10);
    }
    

    private AuthListener authListener = getAuthListener();
    private AuthorityHandler mHandler = null;
    private Collection<String> mAuthorities = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE); 
      setContentView(R.layout.authorities);
      setUpAuthorities();
    }
    
    protected void setUpAuthorities() {
        Intent intent = getIntent();
		if (intent.getStringExtra(Constants.KEY_AUTHORITY) != null) {
			mHandler = AuthorityHelper.getAuthorityHandlerForName(intent.getStringExtra(Constants.KEY_AUTHORITY));
			if (mHandler != null) {
				mAuthorities = Collections.singletonList(intent.getStringExtra(Constants.KEY_AUTHORITY));
			}
		} else {
			mAuthorities = AuthorityHelper.getAuthorities(this);
			for (Iterator<String> iterator = mAuthorities.iterator(); iterator.hasNext();) {
				String a = iterator.next();
				if (AuthorityHelper.getAuthorityHandlerForName(a) == null) {
					iterator.remove();
				}
			}
		}
		if (mAuthorities.size() == 0) {
			authListener.onAuthFailed("No authorities");
		}
		
		if (mAuthorities.size() == 1) {
			mHandler = AuthorityHelper.getAuthorityHandlerForName(mAuthorities.iterator().next());
			mHandler.authenticate(this, authListener, getClientId(), getClientSecret());
		} else {
			prepareLayout();
		}
    	
    }
    
    /**
	 * @param mAuthorities
	 */
	private void prepareLayout() {
		findViewById(R.id.authority_container).setVisibility(View.VISIBLE);
		LinearLayout ll = (LinearLayout)findViewById(R.id.button_container);
		ll.removeAllViews();
		for (String a : mAuthorities) {
			Button b = new Button(this);
//			b.setLayoutParams(WRAP);
			b.setTextColor(getResources().getColor(android.R.color.white));
			b.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
			b.setText(AuthorityHelper.getAuthorityLabelForName(a));
			b.setOnClickListener(new AuthButtonClickListener(AuthorityHelper.getAuthorityHandlerForName(a)));
			ll.addView(b,WRAP);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mHandler != null) {
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    public void onBackPressed() {
    	if (mHandler == null || mAuthorities != null && mAuthorities.size() == 1) {
        	super.onBackPressed();
        	authListener.onAuthCancelled();
    	} else {
    		if (mHandler != null) {
    			mHandler.cancel();
    			mHandler = null;
    		}
    		prepareLayout();
    	}
    }

    protected abstract String getClientId();
    protected abstract String getClientSecret();
	protected abstract AuthListener getAuthListener();

	private class AuthButtonClickListener implements OnClickListener {

		AuthorityHandler ah;

		public AuthButtonClickListener(AuthorityHandler ah) {
			super();
			this.ah = ah;
		}

		@Override
		public void onClick(View v) {
			mHandler = ah;
			findViewById(R.id.authority_container).setVisibility(View.INVISIBLE);
			mHandler.authenticate(AuthActivity.this, getAuthListener(), getClientId(), getClientSecret());
		}
		
	}

}
