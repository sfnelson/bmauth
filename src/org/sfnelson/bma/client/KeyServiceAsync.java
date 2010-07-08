package org.sfnelson.bma.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface KeyServiceAsync {
	void retrieveKey(AsyncCallback<String> callback);
	void storeSecret(String serial, String token, long timeOffset,
			AsyncCallback<UserData> callback);
	void generateUserData(AsyncCallback<UserData> callback);
	void retrieveUserData(AsyncCallback<UserData> callback);
	void sync(AsyncCallback<UserData> callback);
}
