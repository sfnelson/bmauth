package org.sfnelson.bma.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class KeyField extends Composite implements AsyncCallback<String> {

	private static KeyFieldUiBinder uiBinder = GWT
			.create(KeyFieldUiBinder.class);

	interface KeyFieldUiBinder extends UiBinder<Widget, KeyField> {
	}

	private KeyServiceAsync server;

	@UiField TextBox key;


	public KeyField(KeyServiceAsync keyServer) {
		initWidget(uiBinder.createAndBindUi(this));

		this.server = keyServer;

		refresh();

		Timer t = new Timer() {
			@Override
			public void run() {
				refresh();
			}
		};
		t.scheduleRepeating(5000);
	}

	private void refresh() {
		server.retrieveKey(this);
	}

	public void onFailure(Throwable caught) {
		ErrorConsole.addMessage("Error calculating authenticator key", caught);
	}

	public void onSuccess(String result) {
		key.setText(result);

		if (hasFocus) {
			key.selectAll();
		}
	}

	private boolean hasFocus = false;

	@UiHandler("key")
	public void focused(FocusEvent ev) {
		hasFocus = true;
		key.selectAll();
	}

	@UiHandler("key")
	public void blurred(BlurEvent ev) {
		hasFocus = false;
	}

	@Override
	public void onLoad() {
		super.onLoad();

		key.setFocus(true);
	}
}
