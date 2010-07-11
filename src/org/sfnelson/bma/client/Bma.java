package org.sfnelson.bma.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Bma implements EntryPoint, AsyncCallback<UserData> {

	private final KeyServiceAsync keyService = GWT.create(KeyService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		RootPanel.get().add(new ErrorConsole());

		keyService.retrieveUserData(this);
	}

	public void onFailure(Throwable caught) {
		ErrorConsole.addMessage("Unable to retrieve user data from server", caught);
	}

	public void onSuccess(UserData result) {
		if (result.serial == null) {
			showWelcome();
		} else {
			showKey(result);
		}
	}

	public void showWelcome() {
	    RootPanel.get().add(new WelcomePanel(this, keyService));
	}

    public void showExisting() {
        RootPanel.get().add(new UseExistingPanel(keyService, this));
    }

    public void showKey(UserData data) {
        RootPanel.get().add(new KeyField(this, data, keyService));
    }
}
