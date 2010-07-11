package org.sfnelson.bma.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class WelcomePanel extends Composite implements AsyncCallback<UserData> {

	private static WelcomePanelUiBinder uiBinder = GWT
			.create(WelcomePanelUiBinder.class);

	interface WelcomePanelUiBinder extends UiBinder<Widget, WelcomePanel> {
	}

	private static final String EXISTING_TEXT = "use existing";
	private static final String GENERATE_TEXT = "generate new";
	private static final String GENERATING_TEXT = "generating...";

	private Bma parent;
	private KeyServiceAsync service;

	@UiField Anchor existing;
	@UiField Anchor generate;

	public WelcomePanel(Bma parent, KeyServiceAsync service) {
		initWidget(uiBinder.createAndBindUi(this));

		this.parent = parent;
		this.service = service;

		existing.setText(EXISTING_TEXT);
		generate.setText(GENERATE_TEXT);
	}

	@UiHandler("existing")
	public void existing(ClickEvent ev) {
	    parent.showExisting();

	    removeFromParent();
	}

	@UiHandler("generate")
	public void generate(ClickEvent ev) {
	    service.generateUserData(this);
	    generate.setText(GENERATING_TEXT);
	}

    public void onFailure(Throwable caught) {
        ErrorConsole.addMessage("Unable to generate keys", caught);
        generate.setText(GENERATE_TEXT);
    }

    public void onSuccess(UserData result) {
        removeFromParent();
        parent.showKey(result);
    }
}
