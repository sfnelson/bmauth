package org.sfnelson.bma.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Bma implements EntryPoint {

	private final KeyServiceAsync keyService = GWT.create(KeyService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		RootPanel.get().add(new ErrorConsole());
		RootPanel.get().add(new SecretPanel(keyService));
		RootPanel.get().add(new KeyField(keyService));

	}
}
