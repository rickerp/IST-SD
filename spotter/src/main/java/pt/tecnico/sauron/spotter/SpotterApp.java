package pt.tecnico.sauron.spotter;


import pt.tecnico.sauron.silo.client.SiloClientFrontend;
import pt.tecnico.sauron.silo.grpc.Target;
import pt.tecnico.sauron.silo.grpc.TrackRequest;
import pt.tecnico.sauron.silo.grpc.TrackResponse;

import java.util.Scanner;

public class SpotterApp {

	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", SpotterApp.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		SiloClientFrontend client = new SiloClientFrontend(host, port);

		Scanner scanner = new Scanner(System.in);

		TrackRequest.Builder trackRequest = TrackRequest.newBuilder();

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			String[] tokens = line.split(" ");

			if (tokens.length != 3) {
				System.out.println("Invalid format: (spot|trail) type id");
				continue;
			}

			if (tokens[0].equals("spot")){
				if (tokens[2].contains("*")){
					; // TODO: spot with match
				}

				if (tokens[1].equals("person"))
					trackRequest.setTarget(Target.PERSON);
				else if (tokens[1].equals("car"))
					trackRequest.setTarget(Target.CAR);
				else{
					System.out.println("Invalid type value. Types available: car, person");
					continue;
				}

				trackRequest.setId(tokens[2]);

				TrackResponse response = client.spot(trackRequest.build());

				System.out.printf("%s,%s,%s,,,",
						response.getObservation().getTarget().toString(),
						response.getObservation().getId(),
						response.getObservation().getTs().toString()
				);
			}
		}
	}

}