/*
 * Copyright (c) 2006 Peter Arrenbrecht
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - The names of the contributors may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contact information:
 * Peter Arrenbrecht
 * http://www.arrenbrecht.ch/jcite
 */
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
