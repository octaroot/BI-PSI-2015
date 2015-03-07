import java.io.InputStream;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */
final class InputCommand {

    Socket clientSocket;
    InputStream stream;

    InputCommand(InputStream stream, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.stream = stream;
    }

    public String readLine() {
        StringBuilder inputBuilder = new StringBuilder();
        boolean carriageReturnDetected = false;
        boolean reachedEndOfLine = false;

        while (!reachedEndOfLine) {
            try {

                int data = stream.read();

                if (data == -1)
                    return null;

                char c = (char) data;

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
        printLogMessage(inputLine);

        return inputLine;
    }

    public byte[] readBytes(int count) {
        byte data[] = new byte[count];

        try {
            for (int i = 0; i < count; i++)
            {
                int readData = stream.read();

                if (readData == -1)
                    return null;

                data[i] = (byte) readData;
            }
        } catch (Exception e) {
            return null;
        }

        return data;
    }

    public char readChar() throws Exception {
        int readData;

        try {
            readData = stream.read();

            if (readData == -1)
                throw new Exception("Uname to read single char");

        }
        catch (Exception ignored)
        {
            throw new Exception("Uname to read single char");
        }

        return (char) readData;
    }

    public String readTill(char stop) {
        StringBuilder inputBuilder = new StringBuilder();

        do {
            try {
                int data = stream.read();

                if (data == -1)
                    return null;

                char c = (char) data;

                if (c == stop)
                    break;

                inputBuilder.append(c);

            } catch (Exception e) {
                return null;
            }
        } while (true);

        final String inputLine = inputBuilder.toString();
        printLogMessage(inputLine);

        return inputLine;
    }

    private void printLogMessage(String received) {
        final String message = "[DEBUG][>][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Received " + received.length() + " bytes";

        if (received.length() > 20) {
            System.out.println(message + ". First 20: " + received.substring(0, 19));
        } else {
            System.out.println(message + ": " + received);
        }
    }

}
