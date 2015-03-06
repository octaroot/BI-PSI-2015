import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class OutputCommand {

    private Socket clientSocket;
    private PrintWriter stream;

    public OutputCommand(PrintWriter stream, Socket clientSocket)
    {
        this.stream = stream;
        this.clientSocket = clientSocket;
    }

    public enum MessageTypes {LOGIN, PASSWORD, OK, BAD_CHECKSUM, LOGIN_FAILED, SYNTAX_ERROR, TIMEOUT}

    public void sendMessage(String message, PrintWriter stream)
    {
        System.out.println("[DEBUG][>][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Sending \"" + message + "\\r\\n\"");

        message += "\r\n";
        stream.print(message);
        stream.flush();
    }

    public void sendMessage(MessageTypes message, PrintWriter stream)
    {
        sendMessage(generateMessage(message), stream);
    }

    public static String generateMessage(MessageTypes messageType) {

        return String.valueOf(getMessageCode(messageType)) + " " + messageType.toString().replace('_', ' ');
    }

    private static int getMessageCode(MessageTypes messageType) {
        switch (messageType) {
            case LOGIN:
                return 200;
            case PASSWORD:
                return 201;
            case OK:
                return 202;
            case BAD_CHECKSUM:
                return 300;
            case LOGIN_FAILED:
                return 500;
            case SYNTAX_ERROR:
                return 501;
            case TIMEOUT:
                return 502;
            default:
                throw new IllegalArgumentException("Message type not recognized");
        }
    }
}