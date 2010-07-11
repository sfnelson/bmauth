package org.sfnelson.bma.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class KeyField extends Composite implements AsyncCallback<Key> {

    private static KeyFieldUiBinder uiBinder = GWT
    .create(KeyFieldUiBinder.class);

    interface KeyFieldUiBinder extends UiBinder<Widget, KeyField> {
    }

    private static final int TICK = 100;
    private static final String LOGOUT_TEXT = "log out";
    private static final String DELETE_TEXT = "delete";
    private static final String DELETING_TEXT = "deleting...";

    private final KeyServiceAsync server;
    private final UserData data;
    private final Bma parent;
    private final Timer refreshTimer;
    private final Timer counterTimer;

    private Key lastKey;

    @UiField Label serial;
    @UiField TextBox key;
    @UiField Anchor logout;
    @UiField Anchor delete;
    @UiField FlowPanel time;

    public KeyField(Bma parent, UserData data, KeyServiceAsync keyServer) {
        initWidget(uiBinder.createAndBindUi(this));

        this.parent = parent;
        this.server = keyServer;
        this.data = data;

        serial.setText(data.serial);
        logout.setText(LOGOUT_TEXT);
        delete.setText(DELETE_TEXT);

        refreshTimer = new Timer() {
            @Override
            public void run() {
                refresh();
            }
        };

        counterTimer = new Timer() {
            @Override
            public void run() {
                countdown();
            }
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();

        refreshTimer.schedule(1);
        counterTimer.scheduleRepeating(TICK);

        key.setFocus(true);
    }

    @Override
    public void onUnload() {
        counterTimer.cancel();
        refreshTimer.cancel();

        super.onUnload();
    }

    private void refresh() {
        server.retrieveKey(this);
    }

    private void countdown() {
        if (lastKey != null) {
            double remaining = lastKey.valid;
            time.getElement().getStyle().setWidth(remaining / 5000, Unit.EM);
            lastKey.valid -= TICK;
        }
    }

    public void onFailure(Throwable caught) {
        ErrorConsole.addMessage("Error calculating authenticator key", caught);

        refreshTimer.schedule(30000);
    }

    public void onSuccess(Key result) {
        this.lastKey = result;

        if (!this.isAttached()) {
            return;
        }

        key.setText(result.value);

        if (hasFocus) {
            key.selectAll();
        }

        refreshTimer.schedule(result.valid);
    }

    private boolean hasFocus = false;

    @UiHandler("key")
    public void onFocus(FocusEvent ev) {
        hasFocus = true;
    }

    @UiHandler("key")
    public void onBlur(BlurEvent ev) {
        hasFocus = false;
    }

    @UiHandler("key")
    public void onClick(ClickEvent ev) {
        key.selectAll();
    }

    @UiHandler("logout")
    public void logout(ClickEvent ev) {
        String loc = data.logout + Window.Location.getQueryString();
        Window.Location.assign(loc);
    }

    @UiHandler("delete")
    public void delete(ClickEvent ev) {
        server.deleteUserData(new AsyncCallback<UserData>() {

            public void onFailure(Throwable caught) {
                ErrorConsole.addMessage("Error deleting user data", caught);
                delete.setText(DELETE_TEXT);
            }

            public void onSuccess(UserData result) {
                KeyField.this.removeFromParent();
                parent.showWelcome();
            }
        });
        delete.setText(DELETING_TEXT);
    }
}
