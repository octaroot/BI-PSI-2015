import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */

/**
 * Derived lexical analyzer. Handles PASSWORDmessages
 */
final class PasswordLexicalAnalyzer extends LexicalAnalyzer
{

	@Override
	public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted
	{

		AnalyzerStates state = AnalyzerStates.EMPTY;
		LinkedList<String> tokens = new LinkedList<String>();
		StringBuilder stringBuffer = new StringBuilder();

		for (int i = 0; i < input.length(); i++)
		{

			char c = input.charAt(i);

			stringBuffer.append(c);

			switch (state)
			{
				case EMPTY:
				case NUMBER:
					if (c < '0' || c > '9') throw new SyntaxIncorrect("Password has to be numerical");
					state = AnalyzerStates.NUMBER;
					break;
			}
		}

		if (state == AnalyzerStates.EMPTY)
			throw new InputNotAccepted("Password cannot be empty - not accepted by PasswordLexicalAnalyzer");

		tokens.add(stringBuffer.toString());

		return tokens;
	}

	// ^\d+\r\n$
	// realne ^\d+$ (\r\n osekne getLine)

	protected enum AnalyzerStates
	{
		EMPTY, NUMBER
	}
}
