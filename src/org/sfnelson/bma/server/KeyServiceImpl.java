package org.sfnelson.bma.server;

import java.io.IOException;

import org.sfnelson.bma.client.KeyService;
import org.sfnelson.bma.client.NotAuthenticatedException;
import org.sfnelson.bma.client.UserData;

import sed.authemu.Authenticator;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class KeyServiceImpl extends RemoteServiceServlet implements KeyService {

	private User getUser() throws NotAuthenticatedException {
		User user = UserServiceFactory.getUserService().getCurrentUser();

		if (user == null) {
			throw new NotAuthenticatedException();
		}

		return user;
	}

	public String retrieveKey() throws NotAuthenticatedException {
		SecretStore store = SecretStore.getStore(getUser().getUserId());

		if (store == null || store.getSerial() == null) {
			return null;
		}

		Authenticator auth = new Authenticator();
		auth.setSerial(store.getToken(), store.getSerial(), store.getTimeOffset());

		return auth.getAuthKey();
	}

	public UserData storeSecret(String serial, String token, long timeOffset) throws NotAuthenticatedException {
		SecretStore.setSecret(getUser().getUserId(), serial, token, timeOffset);

		return retrieveUserData();
	}

	public UserData retrieveUserData() throws NotAuthenticatedException {
		SecretStore store = SecretStore.getStore(getUser().getUserId());

		if (store == null) {
			return new UserData(getUser().getNickname(), null, null, 0l);
		}

		return new UserData(getUser().getNickname(), store.getSerial(), store.getToken(), store.getTimeOffset());
	}

	public UserData sync() throws NotAuthenticatedException {
		SecretStore store = SecretStore.getStore(getUser().getUserId());

		if (store == null || store.getSerial() == null) {
			return retrieveUserData();
		}

		Authenticator auth = new Authenticator();
		auth.setSerial(store.getToken(), store.getSerial(), store.getTimeOffset());
		try {
			auth.net_sync();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (auth.time_diff != store.getTimeOffset()) {
			SecretStore.setSecret(getUser().getUserId(), auth.str_serial, auth.str_token, auth.time_diff);
		}

		return retrieveUserData();
	}

	public UserData generateUserData() {
		Authenticator auth = new Authenticator();
		try {
			auth.net_enroll("US");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return storeSecret(auth.str_serial, auth.str_token, auth.time_diff);
	}
}
