import java.io.*;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.input = null;
        this.output = null;
    }

    @Override
    public void run() {
        try {
            try {
                output = new PrintWriter(clientSocket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Couldn't get I/O.");
                System.exit(1);
            }

            long time = System.currentTimeMillis();
            String inputLine, outputLine;
            while ((inputLine = input.readLine()) != null) {
                System.out.println("request (time " + time + "): " + inputLine);
                output.println("ECHO (time " + time + "): " + inputLine);

                if (inputLine.equals("END")) break;
            }

            System.out.printf("ENDING");

            output.close();
            input.close();

        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}
