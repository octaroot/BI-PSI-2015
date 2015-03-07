import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */

/**
 * Derived lexical analyzer. Handles INFO messages
 */
final class InfoMessageLexicalAnalyzer extends LexicalAnalyzer
{

	@Override
	public LinkedList<String> parseTokens(String input)
	{

		LinkedList<String> tokens = new LinkedList<String>();
		StringBuilder stringBuffer = new StringBuilder();

		for (int i = 0; i < input.length(); i++)
		{

			char c = input.charAt(i);

			stringBuffer.append(c);

		}

		tokens.add(stringBuffer.toString());

		return tokens;
	}

	// ^[^\r\n]*\r\n$
	// realne ^.*$ (\r\n osekne getLine)

	protected enum AnalyzerStates {}
}
