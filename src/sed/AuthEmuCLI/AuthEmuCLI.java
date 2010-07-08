package sed.AuthEmuCLI;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import sed.authemu.Authenticator;


public class AuthEmuCLI {

	Authenticator auth = null;
	Preferences prefs = null;
	
	public static void main(String[] args) {
		AuthEmuCLI emu = new AuthEmuCLI();
		emu._main(args);
	}

	public void _main(String[] args) {
		auth = new Authenticator();

		// preferences are located at ~/.java/.userPrefs/FooBMA/prefs.xml
		prefs = Preferences.userNodeForPackage(AuthEmuCLI.class);

		// read command line arguments
		String[] _commands = { "create-eu", "create-us", "oneshot", "run", "sync" };
		List<String> commands = Arrays.asList(_commands);
		if(args.length != 1 || !commands.contains(args[0])) {
			System.out.println("Usage: AuthEmuCLI COMMAND");
			System.out.println("Commands:");
			System.out.println("  create-eu     create a new Blizzard Authenticator, region: EU");
			System.out.println("  create-us     create a new Blizzard Authenticator, region: US");
			System.out.println();
			System.out.println("  oneshot       print the current key");
			System.out.println("  run           print keys indefinitely");
			System.out.println("  sync          resynchronize time");
			System.exit(1);
		}
		String command = args[0].toUpperCase();

		// create
		if(command.startsWith("CREATE")) {
			try {
				String region = command.substring(7,9);

				System.out.printf("* Generating a new Authenticator for the region: %s\n", region);
				System.out.printf("* Requesting serial number...\n");

				auth.net_enroll(region);

				prefs.put("token", auth.str_token);
				prefs.put("serial", auth.str_serial);
				prefs.putLong("time_diff", auth.time_diff);

				System.out.println();
				printSerial();
				System.out.println();
				System.out.printf("In order to use the Authenticator, you need to associate the serial number to your Battle.Net account.\n");
				System.out.printf("Visit %s, enter the serial number, and click Attach.\n",
					Authenticator.str_consts.get(region).get("auth-url-setup"));

				System.exit(0);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
		}

		// Use
		try {
			loadPreferences(prefs);
			System.out.printf("Using saved authenticator.\n\n");
			printSerial();
			System.out.println();
		} catch (BackingStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(command.equals("ONESHOT")) {
			System.out.printf("Key: %s\n", auth.getAuthKey());
			System.exit(0);
		}

		if(command.equals("RUN")) {
			try {
				printKeysLoop();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(command.equals("SYNC")) {
			try {
				auth.net_sync();
				prefs.putLong("time_diff", auth.time_diff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.printf("New time difference [ms]: %d\n", auth.time_diff);
		}
	}

	private void loadPreferences(Preferences p) throws BackingStoreException {
		String s_token   = p.get("token", null);
		String s_serial  = p.get("serial", null);
		long time_diff = p.getLong("time_diff", 0);
		if(s_token == null || s_serial == null) {
			p.clear();
			System.err.println("No token generated yet.");
			System.exit(1);
		}
		auth.setSerial(s_token, s_serial, time_diff);
		if(auth.str_serial == null || auth.str_token == null) {
			System.err.println("Invalid token stored in preferences. generate a new one.");
			System.exit(1);
		}
	}

	private void printSerial() {
		System.out.printf("Serial: %s\n", auth.str_serial);
		System.out.printf("Token:  %s\n", auth.str_token);
		System.out.printf("Client/server time difference [ms]: %d\n", auth.time_diff);
	}

	public void printKeysLoop() throws InterruptedException {
		long t_elapsed, t_elapsed_old = 0;

		System.out.printf("Key: %s\n", auth.getAuthKey());

		while("hell" != "frozen") {
			t_elapsed = auth.timeSinceLastKeyChange() / 1000;
			if( t_elapsed > t_elapsed_old ) {
				for(int i=0; i < (t_elapsed - t_elapsed_old); i++) {
					System.out.printf(".");
				}
				t_elapsed_old = t_elapsed;
			}
			else if( t_elapsed < t_elapsed_old ) {
				System.out.printf("\nkey: %s\n", auth.getAuthKey());
				t_elapsed_old = t_elapsed;
			}
			Thread.sleep(500);
		}
	}

}
