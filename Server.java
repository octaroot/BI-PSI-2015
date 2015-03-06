import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class Server {

    private int port;
    private ServerSocket socket;
    private boolean running;

    public Server(int port) {
        this.port = port;
        this.socket = null;
        this.running = false;
    }

    public void startServer() throws IOException {

        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(1);
        }

        this.running = true;

        while (running) {
            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            System.out.println("Client accepted from: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
            new Thread(new ClientHandler(clientSocket)).start();
        }

        socket.close();
    }
}
