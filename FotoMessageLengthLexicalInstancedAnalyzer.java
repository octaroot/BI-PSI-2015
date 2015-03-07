import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */
class FotoMessageLengthLexicalInstancedAnalyzer extends LexicalAnalyzer {

    protected enum AnalyzerStates {EMPTY, NUMBER, DONE}

    private AnalyzerStates state;
    private LinkedList<String> tokens;

    FotoMessageLengthLexicalInstancedAnalyzer() {
        this.state = AnalyzerStates.EMPTY;
        this.tokens = new LinkedList<String>();
    }

    public void parseCharacter(char c) throws SyntaxIncorrect {
        handleInput(c);
    }

    public boolean isStateFinal()
    {
        return state == AnalyzerStates.DONE;
    }

    private void handleInput(char c) throws SyntaxIncorrect
    {
        switch (state) {

            case NUMBER:
                if (c == ' ') {
                    state = AnalyzerStates.DONE;
                    break;
                }
            case EMPTY:
                if (c < '0' || c > '9') throw new SyntaxIncorrect("Length has to be numerical");
                state = AnalyzerStates.NUMBER;
                break;
            case DONE:
                throw new SyntaxIncorrect("Cannot read anymore, word already accepted");
        }
    }

    @Override
    public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted {

        for (int i = 0; i < input.length(); i++) {
            handleInput(input.charAt(i));
        }

        if (state != AnalyzerStates.DONE)
            throw new InputNotAccepted("Length not specified - not accepted by PayloadTypeLexicalAnalyzer");

        tokens.add(state.toString());

        return tokens;
    }
}
