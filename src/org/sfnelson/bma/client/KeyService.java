package org.sfnelson.bma.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("keyServer")
public interface KeyService extends RemoteService {
	UserData storeSecret(String serial, String token, long timeOffset);
	String retrieveKey();
	UserData generateUserData();
	UserData retrieveUserData();
	UserData sync();
}
