package pt.tecnico.sauron.silo.client;


import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloClientApp {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(SiloClientApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", SiloClientApp.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final int instance;

		if (args.length == 3) {
			instance = Integer.parseInt(args[2]);
			if (instance < 1 || instance > 9) {
				System.out.println("Instance must be between 1 and 9");
				return;
			}
		}
		else
			instance = -1;

		SiloClientFrontend client = new SiloClientFrontend(host, port, instance);
		client.ping();
		System.out.println("Received ping");
	}
	
}
