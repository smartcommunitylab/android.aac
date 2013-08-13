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
package eu.trentorise.smartcampus.ac.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenData {

	private String scope;
	private String access_token;
	private String token_type;
	private String refresh_token;
	private int expires_in;
	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}
	/**
	 * @param scope the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}
	/**
	 * @return the access_token
	 */
	public String getAccess_token() {
		return access_token;
	}
	/**
	 * @param access_token the access_token to set
	 */
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	/**
	 * @return the token_type
	 */
	public String getToken_type() {
		return token_type;
	}
	/**
	 * @param token_type the token_type to set
	 */
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}
	/**
	 * @return the refresh_token
	 */
	public String getRefresh_token() {
		return refresh_token;
	}
	/**
	 * @param refresh_token the refresh_token to set
	 */
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}
	/**
	 * @return the expires_in
	 */
	public int getExpires_in() {
		return expires_in;
	}
	/**
	 * @param expires_in the expires_in to set
	 */
	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}
	/**
	 * @param response
	 * @return
	 */
	public static TokenData valueOf(String response) {
    	try {
			JSONObject json = new JSONObject(response);
    		TokenData data = new  TokenData();
			data.refresh_token = json.getString("refresh_token");
			data.access_token = json.getString("access_token");
			data.expires_in = json.getInt("expires_in");
			data.scope = json.getString("scope");
			data.token_type = json.getString("token_type");
			return data;
		} catch (JSONException e) {
			return null;
		}
	}
}
