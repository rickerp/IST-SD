package pt.tecnico.sauron.eye;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloClientFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EyeApp {

	public static void main(String[] args) throws InterruptedException {
		System.out.println(EyeApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port cameraName latitude longitude%n", EyeApp.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String cameraName = args[2];
		final float latitude = Float.parseFloat(args[3]);
		final float longitude = Float.parseFloat(args[4]);

		final int instance;

		if (args.length == 6) {
			instance = Integer.parseInt(args[5]);
			if (instance < 1 || instance > 9) {
				System.out.println("Instance must be between 1 and 9");
				return;
			}
		}
		else
			instance = -1;

		SiloClientFrontend client = new SiloClientFrontend(host, port, instance);

		try {
			client.camJoin(
					CamJoinRequest.newBuilder()
							.setCameraName(cameraName)
							.setLatitude(latitude)
							.setLongitude(longitude)
							.build()
			);
		} catch (StatusRuntimeException e) {
			System.out.println("Camera join failed: " + e.getStatus().getDescription());
			return;
		}

		repl(cameraName, client);
	}

	private static void repl(String cameraName, SiloClientFrontend client) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		List<Observation> observations = new ArrayList<Observation>();
		boolean hasNextLine = true;

		while (hasNextLine) {

			hasNextLine = scanner.hasNextLine();
			String line;

			if (!hasNextLine || (line = scanner.nextLine().strip()).isEmpty()) {
				if (!observations.isEmpty()) {
					ReportRequest reportRequest = ReportRequest.newBuilder()
							.addAllObservations(observations)
							.setCameraName(cameraName)
							.build();
					try {
						client.report(reportRequest);
					} catch (StatusRuntimeException e) {
						System.out.println("Report failed: " + e.getStatus().getDescription());
						return;
					}
					observations.clear();
				}
				continue;
			}

			if (line.startsWith("#")) {
				continue;
			}

			String[] tokens = line.split(",");
			if (tokens[0].equals("zzz")) {
				Thread.sleep(Integer.parseInt(tokens[1]));
				continue;
			}

			if (tokens.length != 2) {
				System.out.println("Error: input should be `type,id`");
				return;
			}

			Observation.Builder observationBuilder = Observation
					.newBuilder()
					.setId(tokens[1]);

			if (tokens[0].equals("person")) {
				observationBuilder.setTarget(Target.PERSON);
			} else if (tokens[0].equals("car")) {
				observationBuilder.setTarget(Target.CAR);
			} else {
				System.out.println("Error: type should be car|person");
				return;
			}

			observations.add(observationBuilder.build());
		}
	}

}
