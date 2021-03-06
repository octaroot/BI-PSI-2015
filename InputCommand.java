import java.io.InputStream;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */

/**
 * Handles the input of commands
 */
final class InputCommand
{
	Socket      clientSocket;
	InputStream stream;

	/**
	 * Constructor
	 *
	 * @param stream       The input stream to be read from
	 * @param clientSocket The client's socket (for logging purposes)
	 */
	InputCommand(InputStream stream, Socket clientSocket)
	{
		this.clientSocket = clientSocket;
		this.stream = stream;
	}

	/**
	 * Reads a line (a string of chars ending with \r\n)
	 *
	 * @return The line read from the stream
	 */
	public String readLine()
	{
		StringBuilder inputBuilder = new StringBuilder();
		boolean carriageReturnDetected = false;
		boolean reachedEndOfLine = false;

		while (!reachedEndOfLine)
		{
			try
			{

				int data = stream.read();

				if (data == -1) return null;

				char c = (char) data;

				inputBuilder.append(c);

				switch (c)
				{
					case '\r':
						carriageReturnDetected = true;
						break;
					case '\n':
						if (carriageReturnDetected)
						{
							reachedEndOfLine = true;
							carriageReturnDetected = false;
						}
						break;
					default:
						carriageReturnDetected = false;
				}
			}
			catch (Exception e)
			{
				return null;
			}
		}

		final String inputLine = inputBuilder.substring(0, inputBuilder.length() - 2);
		printLogMessage(inputLine);

		return inputLine;
	}

	/**
	 * Reads a specified number of bytes from the stream
	 *
	 * @param count The number of bytes to read
	 * @return The bytes read from the stream
	 */
	public byte[] readBytes(int count)
	{
		byte data[] = new byte[count];

		try
		{
			for (int i = 0; i < count; i++)
			{
				int readData = stream.read();

				if (readData == -1) return null;

				data[i] = (byte) readData;
			}
		}
		catch (Exception e)
		{
			return null;
		}

		return data;
	}

	/**
	 * Reads a single character from the stream
	 *
	 * @return The character read from the stream
	 * @throws Exception
	 */
	public char readChar() throws Exception
	{
		int readData;

		try
		{
			readData = stream.read();

			if (readData == -1) throw new Exception();
		}
		catch (Exception ignored)
		{
			throw new Exception("Unable to read single char");
		}

		return (char) readData;
	}

	/**
	 * Prints a log message to the system's stream (for debugging purposes)
	 *
	 * @param received The data to be printed out
	 */
	private void printLogMessage(String received)
	{
		final String message = "[DEBUG][>][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] " +
				"Received " + received.length() + " bytes";

		if (received.length() > 20)
		{
			System.out.println(message + ". First 20: " + received.substring(0, 19));
		}
		else
		{
			System.out.println(message + ": " + received);
		}
	}

}
