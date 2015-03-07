import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class InfoMessageLexicalAnalyzer extends LexicalAnalyzer {

    protected enum AnalyzerStates { }

    // ^[^\r\n]*\r\n$
    // realne ^.*$ (\r\n osekne getLine)

    @Override
    public LinkedList<String> parseTokens(String input) {

        LinkedList<String> tokens = new LinkedList<String>();
        StringBuilder stringBuffer = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            stringBuffer.append(c);

        }

        tokens.add(stringBuffer.toString());

        return tokens;
    }
}
