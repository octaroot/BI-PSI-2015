import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */

/**
 * Handles the output of commands
 */
final class OutputCommand
{

	private Socket      clientSocket;
	private PrintWriter stream;

	/**
	 * Constructor
	 *
	 * @param stream       The output stream to be written into
	 * @param clientSocket The client's socket (for logging purposes)
	 */
	public OutputCommand(PrintWriter stream, Socket clientSocket)
	{
		this.stream = stream;
		this.clientSocket = clientSocket;
	}

	/**
	 * Generates a message skeleton
	 *
	 * @param messageType The message type
	 * @return The message's skeleton
	 */
	public static String generateMessage(MessageTypes messageType)
	{
		return String.valueOf(getMessageCode(messageType)) + " " + messageType.toString().replace('_', ' ');
	}

	/**
	 * Gets a message code belonging to a message type specified
	 *
	 * @param messageType The message type
	 * @return The message type's code
	 */
	private static int getMessageCode(MessageTypes messageType)
	{
		switch (messageType)
		{
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

	/**
	 * Sends message to the output stream and flushes. Also logs this event
	 *
	 * @param message The message to be sent
	 */
	public void sendMessage(String message)
	{
		System.out.println("[DEBUG][<][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Sending " +
				"\"" + message + "\\r\\n\"");

		message += "\r\n";
		stream.print(message);
		stream.flush();
	}

	/**
	 * Send message overloaded
	 *
	 * @param message The message to be sent
	 */
	public void sendMessage(MessageTypes message)
	{
		sendMessage(generateMessage(message));
	}

	/**
	 * Enum of available message types
	 */
	public enum MessageTypes
	{
		LOGIN, PASSWORD, OK, BAD_CHECKSUM, LOGIN_FAILED, SYNTAX_ERROR, TIMEOUT
	}
}
