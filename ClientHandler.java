import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
    private OutputCommand oc;

    private enum ClientStates {USERNAME, PASSWORD, AUTHENTICATED}

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientRobotName = null;
        this.input = null;
        this.output = null;
        this.clientState = ClientStates.USERNAME;
        this.oc = null;
    }

    @Override
    public void run() {
        try {
            try {
                output = new PrintWriter(clientSocket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                oc = new OutputCommand(output, clientSocket);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O.");
                System.exit(1);
            }

            oc.sendMessage(OutputCommand.MessageTypes.LOGIN, output);

            long time = System.currentTimeMillis();
            String inputLine;

            while (!clientSocket.isClosed() && (inputLine = input.readLine()) != null) {
                System.out.println("[DEBUG] > Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " (time " + time + ") sent: " + inputLine);

                switch (clientState) {
                    case USERNAME:
                        clientRobotName = inputLine;
                        oc.sendMessage(OutputCommand.MessageTypes.PASSWORD, output);
                        clientState = ClientStates.PASSWORD;
                        break;
                    case PASSWORD:
                        UsernameLexicalAnalyzer usernameAnalyzer = new UsernameLexicalAnalyzer();
                        PasswordLexicalAnalyzer passwordAnalyzer = new PasswordLexicalAnalyzer();

                        try {
                            usernameAnalyzer.parseTokens(clientRobotName);
                            passwordAnalyzer.parseTokens(inputLine);

                            if (!Authenticator.credentialsValid(clientRobotName, inputLine))
                                throw new Exception("Credentials invalid");
                        } catch (Exception e) {
                            oc.sendMessage(OutputCommand.MessageTypes.LOGIN_FAILED, output);
                            clientSocket.close();
                            break;
                        }

                        oc.sendMessage(OutputCommand.MessageTypes.OK, output);
                        clientState = ClientStates.AUTHENTICATED;
                        break;
                    case AUTHENTICATED:
                        oc.sendMessage("999 ECHO " + inputLine, output);

                }
            }

            System.out.printf("ENDING");

            if (!clientSocket.isClosed())
                clientSocket.close();

            output.close();
            input.close();

        } catch (IOException e) {
            //ooooops
            e.printStackTrace();
        }
    }
}
