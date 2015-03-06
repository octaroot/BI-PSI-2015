import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
class UsernameLexicalAnalyzer extends LexicalAnalyzer {

    protected enum AnalyzerStates {NAME_PREFIX_CHAR1, NAME_PREFIX_CHAR2, NAME_PREFIX_CHAR3, NAME_PREFIX_CHAR4, NAME_PREFIX_CHAR5, NAME_CONTENTS}

    // ^Robot[^\r\n]*\r\n$
    // realne ^Robot.*$ (\r\n osekne getLine)

    @Override
    public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted {

        AnalyzerStates state = AnalyzerStates.NAME_PREFIX_CHAR1;
        LinkedList<String> tokens = new LinkedList<String>();
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            stringBuffer.append(c);

            switch (state) {
                case NAME_PREFIX_CHAR1:
                    if (c != 'R') throw new SyntaxIncorrect("Username does not contain 'R' at required place");
                    state = AnalyzerStates.NAME_PREFIX_CHAR2;
                    break;
                case NAME_PREFIX_CHAR2:
                    if (c != 'o') throw new SyntaxIncorrect("Username does not contain 'o' at required place");
                    state = AnalyzerStates.NAME_PREFIX_CHAR3;
                    break;
                case NAME_PREFIX_CHAR3:
                    if (c != 'b') throw new SyntaxIncorrect("Username does not contain 'b' at required place");
                    state = AnalyzerStates.NAME_PREFIX_CHAR4;
                    break;
                case NAME_PREFIX_CHAR4:
                    if (c != 'o') throw new SyntaxIncorrect("Username does not contain 'o' at required place");
                    state = AnalyzerStates.NAME_PREFIX_CHAR5;
                    break;
                case NAME_PREFIX_CHAR5:
                    if (c != 't') throw new SyntaxIncorrect("Username does not contain 't' at required place");
                    state = AnalyzerStates.NAME_CONTENTS;
                    break;
            }
        }

        if (state != AnalyzerStates.NAME_CONTENTS)
            throw new InputNotAccepted("Input \"" + input + "\" was not accepted by UsernameLexicalAnalyzer");

        tokens.add(stringBuffer.toString());

        return tokens;
    }
}
