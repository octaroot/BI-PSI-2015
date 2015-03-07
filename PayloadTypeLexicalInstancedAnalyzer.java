import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class PayloadTypeLexicalInstancedAnalyzer extends LexicalAnalyzer {

    protected enum AnalyzerStates {TYPE_CHAR1, TYPE_CHAR2, TYPE_CHAR3, TYPE_CHAR4, TYPE_SPACE, TYPE_DONE}

    public enum PayloadModes {UNKNOWN, INFO, FOTO}
    //INFO_MESSAGE_CONTENTS, PHOTO_LENGTH, PHOTO_DATA, PHOTO_CHEKSUM

    private AnalyzerStates state;
    private LinkedList<String> tokens;
    private PayloadModes mode;

    PayloadTypeLexicalInstancedAnalyzer() {
        this.state = AnalyzerStates.TYPE_CHAR1;
        this.tokens = new LinkedList<String>();
        this.mode = PayloadModes.UNKNOWN;
    }

    public void parseCharacter(char c) throws SyntaxIncorrect {
        handleInput(c);
    }

    public PayloadModes getPayloadType()
    {
        if (!isStateFinal()) return null;
        return mode;
    }

    public boolean isStateFinal()
    {
        return state == AnalyzerStates.TYPE_DONE;
    }

    private void handleInput(char c) throws SyntaxIncorrect
    {
        switch (state) {
            case TYPE_CHAR1:
                if (c == 'I') {
                    mode = PayloadModes.INFO;
                } else if (c == 'F') {
                    mode = PayloadModes.FOTO;
                } else {
                    throw new SyntaxIncorrect("Unrecoginzed char 1 (\"" + c + "\") of type (neither I nor F)");
                }
                state = AnalyzerStates.TYPE_CHAR2;
                break;
            case TYPE_CHAR2:
                if ((mode == PayloadModes.INFO && c != 'N') || (mode == PayloadModes.FOTO && c != 'O'))
                    throw new SyntaxIncorrect("Unrecoginzed char 2 (\"" + c + "\") of type (neither N nor O)");
                state = AnalyzerStates.TYPE_CHAR3;
                break;
            case TYPE_CHAR3:
                if ((mode == PayloadModes.INFO && c != 'F') || (mode == PayloadModes.FOTO && c != 'T'))
                    throw new SyntaxIncorrect("Unrecoginzed char 3 (\"" + c + "\") of type (neither F nor T)");
                state = AnalyzerStates.TYPE_CHAR4;
                break;
            case TYPE_CHAR4:
                if ((mode == PayloadModes.INFO && c != 'O') || (mode == PayloadModes.FOTO && c != 'O'))
                    throw new SyntaxIncorrect("Unrecoginzed char 4 (\"" + c + "\") of type (neither O nor O)");
                state = AnalyzerStates.TYPE_SPACE;
                break;
            case TYPE_SPACE:
                if (c != ' ')
                    throw new SyntaxIncorrect("Unrecoginzed char 5 (\"" + c + "\") of type (should be a space)");
                state = AnalyzerStates.TYPE_DONE;

        }
    }

    @Override
    public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted {

        for (int i = 0; i < input.length(); i++) {
            handleInput(input.charAt(i));
        }

        if (state != AnalyzerStates.TYPE_DONE)
            throw new InputNotAccepted("Type incomplete - not accepted by PayloadTypeLexicalAnalyzer");

        tokens.add(state.toString());

        return tokens;
    }
}
