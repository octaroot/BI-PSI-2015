import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
abstract class LexicalAnalyzer {

    protected enum AnalyzerStates {};

    abstract public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted;
}

class SyntaxIncorrect extends Exception {
    public SyntaxIncorrect(String message){
        super(message);
    }
}

class InputNotAccepted extends Exception {
    public InputNotAccepted(String message){
        super(message);
    }
}