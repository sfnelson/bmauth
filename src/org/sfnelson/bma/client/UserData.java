package org.sfnelson.bma.client;

import java.io.Serializable;

public class UserData implements Serializable {
	private static final long serialVersionUID = 3512029399536298861L;

	public String user;
	public String serial;
	public String token;
	public Long timeOffset;

	public UserData() {}

	public UserData(String user, String serial, String token, Long timeOffset) {
		this.user = user;
		this.serial = serial;
		this.token = token;
		this.timeOffset = timeOffset;
	}
}
