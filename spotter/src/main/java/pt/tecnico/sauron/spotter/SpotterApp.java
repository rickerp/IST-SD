package pt.tecnico.sauron.spotter;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloClientFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SpotterApp {

	public static final String wrongArgsMessage = "Wrong arguments. Type help to read the documentation";
	public static final String wrongTypeMessage = "Invalid type value. Types available: car, person";
	public static final String exceptionMessage = "Caught exception with description: ";

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

		Scanner scanner = new Scanner(System.in);

		boolean hasNextLine = true;
		while (hasNextLine) {

			System.out.print("> ");
			System.out.flush();

			hasNextLine = scanner.hasNextLine();
			if (!hasNextLine)
				break;

			String line = scanner.nextLine();

			String[] tokens = line.split(" ");

			if (tokens[0].equals("spot")) {
				if (tokens.length != 3) {
					System.out.println(wrongArgsMessage);
					continue;
				}
				if (tokens[2].contains("*"))
					spotMatch(tokens[1], tokens[2], client);
				else
					spot(tokens[1], tokens[2], client);
			}

			else if (tokens[0].equals("trail")) {
				if (tokens.length != 3) {
					System.out.println(wrongArgsMessage);
					continue;
				}

				trail(tokens[1], tokens[2], client);
			}

			else if (tokens[0].equals("ping")) {
				if (tokens.length != 1) {
					System.out.println(wrongArgsMessage);
					continue;
				}

				client.ping();
				System.out.println("Server is running");
			}

			else if (tokens[0].equals("init")) {
				if (tokens.length != 1) {
					System.out.println(wrongArgsMessage);
					continue;
				}

				client.init();
			}

			else if (tokens[0].equals("clear")) {
				if (tokens.length != 1) {
					System.out.println(wrongArgsMessage);
					continue;
				}

				client.clear();
			}

			else if (tokens[0].equals("help")) {
				if (tokens.length != 1) {
					System.out.println(wrongArgsMessage);
					continue;
				}

				System.out.println("Available commands:\n"
						+ "spot TYPE ID      - prints the last observation of an object of a TYPE(person or car) with the respective ID. Where ID can have * to show all matchs\n"
						+ "trail TYPE ID     - prints all the observations of an object of a TYPE(person or car) with the respective ID\n"
						+ "ping              - checks if the server is running\n"
						+ "clear             - clears all the state of the server\n"
						+ "init              - initializes the server");
			}

			else
				System.out.println("Type help to see the documentation");
		}
	}

	public static void spot(String type, String id, SiloClientFrontend client) {
		TrackRequest.Builder trackRequest = TrackRequest.newBuilder();

		Target target = parseTarget(type);
		if (target == null) {
			System.out.println(wrongTypeMessage);
			return;
		}

		trackRequest.setTarget(target);
		trackRequest.setId(id);

		try {
			TrackResponse response = client.track(trackRequest.build());

			printObservation(response.getObservation(), client);

		} catch (StatusRuntimeException e) {
			System.out.println(exceptionMessage + e.getStatus().getDescription());
		}
	}

	public static void spotMatch(String type, String id, SiloClientFrontend client) {
		TrackMatchRequest.Builder trackRequest = TrackMatchRequest.newBuilder();

		Target target = parseTarget(type);
		if (target == null) {
			System.out.println(wrongTypeMessage);
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

			List<Observation> observations = type.equals("person")
					? response.getObservationsList().stream().sorted(Comparator.comparingInt(p->Integer.parseInt(p.getId())))
							.collect(Collectors.toList())
					: response.getObservationsList().stream().sorted(Comparator.comparing(Observation::getId))
							.collect(Collectors.toList());

			for (Observation observation : observations)
				printObservation(observation, client);

		} catch (StatusRuntimeException e) {
			System.out.println(exceptionMessage + e.getStatus().getDescription());
		}
	}

	public static void trail(String type, String id, SiloClientFrontend client) {
		TraceRequest.Builder trackRequest = TraceRequest.newBuilder();

		Target target = parseTarget(type);
		if (target == null) {
			System.out.println(wrongTypeMessage);
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
			System.out.println(exceptionMessage + e.getStatus().getDescription());
		}
	}

	public static void printObservation(Observation observation, SiloClientFrontend client) {
		String cameraName = observation.getCameraName();
		if (cameraName.equals("")) {
			System.out.println("");
			return;
		}

		System.out
				.printf("%s,%s,%s,%s,%s,%s%n", observation.getTarget().toString().toLowerCase(), observation.getId(),
						Instant.ofEpochSecond(observation.getTs().getSeconds()).atZone(ZoneId.systemDefault())
								.toLocalDateTime().toString(),
						observation.getCameraName(), observation.getCamInfo().getLatitude(), observation.getCamInfo().getLongitude());
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
