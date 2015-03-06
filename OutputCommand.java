/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class OutputCommand {
    public enum MessageTypes {LOGIN, PASSWORD, OK, BAD_CHECKSUM, LOGIN_FAILED, SYNTAX_ERROR, TIMEOUT}

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