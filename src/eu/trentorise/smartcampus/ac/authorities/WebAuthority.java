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

package eu.trentorise.smartcampus.ac.authorities;

import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.AuthListener;
import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.R;
import eu.trentorise.smartcampus.ac.model.TokenData;
import eu.trentorise.smartcampus.ac.network.RemoteConnector;

/**
 * @author raman
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebAuthority implements AuthorityHandler {

    static final FrameLayout.LayoutParams FILL =
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.MATCH_PARENT);
	protected WebView mWebView;
    private ProgressDialog mSpinner; 
    private ImageView mCrossImage; 
    private FrameLayout mContent;

	protected Activity mActivity;
	protected String mName;
	protected AuthListener mAuthListener;
	
	protected String mClientId;
	protected String mClientSecret;
	
	/**
	 * @param mName
	 * @param activity
	 */
	public WebAuthority(String mName) {
		super();
		this.mName = mName;
	}
	
	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public void authenticate(Activity activity, AuthListener listener, String clientId, String clientSecret) {
		this.mActivity = activity;
		this.mAuthListener = listener;
		this.mClientId = clientId;
		this.mClientSecret = clientSecret;
		setUp();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	protected void setUp() {
        mSpinner = new ProgressDialog(mActivity);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage(mActivity.getString(R.string.progress_loading)); 
        mContent = new FrameLayout(mActivity); 

        createCrossImage(); 
        int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
        setUpWebView(crossWidth / 2); 
        
        mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mActivity.addContentView(mContent, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); 
    }

	@Override
	public void cancel() {
		close();
	}

	protected void close() {
		if (mContent != null)  {
			mContent.setVisibility(View.GONE);
		}
	}
	
    private void setUpWebView(int margin) {
        LinearLayout webViewContainer = new LinearLayout(mActivity);
        mWebView = new WebView(mActivity);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setLoadWithOverviewMode(true);
//        mWebView.getSettings().setUseWideViewPort(true);
        CookieSyncManager.createInstance(mActivity.getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance(); 
        cookieManager.removeAllCookie();
        
        startWebView();
        mWebView.setLayoutParams(FILL);
        mWebView.setVisibility(View.INVISIBLE);
        
        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(mWebView);
        mContent.addView(webViewContainer);
    } 

    private void createCrossImage() {
        mCrossImage = new ImageView(mActivity);
        // Dismiss the dialog when user click on the 'x'
        mCrossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mActivity.onBackPressed();
            }
        });
        Drawable crossDrawable = mActivity.getResources().getDrawable(R.drawable.close);
        mCrossImage.setImageDrawable(crossDrawable);
        /* 'x' should not be visible while webview is loading
         * make it visible only after webview has fully loaded
        */
        mCrossImage.setVisibility(View.INVISIBLE);
    } 

	private void startWebView() {
		mWebView.setWebViewClient(new AuthWebViewClient());
        Intent intent = mActivity.getIntent();

		try {
			String url = prepareURL(intent);
		    mWebView.loadUrl(url);
		} catch (NameNotFoundException e) {
			close();
			mAuthListener.onAuthFailed("Failed to obtain AAC url");
		}
	}

	/**
	 * @param intent
	 * @return
	 * @throws NameNotFoundException 
	 */
	protected String prepareURL(Intent intent) throws NameNotFoundException {
		String url = Constants.getAuthUrl(mActivity);
		url += (url.endsWith("/") ? "" : "/") + "eauth/authorize/" + mName;
		String redirect = getOkUrl(mActivity);
		if (intent.hasExtra(Constants.KEY_REDIRECT_URI)) {
			redirect = intent.getStringExtra(Constants.KEY_REDIRECT_URI);
		}
		url += "?client_id="+mClientId +"&response_type=code&redirect_uri="+redirect;
		if (intent.hasExtra(Constants.KEY_SCOPE)) {
			url += "&scope="+URLEncoder.encode(intent.getStringExtra(Constants.KEY_SCOPE));
		}
		return url;
	}
	
	public class AuthWebViewClient extends WebViewClient {
		
		private boolean verified = false;

		public AuthWebViewClient() {
			super();
		}

		private boolean verifyUrl(String url) throws NameNotFoundException {
			if (isOkUrl(url)){
				String code = Uri.parse(url).getQueryParameter("code");
				String scope = mActivity.getIntent().getStringExtra(Constants.KEY_SCOPE);
				String redirectUri = getOkUrl(mActivity);
				if (code != null) {
					new ValidateAsyncTask().execute(code, mClientId, mClientSecret, scope, redirectUri);
				} else {
					close();
					mAuthListener.onAuthFailed("No token provided");
				}
				return true;
			} 
			if (isOkResultUrl(url)) {
				Uri uri = Uri.parse(url);	
				String access_token = uri.getQueryParameter("access_token");
				String scope = uri.getQueryParameter("scope");
				String refresh_token = uri.getQueryParameter("refresh_token");
				String type = uri.getQueryParameter("token_type");
				String expInStr = uri.getQueryParameter("expires_in");
				int expires_in = Integer.parseInt(expInStr);
				if (access_token == null || access_token.length() == 0 ||
					scope == null || scope.length() == 0 ||	
					refresh_token == null || refresh_token.length() == 0 ||
					expires_in <= 0) 
				{
					throw new IllegalArgumentException("Incorrect token data");
				}
				TokenData tokenData = new TokenData();
				tokenData.setAccess_token(access_token);
				tokenData.setExpires_in(expires_in);
				tokenData.setRefresh_token(refresh_token);
				tokenData.setScope(scope);
				tokenData.setToken_type(type);
				close();
				mAuthListener.onTokenAcquired(tokenData);
			}
			if (isCancelUrl(url)) {
            	mActivity.onBackPressed();
				return true;
			}
			return false;
		}

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (!verified) {
				try {
					verified  = verifyUrl(url);
				} catch (NameNotFoundException e) {
					close();
					mAuthListener.onAuthFailed("No auth url specified.");
					return;
				} catch (Exception e) {
					close();
					mAuthListener.onAuthFailed("Authentication problem: "+e.getMessage());
					return;
				}	 
			} 
			if (!verified) {
	            super.onPageStarted(view, url, favicon);
	            mSpinner.show();
			} else {
	            mWebView.setVisibility(View.INVISIBLE);
	            mCrossImage.setVisibility(View.INVISIBLE);
			}
        }  
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if (!verified) {
				super.onPageFinished(view, url);
	            mSpinner.dismiss();
	            /* 
	             * Once webview is fully loaded, set the mContent background to be transparent
	             * and make visible the 'x' image. 
	             */
	            mContent.setBackgroundColor(Color.TRANSPARENT);
	            mWebView.setVisibility(View.VISIBLE);
	            mCrossImage.setVisibility(View.VISIBLE);
			}
        }

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) 
		{
			handler.proceed();
		}
	}

	private class ValidateAsyncTask extends AsyncTask<String, Void, TokenData> {

		@Override
		protected TokenData doInBackground(String... params) {
			try {
				return RemoteConnector.validateAccessCode(Constants.getAuthUrl(mActivity), params[0], params[1], params[2], params[3], params[4]);
			} catch (NameNotFoundException e) {
				return null;
			} catch (AACException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(TokenData data) {
			close();
			if (data == null || data.getAccess_token() == null) {
				mAuthListener.onAuthFailed("Token validation failed");
			} else {
				mAuthListener.onTokenAcquired(data);
			}
		}
	}

	
	/**
	 * Retrieve the SmartCampus correct redirect URL
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	static String getOkUrl(Context context) {
		return "http://localhost";
	}

	/**
	 * @param url
	 * @return true if the url is the correct default redirect url with code request parameter
	 */
	public boolean isOkUrl(String url) {
		return url.startsWith(getOkUrl(mActivity)) && Uri.parse(url).getQueryParameter("code") != null;
	}
	/**
	 * @param url
	 * @return true if the url is the correct token result url with token data parameters
	 */
	public boolean isOkResultUrl(String url) {
		return url.startsWith(getOkUrl(mActivity)) && Uri.parse(url).getQueryParameter("access_token") != null;
	}
	/**
	 * @param url
	 * @return true if the url is the correct redirect url with code request parameter
	 */
	public boolean isCancelUrl(String url) {
		return url.startsWith(getOkUrl(mActivity)) && Uri.parse(url).getQueryParameter("error") != null;
	}

	
}
