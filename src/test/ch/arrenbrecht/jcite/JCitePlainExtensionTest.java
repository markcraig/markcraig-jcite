package ch.arrenbrecht.jcite;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class JCitePlainExtensionTest extends TestCase {

	public void testCitelet() throws Exception {
		JCite citer = new JCite();
		citer.registerCitelet(new MyCitelet(citer));
		// -- test
		String cited = citer.process("A test string\n" + //
				"with a citation:\n" + //
				/**/"<stripped>[demo:exA]</stripped>\n"/**/+ //
				"and another one as in info block:\n" + //
				/**/"<stripped>[demo:exB as INFO]</stripped>"/**/);
		assertEquals("A test string\n" + //
				"with a citation:\n" + //
				/**/"DEMO:	Example A\n" + //
				"	And a little more of example A.\n" + //
				"\n"/**/+ //
				"and another one as in info block:\n" + //
				/**/"INFO:	Example B\n"/**/, //
				cited);
		// -- test
	}

	// -- MyProvider
	public static class MyProvider implements JCiteletProvider {
		public JCitelet getCitelet(JCite _jcite) {
			return new MyCitelet(_jcite);
		}
	}
	// -- MyProvider

	// -- MyCitelet
	public static class MyCitelet /**/extends JCitelet/**/{

		private final Map<String, String> fragments = new HashMap<String, String>();

		public MyCitelet(JCite _jcite) {
			super(_jcite);
			this.fragments.put("exA", "Example A\nAnd a little more of example A.");
			this.fragments.put("exB", "Example B");
		}
		// -- MyCitelet

		public MyCitelet(JCite _jcite, String... _namesAndTexts) {
			super(_jcite);
			int i = 0;
			while (i < _namesAndTexts.length)
				fragments.put(_namesAndTexts[i++], _namesAndTexts[i++]);
		}

		// -- referencePrefix
		@Override protected String /**/referencePrefix/**/() {
			return "demo";
		}
		// -- referencePrefix

		// -- cite
		@Override public Citation /**/cite/**/(String _reference) throws JCiteError, IOException {
			final String[] parts = _reference.split(" as ");
			final String key = parts[0];
			final String fragment = this.fragments.get(key);
			if (null == fragment)
				throw new JCiteError("Cannot find source for " + key);
			if (parts.length == 1)
				return /**/new Citation/**/(fragment);
			final String formattingOptions = parts[1];
			return /**/new AnnotatedCitation/**/(fragment, formattingOptions);
		}
		// -- cite

		// -- format
		@Override protected String /**/format/**/(Insertion _insertion) throws JCiteError, IOException {
			final String[] lines = _insertion.text().split("\n");
			final StringBuilder fmt = new StringBuilder();
			String prefix = "DEMO:\t";
			if (_insertion /**/instanceof AnnotatedCitation/**/)
				prefix = ((AnnotatedCitation) _insertion).annotation() + ":\t";
			for (String line : lines) {
				fmt.append(prefix).append(line).append("\n");
				prefix = "\t";
			}
			return fmt.toString();
		}
		// -- format

	}

}
