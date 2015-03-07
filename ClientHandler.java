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
class ClientHandler implements Runnable {

    private Socket clientSocket;
    private InputStream input;
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

    public void kill()
    {
        System.err.println("[DEBUG][!][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] We took too long, ClientHandler killer called. Sending TIMEOUT now");
        oc.sendMessage(OutputCommand.MessageTypes.TIMEOUT);
        closeConnection();
    }

    @Override
    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = clientSocket.getInputStream();

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
                if (inputLine == null) {
                    closeConnection();
                    return;
                }
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

                            char c = ic.readChar();

                            sb.append(c);
                            payloadTypeAnalyzer.parseCharacter(c);
                        }

                        PayloadTypeLexicalInstancedAnalyzer.PayloadModes payloadMode = payloadTypeAnalyzer.getPayloadType();

                        System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Recognized message type - " + payloadMode);

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
                        System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Bad message format. Received: \"" + sb.toString() + "\"");
                        oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
                        closeConnection();
                        return;
                    } catch (Exception e) {
                        //stream already ended
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

                    System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] FOTO_MESSAGE start");

                    FotoMessageLengthLexicalInstancedAnalyzer fotoMessageLengthAnalyzer = new FotoMessageLengthLexicalInstancedAnalyzer();
                    StringBuilder messageLengthBuilder = new StringBuilder();

                    while (!fotoMessageLengthAnalyzer.isStateFinal()) {
                        try {
                            char c = ic.readChar();
                            fotoMessageLengthAnalyzer.parseCharacter(c);
                            if (fotoMessageLengthAnalyzer.isStateFinal()) break;
                            messageLengthBuilder.append(c);
                        } catch (SyntaxIncorrect syntaxIncorrect) {
                            System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Bad FOTO message length format. Received: \"" + messageLengthBuilder.toString() + "\"");
                            oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
                            closeConnection();
                            return;
                        } catch (Exception e) {
                            //stream already ended
                            closeConnection();
                            return;
                        }
                    }

                    String dataLengthString = messageLengthBuilder.toString();

                    System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] FOTO_MESSAGE read size - \"" + dataLengthString + "\"");

                    int dataLength;

                    if (!Pattern.matches("^\\d+$", dataLengthString)) {
                        System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Bad image length format. Received: \"" + dataLengthString + "\"");
                        oc.sendMessage(OutputCommand.MessageTypes.SYNTAX_ERROR);
                        closeConnection();
                        return;
                    }

                    dataLength = Integer.parseInt(dataLengthString);

                    System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] FOTO_MESSAGE casted length to int \"" + dataLength + "\". reading that amount of bytes now");

                    byte imageData[] = ic.readBytes(dataLength);

                    if (imageData == null) {
                        closeConnection();
                        return;
                    }

                    System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Received " + dataLengthString + " bytes of image data");
                    byte correctChecksumBytes[] = ic.readBytes(4);

                    if (correctChecksumBytes == null) {
                        closeConnection();
                        return;
                    }

                    System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Received 4 bytes of image checksum");
                    ByteBuffer bb = ByteBuffer.wrap(correctChecksumBytes);
                    bb.order(ByteOrder.BIG_ENDIAN);
                    int correctChecksum = bb.getInt();

                    final int calculatedChecksum = ImageDataValidator.calculateChecksum(imageData);

                    if (calculatedChecksum != correctChecksum) {
                        System.out.println("[INFO][ ][" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Checksum mismatch. Received: " + correctChecksum + ", calculated: " + calculatedChecksum);
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
}
