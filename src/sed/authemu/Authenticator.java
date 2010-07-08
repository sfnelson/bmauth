package sed.authemu;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Authenticator {
	/*--- Constants ---*/
	/// Example: str_consts.get("EU").get("auth-url-enroll")
	public static Map<String, Map<String,String>> str_consts;

	/*--- Variables that define the Authenticator instance (serial, ...) ---*/
	/// Difference between server & local time [ms]
	public long time_diff;

	/// Serial number, {EU|US}-NNNN-NNNN-NNNN
	public String str_serial;

	/// Token, 40 hexadecimal characters
	public String str_token;

	/*--- Internal state variables ---*/
	/// 37-byte XOR key, used to encrypt Token+Serial
	byte[] xor_key_random37;

	/// Token in binary form (20 bytes)
	byte[] token;

	/// last generated key
	String last_key_string;

	/// time of last generated key
	long last_key_time;

	public Authenticator() {
		str_consts = new HashMap<String, Map<String, String>>();
		str_consts.put("US", new HashMap<String,String>());
		str_consts.put("EU", new HashMap<String,String>());

		str_consts.get("US").put("auth-url-enroll",   "http://m.us.mobileservice.blizzard.com/enrollment/enroll.htm");
		str_consts.get("US").put("auth-url-time",     "http://m.us.mobileservice.blizzard.com/enrollment/time.htm");
		str_consts.get("US").put("auth-url-setup",    "http://www.battle.net/bma");
		str_consts.get("US").put("auth-url-acctmgmt", "http://www.battle.net/account/management");
		str_consts.get("US").put("auth-region",       "US");
		str_consts.get("US").put("auth-phone-model",  "Motorola RAZR v3");

		str_consts.get("EU").put("auth-url-enroll",   "http://m.eu.mobileservice.blizzard.com/enrollment/enroll.htm");
		str_consts.get("EU").put("auth-url-time",     "http://m.eu.mobileservice.blizzard.com/enrollment/time.htm");
		str_consts.get("EU").put("auth-url-setup",    "http://eu.battle.net/bma");
		str_consts.get("EU").put("auth-url-acctmgmt", "http://eu.battle.net/account/management");
		str_consts.get("EU").put("auth-region",       "EU");
		str_consts.get("EU").put("auth-phone-model",  "Motorola RAZR v3");

		last_key_time = 0;
		last_key_string = null;
	}

	String getRegion() {
		return (str_serial == null) ? "" : str_serial.substring(0,2);
	}

	/**
	 * @return time [ms] since the last key change
	 */
	public long timeSinceLastKeyChange() {
		return (System.currentTimeMillis() + time_diff) % 30000;
	}

	/**
	 * @return The current authentication key
	 */
	public String getAuthKey() {
		long time_now = System.currentTimeMillis();
		long time_div = (time_now  + time_diff) / 30000;
		long time_last_div = (last_key_time + time_diff) / 30000;
		if( time_div != time_last_div ) {
			last_key_time = time_now;
			return getAuthKeyString(time_div);
		}
		return last_key_string;
	}

	public void setSerial(String s_token, String s_serial, long t_diff) {
		this.time_diff = t_diff;
		checkAndStoreTokenSerial(s_token, s_serial);
		if( s_token == null || s_serial == null )
			throw new RuntimeException("invalid token or serial number");
		parseTokenString();
	}

	/**
	 * @param time_divided (currentTimeMillis() + time_offset) / 30000
	 * @return The current authentication key
	 */
	private String getAuthKeyString(long time_divided) {
		int i = AuthKeyGen.calcAuthKey(token, time_divided);
		String authkey = "";
		for (int j = 0; j < 8; j++) {
			authkey = i % 10 + authkey;
			i /= 10;
		}

		return authkey;
	}

	private void checkAndStoreTokenSerial(String token, String serial) {
		boolean valid_token = false;
		boolean valid_serial = false;
		if (token.length() == 40) {
			valid_token = true;
			int i = 0;
			do {
				if (i >= 40) {
					break;
				}
				char c = token.charAt(i);
				if (!(valid_token &= c >= '0' && c <= '9' || c >= 'a' && c <= 'f'
						|| c >= 'A' && c <= 'F')) {
					break;
				}
				i++;
			} while (true);
		}
		if (serial.length() == 17) {
			valid_serial = true;
			int i = 0;
			do {
				if (i >= 17) {
					break;
				}
				char c = serial.charAt(i);
				if (i < 2) {
					valid_serial &= c == 'E' || c == 'U' || c == 'S';
				} else {
					valid_serial &= c >= '0' && c <= '9' || c == '-';
				}
				if (!valid_serial) {
					break;
				}
				i++;
			} while (true);
		}
		if (valid_token && valid_serial) {
			str_token = token;
			str_serial = serial;
			return;
		} else {
			str_token = null;
			str_serial = null;
			return;
		}
	}

	/**
	 * Converts str_token (40 hex chars) to token (20 bytes)
	 */
	private void parseTokenString() {
		if (str_serial != null && str_token != null) {
			byte btoken[] = new byte[20];
			for (int i = 0; i < str_token.length(); i += 2) {
				byte b = (byte) ((hexcharToNibble(str_token.charAt(i)) << 4) + hexcharToNibble(str_token
						.charAt(i + 1)));
				btoken[i >> 1] = b;
			}

			token = btoken;
			return;
		}
	}

	/// only related to the first contact, generates part of the POST data
	byte[] generateEnrollmentMash(String region) {
		xor_key_random37 = BlizzCrypt.genRandomBytes(37);

		byte mash[] = new byte[55];
		System.arraycopy(xor_key_random37, 0, mash, 0, 37);
		byte tmp[];
		System.arraycopy(tmp = region.getBytes(), 0, mash, 37, Math.min(tmp.length, 2));
		System.arraycopy(tmp = str_consts.get(region).get("auth-phone-model").getBytes(), 0, mash, 39, Math
				.min(tmp.length, 16));
		return mash;
	}

    /**
     * @param abyte array of bytes
     * @param offset offset
     * @return long integer (QWORD), read from abyte[offset]
     */
    static long bytesToLong(byte abyte[], int offset)
    {
        long result = 0L;
        for(int k = offset; k < offset + 8; k++)
        {
            result <<= 8;
            long l1 = abyte[k] & 0xff;
            result += l1;
        }

        return result;
    }

	/**
	 * @param nibble [0..15]
	 * @return hexadecimal character
	 */
	private static char nibbleToHexChar(int nibble) {
		if (nibble < 10)
			return (char) (48 + nibble);
		else
			return (char) (97 + (nibble - 10));
	}

	/**
	 * @param c hexadecimal character
	 * @return nibble [0..15]
	 */
	private static int hexcharToNibble(char c) {
		if (c >= '0' && c <= '9')
			return c - 48;
		if (c >= 'a' && c <= 'f')
			return 10 + (c - 97);
		if (c >= 'A' && c <= 'F')
			return 10 + (c - 65);
		else
			return 0;
	}

	/**
	 * Creates a new authenticator.
	 * @throws IOException
	 */
	public void net_enroll(String region) throws IOException {
		if(!region.equals("EU") && !region.equals("US"))
			// FIXME: what's a suitable Exception class?
			throw new RuntimeException("invalid region code");

		// Enroll request
		URL url_enroll = new URL(str_consts.get(region).get("auth-url-enroll"));
		HttpURLConnection conn = (HttpURLConnection) url_enroll.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-type", "application/octet-stream");
		conn.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
		conn.setReadTimeout(10000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();

		// Generate enrollment bytes
		byte[] data_out;
		BlizzCrypt.prepareEnrollmentBytes(generateEnrollmentMash(region));
		while(BlizzCrypt.round() != 12) {}
		data_out = BlizzCrypt.getOTPBytes();

		// Send enrollment bytes
		OutputStream out_stream = conn.getOutputStream();
		out_stream.write(data_out);
		out_stream.close();

		// Read reply
		byte[] b_servertime = new byte[8];
		byte[] b_token_and_serial = new byte[37];

		InputStream inp_stream = conn.getInputStream();
		inp_stream.read(b_servertime, 0, 8);
		inp_stream.read(b_token_and_serial, 0, 37);
		inp_stream.close();
		conn.disconnect();

		// Decrypt reply
		byte[] b_token      = new byte[20];
		byte[] b_serial     = new byte[17];
		time_diff = bytesToLong(b_servertime, 0) - System.currentTimeMillis();
		BlizzCrypt.xorEncryptArray(b_token_and_serial, xor_key_random37);
		System.arraycopy(b_token_and_serial, 0, b_token, 0, 20);
		System.arraycopy(b_token_and_serial, 20, b_serial, 0, 17);
		char c_token[] = new char[40];
		for (int i = 0; i < 20; i++) {
			int nib_up = (b_token[i] & 0xf0) >> 4;
			int nib_lo = b_token[i] & 0xf;
			c_token[i * 2] = nibbleToHexChar(nib_up);
			c_token[i * 2 + 1] = nibbleToHexChar(nib_lo);
		}

		setSerial(new String(c_token), new String(b_serial, 0, 17), time_diff);
	}

	/**
	 * Updates the server<->client time difference.
	 * @throws IOException
	 */
	public void net_sync() throws IOException {
		URL url_sync = new URL(str_consts.get(getRegion()).get("auth-url-time"));
		HttpURLConnection conn = (HttpURLConnection) url_sync.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "application/octet-stream");
		conn.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
		conn.setReadTimeout(10000);
		conn.setDoInput(true);
		conn.connect();

		// retrieve time
		byte[] b_servertime = new byte[8];
		InputStream inp_stream = conn.getInputStream();
		inp_stream.read(b_servertime, 0, 8);
		inp_stream.close();
		conn.disconnect();

		time_diff = bytesToLong(b_servertime, 0) - System.currentTimeMillis();
	}

}
