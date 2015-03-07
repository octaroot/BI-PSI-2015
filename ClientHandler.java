import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
final class ClientHandler implements Runnable
{

	private Socket        clientSocket;
	private InputStream   input;
	private PrintWriter   output;
	private ClientStates  clientState;
	private String        clientRobotName;
	private OutputCommand oc;
	private InputCommand  ic;
	private Boolean       lineMode;

	/**
	 * Constructs the ClientHandler
	 *
	 * @param clientSocket Client's socket
	 */
	public ClientHandler(Socket clientSocket)
	{
		this.clientSocket = clientSocket;
		this.clientRobotName = null;
		this.input = null;
		this.output = null;
		this.clientState = ClientStates.USERNAME;
		this.oc = null;
		this.lineMode = true;
	}

	/**
	 * Closes the connection. Flushes output and waits a for a short time before closing it
	 */
	private void closeConnection()
	{
		try
		{
			output.flush();
			Thread.sleep(100);

			input.close();
			output.close();

			if (!clientSocket.isClosed()) clientSocket.close();
		}
		catch (Exception e)
		{
			System.err.println("Unable to gracefully close connection.");
		}
	}

	/**
	 * In case the connection hans for too long, this method can be called to gracefully timeout
	 */
	public void kill()
	{
		System.err.println("[DEBUG][!][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] We took " +
				"too long, ClientHandler killer called. Sending TIMEOUT now");
		oc.sendMessage(OutputCommand.MessageTypes.TIMEOUT);
		closeConnection();
	}

	/**
	 * Main method, sets up streams and handles all communication logic
	 */
	@Override
	public void run()
	{
		try
		{
			output = new PrintWriter(clientSocket.getOutputStream(), true);
			input = clientSocket.getInputStream();

			oc = new OutputCommand(output, clientSocket);
			ic = new InputCommand(input, clientSocket);
		}
		catch (IOException e)
		{
			System.err.println("Unable to open streams");
			return;
		}

		/**
		 * Send a LOGIN message to client
		 */
		oc.sendMessage(OutputCommand.MessageTypes.LOGIN);

		String inputLine = "";

		/**
		 * Main loop
		 */
		while (!clientSocket.isClosed())
		{

			/**
			 * We like to read till \r\n whenever we can
			 */
			if (lineMode)
			{
				inputLine = ic.readLine();
				if (inputLine == null)
				{
					closeConnection();
					return;
				}
			}

			/**
			 * Mail communication logic switch
			 */
			switch (clientState)
			{
				case USERNAME:
					clientRobotName = inputLine;
					oc.sendMessage(OutputCommand.MessageTypes.PASSWORD);
					clientState = ClientStates.PASSWORD;
					break;

				case PASSWORD:
					UsernameLexicalAnalyzer usernameAnalyzer = new UsernameLexicalAnalyzer();
					PasswordLexicalAnalyzer passwordAnalyzer = new PasswordLexicalAnalyzer();

					try
					{
						usernameAnalyzer.parseTokens(clientRobotName);
						passwordAnalyzer.parseTokens(inputLine);

						if (!Authenticator.areCredentialsValid(clientRobotName, inputLine))
							throw new Exception("Credentials invalid");
					}
					catch (Exception e)
					{
						oc.sendMessage(OutputCommand.MessageTypes.LOGIN_FAILED);
						closeConnection();
						return;
					}

					oc.sendMessage(OutputCommand.MessageTypes.OK);
					clientState = ClientStates.AUTHENTICATED;
					lineMode = false;
					break;

				case AUTHENTICATED:
					PayloadTypeLexicalInstancedAnalyzer payloadTypeAnalyzer = new PayloadTypeLexicalInstancedAnalyzer();
					StringBuilder sb = new StringBuilder();

					try
					{
						for (int i = 0; i < 5; i++)
						{
							char c = ic.readChar();

							sb.append(c);
							payloadTypeAnalyzer.parseCharacter(c);
						}

						PayloadTypeLexicalInstancedAnalyzer.PayloadModes payloadMode = payloadTypeAnalyzer.getPayloadType();

						System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Recognized message type - " + payloadMode);

						switch (payloadMode)
						{
							case INFO:
								lineMode = true;
								clientState = ClientStates.INFO_MESSAGE;
								break;
							case FOTO:
								lineMode = false;
								clientState = ClientStates.FOTO_MESSAGE;
						}

					}
					catch (SyntaxIncorrect syntaxIncorrect)
					{
						System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Bad message format. Received: \"" + sb.toString() + "\"");
						oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
						closeConnection();
						return;
					}
					catch (Exception e)
					{
						closeConnection();
						return;
					}
					break;

				case INFO_MESSAGE:
					InfoMessageLexicalAnalyzer infoMessageAnalyzer = new InfoMessageLexicalAnalyzer();
					infoMessageAnalyzer.parseTokens(inputLine);

					oc.sendMessage(OutputCommand.MessageTypes.OK);
					clientState = ClientStates.AUTHENTICATED;
					lineMode = false;
					break;

				case FOTO_MESSAGE:
					FotoMessageLengthLexicalInstancedAnalyzer fotoMessageLengthAnalyzer = new FotoMessageLengthLexicalInstancedAnalyzer();
					StringBuilder messageLengthBuilder = new StringBuilder();

					while (!fotoMessageLengthAnalyzer.isStateFinal())
					{
						try
						{
							char c = ic.readChar();
							fotoMessageLengthAnalyzer.parseCharacter(c);
							if (fotoMessageLengthAnalyzer.isStateFinal()) break;
							messageLengthBuilder.append(c);
						}
						catch (SyntaxIncorrect syntaxIncorrect)
						{
							System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Bad FOTO message length format. Received: \"" +
									messageLengthBuilder.toString() + "\"");
							oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
							closeConnection();
							return;
						}
						catch (Exception e)
						{
							closeConnection();
							return;
						}
					}

					String dataLengthString = messageLengthBuilder.toString();

					int dataLength;

					if (!Pattern.matches("^\\d+$", dataLengthString))
					{
						System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Bad image length format. Received: \"" + dataLengthString + "\"");
						oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
						closeConnection();
						return;
					}

					dataLength = Integer.parseInt(dataLengthString);

					byte imageData[] = ic.readBytes(dataLength);

					if (imageData == null)
					{
						closeConnection();
						return;
					}

					System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() +
							"] Received " + dataLengthString + " bytes of image data");
					byte correctChecksumBytes[] = ic.readBytes(4);

					if (correctChecksumBytes == null)
					{
						closeConnection();
						return;
					}

					System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() +
							"] Received 4 bytes of image checksum");
					ByteBuffer bb = ByteBuffer.wrap(correctChecksumBytes);
					bb.order(ByteOrder.BIG_ENDIAN);
					int correctChecksum = bb.getInt();

					final int calculatedChecksum = ImageDataValidator.calculateChecksum(imageData);

					if (calculatedChecksum != correctChecksum)
					{
						System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Checksum mismatch. Received: " + correctChecksum + ", " +
								"calculated: " + calculatedChecksum);
						oc.sendMessage(OutputCommand.MessageTypes.BAD_CHECKSUM);
						clientState = ClientStates.AUTHENTICATED;
						break;
					}

					//TODO: save image to disk

					oc.sendMessage(OutputCommand.MessageTypes.OK);
					clientState = ClientStates.AUTHENTICATED;

					break;
			}
		}

		closeConnection();
	}

	/**
	 * Client state during the communication
	 */
	private enum ClientStates
	{
		USERNAME, PASSWORD, AUTHENTICATED, INFO_MESSAGE, FOTO_MESSAGE
	}
}
