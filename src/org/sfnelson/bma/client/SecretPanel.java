package org.sfnelson.bma.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SecretPanel extends Composite implements AsyncCallback<UserData> {

	private static SecretPanelUiBinder uiBinder = GWT
			.create(SecretPanelUiBinder.class);

	interface SecretPanelUiBinder extends UiBinder<Widget, SecretPanel> {
	}

	@UiField Label user;
	@UiField TextBox serial;
	@UiField TextBox token;
	@UiField TextBox offset;
	@UiField Button update;
	@UiField Button sync;
	@UiField Button generate;

	private KeyServiceAsync server;

	public SecretPanel(KeyServiceAsync server) {
		initWidget(uiBinder.createAndBindUi(this));

		this.server = server;

		server.retrieveUserData(this);
	}

	@UiHandler("update")
	void update(ClickEvent e) {
		server.storeSecret(serial.getText(), token.getText(), Long.parseLong(offset.getText()), this);
	}

	@UiHandler("sync")
	void sync(ClickEvent e) {
		server.sync(this);
	}

	@UiHandler("generate")
	void generate(ClickEvent e) {
		server.generateUserData(this);
	}

	public void onFailure(Throwable caught) {
		ErrorConsole.addMessage("Error accessing authenticator state", caught);
	}

	public void onSuccess(UserData result) {
		user.setText(result.user);
		serial.setText(result.serial);
		token.setText(result.token);
		offset.setText(String.valueOf(result.timeOffset));
	}
}
