import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */

/**
 * Main server class. Handles the connecting clients and starts new threads
 */
final class Server
{

	private int          port;
	private ServerSocket socket;

	public Server(int port)
	{
		this.port = port;
		this.socket = null;
	}

	/**
	 * Starts the server. Server accepts clients in an infinite loop
	 *
	 * @throws IOException
	 */
	public void startServer() throws IOException
	{
		try
		{
			socket = new ServerSocket(port);
		}
		catch (IOException e)
		{
			System.err.println("Could not listen on port: " + port);
			System.exit(1);
		}

		while (true)
		{
			Socket clientSocket = null;
			try
			{
				clientSocket = socket.accept();
			}
			catch (IOException e)
			{
				System.err.println("Accept failed.");
				System.exit(1);
			}

			System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Client accepted");
			ClientHandler runnable = new ClientHandler(clientSocket);
			Thread clientThread = new Thread(runnable);
			clientThread.start();

			new Thread(new TheadKiller(clientThread, runnable)).start();

		}
	}
}
