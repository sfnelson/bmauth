package org.sfnelson.bma.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class UseExistingPanel extends Composite implements AsyncCallback<UserData> {

    private static UseExistingPanelUiBinder uiBinder = GWT.create(UseExistingPanelUiBinder.class);

    interface UseExistingPanelUiBinder extends UiBinder<Widget, UseExistingPanel> {
    }

    interface Style extends CssResource {
        String error();
        String init();
        String ready();
    }

    private static final String INVALID = "invalid";
    private static final String READY = "ready";
    private static final String SETTING = "saving";
    private static final String DEFAULT_SERIAL = "US-0000-0000-0000";
    private static final String DEFAULT_SECRET = "0000000000000000000000000000000000000000";

    private KeyServiceAsync service;
    private Bma parent;

    @UiField Style style;
    @UiField TextBox serial;
    @UiField TextBox secret;
    @UiField Anchor submit;

    public UseExistingPanel(KeyServiceAsync service, Bma parent) {
        initWidget(uiBinder.createAndBindUi(this));

        this.service = service;
        this.parent = parent;

        secret.setText(DEFAULT_SECRET);
        serial.setText(DEFAULT_SERIAL);
        submit.setText(INVALID);

        validate();
    }

    @UiHandler("serial")
    void serialKeyUp(KeyUpEvent ev) {
        validate();

        if (ev.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            submitOnClick(null);
        }
    }

    @UiHandler("secret")
    void secretKeyUp(KeyUpEvent ev) {
        validate();

        if (ev.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            submitOnClick(null);
        }
    }

    @UiHandler("secret")
    void secretOnFocus(FocusEvent ev) {
        if (secret.getText().equals(DEFAULT_SECRET)) {
            secret.setText("");
        }
    }

    @UiHandler("serial")
    void serialOnFocus(FocusEvent ev) {
        if (serial.getText().equals(DEFAULT_SERIAL)) {
            serial.setText("");
        }
    }

    @UiHandler("secret")
    void secretOnBlur(BlurEvent ev) {
        if (secret.getText().equals("")) {
            secret.setText(DEFAULT_SECRET);
        }

        validate();
    }

    @UiHandler("serial")
    void serialOnBlur(BlurEvent ev) {
        if (serial.getText().equals("")) {
            serial.setText(DEFAULT_SERIAL);
        }

        validate();
    }

    @UiHandler("submit")
    void submitOnClick(ClickEvent ev) {
        if (validate()) {
            service.storeSecret(serial.getText(), secret.getText(), 0, this);
            submit.setText(SETTING);
        }
    }

    public boolean validate() {
        boolean ok = true;

        if (validateSerial()) {
            serial.removeStyleName(style.error());
        } else {
            ok = false;
            serial.addStyleName(style.error());
        }

        if (serial.getText().equals(DEFAULT_SERIAL)) {
            serial.addStyleName(style.init());
            ok = false;
        } else {
            serial.removeStyleName(style.init());
        }

        if (validateSecret()) {
            secret.removeStyleName(style.error());
        } else {
            ok = false;
            secret.addStyleName(style.error());
        }

        if (secret.getText().equals(DEFAULT_SECRET)) {
            secret.addStyleName(style.init());
            ok = false;
        } else {
            secret.removeStyleName(style.init());
        }

        if (ok) {
            submit.addStyleName(style.ready());
            submit.setText(READY);
        } else {
            submit.removeStyleName(style.ready());
            submit.setText(INVALID);
        }

        return ok;
    }

    private boolean validateSerial() {
        return serial.getText().matches("^(US|KR|EU)-[0-9]{4}-[0-9]{4}-[0-9]{4}$");
    }

    private boolean validateSecret() {
        return secret.getText().matches("^[0-9A-Fa-f]{40}$");
    }

    public void onFailure(Throwable caught) {
        ErrorConsole.addMessage("Unable to save keys", caught);
        validate();
    }

    public void onSuccess(UserData result) {
        removeFromParent();
        parent.showKey(result);
    }
}
