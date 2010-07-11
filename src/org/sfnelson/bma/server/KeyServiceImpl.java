package org.sfnelson.bma.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.sfnelson.bma.client.Key;
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

	public Key retrieveKey() throws NotAuthenticatedException {
		SecretStore store = SecretStore.getStore(getUser().getUserId());

		if (store == null || store.getSerial() == null) {
			return null;
		}

		Authenticator auth = new Authenticator();
		auth.setSerial(store.getToken(), store.getSerial(), store.getTimeOffset());

		Key k = new Key();
		k.value = auth.getAuthKey();
		k.valid = (int) (30000 - auth.timeSinceLastKeyChange());
		return k;
	}

	public UserData storeSecret(String serial, String token, long timeOffset) throws NotAuthenticatedException {
		SecretStore.setSecret(getUser().getUserId(), serial, token, timeOffset);

		return retrieveUserData();
	}

	public UserData retrieveUserData() throws NotAuthenticatedException {
		SecretStore store = SecretStore.getStore(getUser().getUserId());

		String logout = UserServiceFactory.getUserService().createLogoutURL("/index.html");

		if (store == null) {
			return new UserData(getUser().getNickname(), logout, null, 0l);
		}

		return new UserData(getUser().getNickname(), logout,
		        store.getSerial(), store.getTimeOffset());
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

	public UserData deleteUserData() {
	    SecretStore data = SecretStore.getStore(getUser().getUserId());

	    sendMail(getUser(), data);

	    return storeSecret(null, null, 0);
	}

	private void sendMail(User user, SecretStore data) {
	    Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        String msgBody = "You have deleted your mobile authenticator account successfully.\n\n"
            + "In case you want to reactivate it later, we've attached your details to this "
            + "email. You can enter them again at https://bmauth.appspot.com/ or you "
            + "can also use them with one of many other authenticator emulators.\n\n"
            + "Serial:\t" + data.getSerial() + "\n"
            + "Token:\t" + data.getToken() + "\n\n";

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("bmauth@sfnelson.org", "Mobile Authenticator Administrator"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress(user.getEmail()));
            msg.setSubject("Mobile Authenticator Account Reset");
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	}
}
