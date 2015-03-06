import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private ClientStates clientState;
    private String clientRobotName;

    private enum ClientStates {USERNAME, PASSWORD, AUTHENTICATED}

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientRobotName = null;
        this.input = null;
        this.output = null;
        this.clientState = ClientStates.USERNAME;
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

            OutputCommand.sendMessage(OutputCommand.MessageTypes.LOGIN, output);

            long time = System.currentTimeMillis();
            String inputLine;

            while (!clientSocket.isClosed() && (inputLine = input.readLine()) != null) {
                System.out.println("[DEBUG] > Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " (time " + time + ") sent: " + inputLine);

                switch (clientState) {
                    case USERNAME:
                        clientRobotName = inputLine;
                        OutputCommand.sendMessage(OutputCommand.MessageTypes.PASSWORD, output);
                        clientState = ClientStates.PASSWORD;
                        break;
                    case PASSWORD:
                        UsernameLexicalAnalyzer usernameAnalyzer = new UsernameLexicalAnalyzer();
                        PasswordLexicalAnalyzer passwordAnalyzer = new PasswordLexicalAnalyzer();

                        try
                        {
                            usernameAnalyzer.parseTokens(clientRobotName);
                            passwordAnalyzer.parseTokens(inputLine);

                            if (!Authenticator.credentialsValid(clientRobotName, inputLine))
                                throw new Exception("Credentials invalid");
                        }
                        catch (Exception e)
                        {
                            OutputCommand.sendMessage(OutputCommand.MessageTypes.LOGIN_FAILED, output);
                            clientSocket.close();
                            break;
                        }

                        OutputCommand.sendMessage(OutputCommand.MessageTypes.OK, output);
                        clientState = ClientStates.AUTHENTICATED;
                        break;
                    case AUTHENTICATED:
                        OutputCommand.sendMessage("999 ECHO " + inputLine, output);

                }
            }

            System.out.printf("ENDING");

            if (!clientSocket.isClosed())
                clientSocket.close();

            output.close();
            input.close();

        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}
