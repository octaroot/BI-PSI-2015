import java.io.IOException;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
public class Robot {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Server: java robot.Robot <port>");
            System.exit(1);
        }

        int port = 0;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Please specify the port as a correct number");
            System.exit(2);
        } finally {
            if (port < 3000 || port > 3999) {
                System.err.println("Please specify a port within the range 3000 through 3999 (including)");
                System.exit(3);
            }
        }

        System.out.println("Starting server...\n");
        Server server = new Server(port);
        server.startServer();

    }
}
