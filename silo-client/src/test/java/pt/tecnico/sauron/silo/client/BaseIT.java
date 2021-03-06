package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Properties;

public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	protected static SiloClientFrontend client;
	
	@BeforeAll
	public static void oneTimeSetup() throws IOException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);

			String host = testProps.getProperty("server.host");
			int port = Integer.parseInt(testProps.getProperty("server.port"));

			try {
				client = new SiloClientFrontend(host, port, 1);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		client.clear();
	}
	
	@AfterAll
	public static void cleanup() {
		client.clear();
		client.end();
	}

	@BeforeEach
    public void setUp() { }

    @AfterEach
    public void tearDown() { }

}
