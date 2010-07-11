package org.sfnelson.bma.client;

import java.io.Serializable;

public class UserData implements Serializable {
	private static final long serialVersionUID = 3512029399536298861L;

	public String user;
	public String serial;
	public String logout;
	public Long timeOffset;

	public UserData() {}

	public UserData(String user, String logout, String serial, Long timeOffset) {
		this.user = user;
		this.logout = logout;
		this.serial = serial;
		this.timeOffset = timeOffset;
	}
}
