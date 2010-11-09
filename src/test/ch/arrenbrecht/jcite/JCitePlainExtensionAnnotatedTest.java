package ch.arrenbrecht.jcite;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class JCitePlainExtensionAnnotatedTest extends TestCase {

	/**
	 * You can extend JCite by implementing your own {@link JCitelet}. Here's how:
	 */
	public void testCitelet() throws Exception {

		/**
		 * Citelets have to be registered with JCite. You normally do this through a service locator
		 * configuration file "META-INF/services/ch.arrenbrecht.jcite.JCiteletProvider" where you
		 * give the qualified name of your own implementation of {@link JCiteletProvider}. In this
		 * test setting, however, I register it directly.
		 *
		 * This also allows me to preconfigure it with a bunch of test citations. Normally, you'd
		 * read them from external sources, like files being cited.
		 */
		JCite citer = new JCite();
		citer.registerCitelet(new MyCitelet(citer, //
				"exA", "Example A\nAnd a little more of example A.", //
				"exB", "Example B"));

		/**
		 * The source text into which we're citing. Citations are addressed to our demo citelet via
		 * the "demo" tag (see {@link MyCitelet#markupTag()} below).
		 */
		String cited = citer.process("A test string\n" + //
				"with a citation:\n" + //
				"<stripped>[demo:exA]</stripped>\n" + //
				"and another one as in info block:\n" + //
				"<stripped>[demo:exB as INFO]</stripped>");

		/**
		 * JCite is meant to be used to cite into HTML. So it assumes that it should always strip
		 * away the HTML tag enclosing the citation instruction ("stripped" in our case). Typically,
		 * you use a "pre" tag here when authoring citation instructions.
		 */
		assertEquals("A test string\n" + //
				"with a citation:\n" + //
				"DEMO:	Example A\n" + //
				"	And a little more of example A.\n" + "\n" + "and another one as in info block:\n" + //
				"INFO:	Example B\n", //
				cited);
	}

	public static class MyCitelet extends JCitelet {

		/**
		 * This custom citelet has a fixed list of citations it offers.
		 */
		private final Map<String, String> sources = new HashMap<String, String>();

		public MyCitelet(JCite _jcite, String... _namesAndTexts) {
			super(_jcite);
			int i = 0;
			while (i < _namesAndTexts.length)
				sources.put(_namesAndTexts[i++], _namesAndTexts[i++]);
		}

		/**
		 * Since we can have multiple citelets, JCite needs to know which of the citelets to address
		 * when resolving a citation. It does this via this tag.
		 */
		@Override protected String referencePrefix() {
			return "demo";
		}

		/**
		 * We get called here when JCite finds an instruction to cite something from this citelet.
		 * It passes the part of the instruction within the [], and after the : to us, so for
		 * "[demo:foo bar]\n" it passes "foo bar". It is fully up to us to decide what to do with
		 * this. In this example, we see if there is a style instruction present, separate by
		 * " as ". If so, we pass it on to the formatter.
		 *
		 * Styled or not, we always return the cited text. When JCite is used with tripwires, this
		 * text is what gets recorded in the tripwire database. It should be kept as raw as
		 * possible, so that changes in how a citelet formats citations (see below) do not
		 * invalidate the tripwires.
		 */
		@Override public Citation cite(String _markup) throws JCiteError, IOException {
			final String[] parts = _markup.split(" as ");
			final String name = parts[0];
			final String cited = this.sources.get(name);
			if (null == cited)
				throw new JCiteError("Cannot find source for " + name);
			return (parts.length > 1) ? new AnnotatedCitation(cited, parts[1]) : new Citation(cited);
		}

		/**
		 * So here we format the citation for actual insertion into the HTML documentation.
		 */
		@Override protected String format(Insertion _insertion) throws JCiteError, IOException {
			final String[] lines = _insertion.text().split("\n");
			final StringBuilder fmt = new StringBuilder();
			/** This is how we access markup annotations again: */
			String prefix = (_insertion instanceof AnnotatedCitation) ? ((AnnotatedCitation) _insertion).annotation()
					+ ":\t" : "DEMO:\t";
			for (String line : lines) {
				fmt.append(prefix).append(line).append("\n");
				prefix = "\t";
			}
			return fmt.toString();
		}

	}

}
