package pt.tecnico.sauron.spotter;


import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloClientFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.time.ZoneId;
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

		boolean hasNextLine = true;
		while (hasNextLine) {

			System.out.print("> ");
			System.out.flush();

			hasNextLine = scanner.hasNextLine();
			if (!hasNextLine) break;

			String line = scanner.nextLine();

			String[] tokens = line.split(" ");

			if (tokens[0].equals("spot")){
				if (tokens.length != 3) {
					System.out.println("Wrong arguments. Type help to read the documentation");
					continue;
				}
				if (tokens[2].contains("*"))
					spotMatch(tokens[1], tokens[2], client);
				else
					spot(tokens[1], tokens[2], client);
			}

			else if (tokens[0].equals("trail")) {
				if (tokens.length != 3) {
					System.out.println("Wrong arguments. Type help to read the documentation");
					continue;
				}

				trail(tokens[1], tokens[2], client);
			}

			else if (tokens[0].equals("ping")) {
				if (tokens.length != 1) {
					System.out.println("Wrong arguments. Type help to read the documentation");
					continue;
				}

				client.ping();
				System.out.println("Server is running");
			}

			else if (tokens[0].equals("clear")) {
				if (tokens.length != 1) {
					System.out.println("Wrong arguments. Type help to read the documentation");
					continue;
				}

				client.clear();
			}

			else if (tokens[0].equals("help")) {
				if (tokens.length != 1) {
					System.out.println("Wrong arguments. Type help to read the documentation");
					continue;
				}

				System.out.println("Available commands:\n" +
						"spot TYPE ID      - prints the last observation of an object of a TYPE(person or car) with the respective ID. Where ID can have * to show all matchs\n" +
						"trail TYPE ID     - prints all the observations of an object of a TYPE(person or car) with the respective ID\n" +
						"ping              - checks if the server is running\n" +
						"clear             - clears all the state of the server"
						);
			}

			else
				System.out.println("Type help to see the documentation");
		}
	}

	public static void spot(String type, String id, SiloClientFrontend client) {
		TrackRequest.Builder trackRequest = TrackRequest.newBuilder();

		Target target = parseTarget(type);
		if (target == null) {
			System.out.println("Invalid type value. Types available: car, person");
			return;
		}

		trackRequest.setTarget(target);
		trackRequest.setId(id);

		try {
			TrackResponse response = client.track(trackRequest.build());

			printObservation(response.getObservation(), client);

		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " + e.getStatus().getDescription());
		}
	}

	public static void spotMatch(String type, String id, SiloClientFrontend client) {
		TrackMatchRequest.Builder trackRequest = TrackMatchRequest.newBuilder();

		Target target = parseTarget(type);
		if (target == null) {
			System.out.println("Invalid type value. Types available: car, person");
			return;
		}

		trackRequest.setTarget(target);
		trackRequest.setId(id);

		try {
			TrackMatchResponse response = client.trackMatch(trackRequest.build());

			if (response.getObservationsList().size() == 0) {
				System.out.println("");
				return;
			}

			for (Observation observation : response.getObservationsList())
				printObservation(observation, client);

		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " + e.getStatus().getDescription());
		}
	}

	public static void trail(String type, String id, SiloClientFrontend client) {
		TraceRequest.Builder trackRequest = TraceRequest.newBuilder();

		Target target = parseTarget(type);
		if (target == null) {
			System.out.println("Invalid type value. Types available: car, person");
			return;
		}

		trackRequest.setTarget(target);
		trackRequest.setId(id);

		try {
			TraceResponse response = client.trace(trackRequest.build());

			if (response.getObservationsList().size() == 0) {
				System.out.println("");
				return;
			}

			for (Observation observation : response.getObservationsList())
				printObservation(observation, client);

		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " + e.getStatus().getDescription());
		}
	}

	public static void printObservation(Observation observation, SiloClientFrontend client) {
		String cameraName = observation.getCameraName();
		if (cameraName.equals("")) {
			System.out.println("");
			return;
		}

		CamInfoResponse camInfo = client.camInfo(CamInfoRequest.newBuilder().setCameraName(cameraName).build());

		System.out.printf("%s,%s,%s,%s,%s,%s%n",
				observation.getTarget().toString().toLowerCase(),
				observation.getId(),
				Instant.ofEpochSecond(observation.getTs().getSeconds()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString(),
				observation.getCameraName(),
				camInfo.getLatitude(),
				camInfo.getLongitude()
		);
	}

	public static Target parseTarget(String target) {
		if (target.equals("person"))
			return Target.PERSON;
		else if (target.equals("car"))
			return Target.CAR;
		else
			return null;
	}
}
