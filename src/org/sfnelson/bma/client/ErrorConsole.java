package org.sfnelson.bma.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ErrorConsole extends Composite {

	private static ErrorConsoleUiBinder uiBinder = GWT
			.create(ErrorConsoleUiBinder.class);

	interface ErrorConsoleUiBinder extends UiBinder<Widget, ErrorConsole> {
	}

	@UiField Label message;

	private static ErrorConsole console;

	public ErrorConsole() {
		initWidget(uiBinder.createAndBindUi(this));

		hide();

		console = this;
	}

	private void setMessage(String message) {
		this.message.setText(message);
		this.getStyleElement().getStyle().setVisibility(Visibility.VISIBLE);
		timer.schedule(5000);
	}

	private void hide() {
		this.getStyleElement().getStyle().setVisibility(Visibility.HIDDEN);
	}

	public static void addMessage(String message, Throwable ex) {
		console.setMessage(message);
	}

	private Timer timer = new Timer() {
		@Override
		public void run() {
			hide();
		}
	};
}
