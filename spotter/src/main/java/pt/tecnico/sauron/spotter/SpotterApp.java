package pt.tecnico.sauron.spotter;


import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloClientFrontend;
import pt.tecnico.sauron.silo.grpc.*;

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

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			String[] tokens = line.split(" ");

			if (tokens.length != 3) {
				System.out.println("Invalid format: (spot|trail) type id");
				continue;
			}

			if (tokens[0].equals("spot")){
				if (tokens[2].contains("*"))
					spotMatch(tokens[1], tokens[2], client);

				else
					spot(tokens[1], tokens[2], client);
			}

			else
				System.out.println("Invalid format: (spot|trail) type id");
		}
	}

	public static void spot(String type, String id, SiloClientFrontend client) {
		try {
			TrackRequest.Builder trackRequest = TrackRequest.newBuilder();

			if (type.equals("person"))
				trackRequest.setTarget(Target.PERSON);
			else if (type.equals("car"))
				trackRequest.setTarget(Target.CAR);
			else
				System.out.println("Invalid type value. Types available: car, person");

			trackRequest.setId(id);

			TrackResponse response = client.spot(trackRequest.build());

			System.out.printf("%s,%s,%s,,,%n",
					response.getObservation().getTarget().toString(),
					response.getObservation().getId(),
					response.getObservation().getTs().toString()
			);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " + e.getStatus().getDescription());
		}
	}

	public static void spotMatch(String type, String id, SiloClientFrontend client) {
		try {
			TrackRequest.Builder trackRequest = TrackRequest.newBuilder();

			if (type.equals("person"))
				trackRequest.setTarget(Target.PERSON);
			else if (type.equals("car"))
				trackRequest.setTarget(Target.CAR);
			else
				System.out.println("Invalid type value. Types avaliable: car, person");

			trackRequest.setId(id);

			TrackMatchResponse response = client.spotMatch(trackRequest.build());
			
			for (Observation obs : response.getObservationsList())
				System.out.printf("%s,%s,%s,,,%n",
						obs.getTarget().toString(),
						obs.getId(),
						obs.getTs().toString()
				);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " + e.getStatus().getDescription());
		}
	}

}
