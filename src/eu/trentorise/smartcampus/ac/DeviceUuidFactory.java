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

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * @author raman
 *
 */
public class DeviceUuidFactory {

	private static final String INCORRECT_ANDROID_ID = "9774d56d682e549c";
	protected static final String PREFS_FILE = "device_id.xml";
	protected static final String PREFS_DEVICE_ID = "device_id";

	protected volatile static UUID uuid;

	public DeviceUuidFactory(Context context) {

	    if( uuid ==null ) {
	        synchronized (DeviceUuidFactory.class) {
	            if( uuid == null) {
	                final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
	                final String id = prefs.getString(PREFS_DEVICE_ID, null );

	                if (id != null) {
	                    // Use the ids previously computed and stored in the prefs file
	                    uuid = UUID.fromString(id);

	                } else {

	                    final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

	                    // Use the Android ID unless it's broken, in which case fallback on deviceId,
	                    // unless it's not available, then fallback on a random number which we store
	                    // to a prefs file
	                    try {
	                        if (!INCORRECT_ANDROID_ID.equals(androidId)) {
	                            uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
	                        } else {
	                            final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
	                            uuid = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
	                        }
	                    } catch (UnsupportedEncodingException e) {
	                        throw new RuntimeException(e);
	                    }

	                    // Write the value out to the prefs file
	                    prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString() ).commit();

	                }

	            }
	        }
	    }

	}


	public UUID getDeviceUuid() {
	    return uuid;
	}
}
