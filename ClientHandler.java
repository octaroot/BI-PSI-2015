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
    private InputCommand ic;
    private Boolean lineMode;

    private enum ClientStates {USERNAME, PASSWORD, AUTHENTICATED, INFO_MESSAGE, FOTO_MESSAGE}

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientRobotName = null;
        this.input = null;
        this.output = null;
        this.clientState = ClientStates.USERNAME;
        this.oc = null;
        this.lineMode = true;
    }

    private void closeConnection() {
        try {
            if (!this.clientSocket.isClosed()) {
                System.out.println("[DEBUG][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] CLOSING CONNECTION");
                this.clientSocket.close();
            }
            this.input.close();
            this.output.close();
        } catch (Exception ignored) {

        }
    }

    @Override
    public void run() {
        try {
            try {
                output = new PrintWriter(clientSocket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                oc = new OutputCommand(output, clientSocket);
                ic = new InputCommand(input, clientSocket);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O.");
                System.exit(1);
            }

            oc.sendMessage(OutputCommand.MessageTypes.LOGIN);

            String inputLine = "";

            while (!clientSocket.isClosed()) {

                //dokud nemuzeme posilat FOTO, tak klido jedem texove
                if (lineMode) {
                    inputLine = ic.readLine();
                }

                switch (clientState) {
                    case USERNAME:
                        clientRobotName = inputLine;
                        oc.sendMessage(OutputCommand.MessageTypes.PASSWORD);
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

                        try {
                            for (int i = 0; i < 5; i++) {
                                char c = (char) input.read();

                                sb.append(c);
                                payloadTypeAnalyzer.parseCharacter(c);
                            }

                            PayloadTypeLexicalInstancedAnalyzer.PayloadModes payloadMode = payloadTypeAnalyzer.getPayloadType();

                            System.out.println("[INFO] Recognized message type - " + payloadMode);

                            switch (payloadMode) {
                                case INFO:
                                    lineMode = true;
                                    clientState = ClientStates.INFO_MESSAGE;
                                    break;
                                case FOTO:
                                    lineMode = false;
                                    clientState = ClientStates.FOTO_MESSAGE;
                            }

                        } catch (SyntaxIncorrect syntaxIncorrect) {
                            System.out.println("[INFO] Bad message format. Received: \"" + sb.toString() + "\"");
                            oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
                            closeConnection();
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

                        //TODO
                        closeConnection();

                        break;
                }
            }

            closeConnection();

        } catch (IOException e) {
            //ooooops
            e.printStackTrace();
        }
    }
}
