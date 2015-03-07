import java.io.BufferedReader;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */
final class InputCommand {

    BufferedReader stream;
    Socket clientSocket;

    InputCommand(BufferedReader stream, Socket clientSocket) {
        this.stream = stream;
        this.clientSocket = clientSocket;
    }

    public String readLine() {
        StringBuilder inputBuilder = new StringBuilder();
        boolean carriageReturnDetected = false;
        boolean reachedEndOfLine = false;

        while (!reachedEndOfLine) {
            try {
                char c = (char) stream.read();
                inputBuilder.append(c);

                switch (c) {
                    case '\r':
                        carriageReturnDetected = true;
                        break;
                    case '\n':
                        if (carriageReturnDetected) {
                            reachedEndOfLine = true;
                            carriageReturnDetected = false;
                        }
                        break;
                    default:
                        carriageReturnDetected = false;
                }
            } catch (Exception e) {
                return null;
            }
        }

        final String inputLine = inputBuilder.substring(0, inputBuilder.length() - 2);
        final String message = "[DEBUG][>][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Received " + inputLine.length() + " bytes";

        if (inputLine.length() > 20) {
            System.out.println(message + ". First 20: " + inputLine.substring(0,19));
        } else {
            System.out.println(message + ": " +  inputLine);
        }

        return inputLine;
    }

}
