package org.sfnelson.bma.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class SecretStore {

	private static final PersistenceManagerFactory PMF =
		JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public static SecretStore getStore(String userId) {
		Key key = KeyFactory.createKey(SecretStore.class.getSimpleName(), userId);

		SecretStore store = null;

		PersistenceManager pm = PMF.getPersistenceManager();
		try {
			store = pm.getObjectById(SecretStore.class, key);
		} catch (Exception ex) {
		} finally {
			pm.close();
		}

		return store;
	}

	public static void setSecret(String userId, String serial, String token, long timeOffset) {
		Key key = KeyFactory.createKey(SecretStore.class.getSimpleName(), userId);

		PersistenceManager pm = PMF.getPersistenceManager();
		pm.setDetachAllOnCommit(true);

		SecretStore store = null;
		try {
			store = pm.getObjectById(SecretStore.class, key);
		} catch (Exception ex) {}

		if (store == null) {
			store = new SecretStore(key, pm);
		}

		store.serial = serial;
		store.token = token;
		store.timeOffset = timeOffset;
		pm.close();
	}

	public SecretStore(Key key, PersistenceManager pm) {
		this.key = key;

		pm.makePersistent(this);
	}

	@SuppressWarnings("unused")
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String serial;

	@Persistent
	private String token;

	@Persistent
	private Long timeOffset;

	String getSerial() {
		return serial;
	}

	String getToken() {
		return token;
	}

	Long getTimeOffset() {
		return timeOffset;
	}
}
