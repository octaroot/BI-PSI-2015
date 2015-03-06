import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class PayloadTypeLexicalAnalyzer extends LexicalAnalyzer {

    protected enum AnalyzerStates {TYPE_CHAR1, TYPE_CHAR2, TYPE_CHAR3, TYPE_CHAR4, TYPE_DONE}

    protected enum PayloadModes {UNKNOWN, INFO, FOTO}
    //, INFO_MESSAGE_CONTENTS, PHOTO_LENGTH, PHOTO_DATA, PHOTO_CHEKSUM

    @Override
    public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted {

        AnalyzerStates state = AnalyzerStates.TYPE_CHAR1;
        LinkedList<String> tokens = new LinkedList<String>();

        PayloadModes mode = PayloadModes.UNKNOWN;

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            switch (state) {
                case TYPE_CHAR1:
                    if (c == 'I') {
                        mode = PayloadModes.INFO;
                    } else if (c == 'F') {
                        mode = PayloadModes.FOTO;
                    } else {
                        throw new SyntaxIncorrect("Unrecoginzed char 1 (\"" + c + "\")of type (neither I nor F)");
                    }
                    state = AnalyzerStates.TYPE_CHAR2;
                    break;
                case TYPE_CHAR2:
                    if ((mode == PayloadModes.INFO && c != 'N') || (mode == PayloadModes.FOTO && c != 'O'))
                        throw new SyntaxIncorrect("Unrecoginzed char 2 (\"" + c + "\")of type (neither N nor O)");
                    state = AnalyzerStates.TYPE_CHAR3;
                    break;
                case TYPE_CHAR3:
                    if ((mode == PayloadModes.INFO && c != 'F') || (mode == PayloadModes.FOTO && c != 'T'))
                        throw new SyntaxIncorrect("Unrecoginzed char 3 (\"" + c + "\")of type (neither F nor T)");
                    state = AnalyzerStates.TYPE_CHAR4;
                    break;
                case TYPE_CHAR4:
                    if ((mode == PayloadModes.INFO && c != 'O') || (mode == PayloadModes.FOTO && c != 'O'))
                        throw new SyntaxIncorrect("Unrecoginzed char 4 (\"" + c + "\")of type (neither O nor O)");
                    state = AnalyzerStates.TYPE_DONE;
                    break;
            }
        }

        if (state != AnalyzerStates.TYPE_DONE)
            throw new InputNotAccepted("Type incomplete - not accepted by PayloadTypeLexicalAnalyzer");

        tokens.add(state.toString());

        return tokens;
    }
}
