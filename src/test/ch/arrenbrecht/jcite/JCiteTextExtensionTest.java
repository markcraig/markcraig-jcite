package ch.arrenbrecht.jcite;

import java.io.IOException;

import junit.framework.TestCase;

public class JCiteTextExtensionTest extends TestCase {

	public void testCitelet() throws Exception {
		JCite citer = new JCite();
		citer.addSourceFolder("src/test/data");
		citer.registerCitelet(new MyCitelet(citer));
		// -- test
		String cited = citer.process("A test string\n"
				+ "with a citation:\n"
				+ /**/"<stripped>[demo:simplefile.txt:exA]</stripped>\n"/**/
				+ "and another one as in info block:\n"
				+ /**/"<stripped>[demo:simplefile.txt:exB;INFO]</stripped>")/**/;
		assertEquals("A test string\n"
				+ "with a citation:\n"
				+ /**/"DEMO:	Example A\n"
				+ "	And a little more of example A.\n"
				+ "\n"/**/
				+ "and another one as in info block:\n"
				+ /**/"INFO:	Example B\n"/**/
				, cited);
		// -- test
	}

	// -- MyCitelet
	public static class MyCitelet /**/extends TextBasedCitelet/**/ {

		public MyCitelet(JCite _jcite) {
			super(_jcite);
		}

		@Override protected String /**/referencePrefix/**/() {
			return "demo";
		}
		// -- MyCitelet

		// -- markersFor
		@Override protected FragmentMarker[] /**/markersFor/**/(String _fragmentName) {
			return new FragmentMarker[] { /**/new BlockMarker/**/(
					"// begin " + _fragmentName + "\n", //
					"// end " + _fragmentName + "\n") };
		}
		// -- markersFor

		// -- format
		@Override protected String /**/format/**/(Insertion _insertion) throws JCiteError, IOException {
			final String[] lines = /**/stripIndentation/**/( _insertion.text() ).split("\n");
			final StringBuilder fmt = new StringBuilder();
			String prefix = "DEMO:\t";
			if (_insertion /**/instanceof AnnotatedCitation/**/)
				prefix = ((AnnotatedCitation) _insertion).annotation() + ":\t";
			for (String line: lines) {
				fmt.append(prefix).append(line).append("\n");
				prefix = "\t";
			}
			return fmt.toString();
		}
		// -- format

	}

}
