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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public abstract class AbstractJCiteTest extends TestCase
{

	protected void assertEquivalentHtmlFiles( File _expected, File _actual ) throws IOException
	{
		String expected = sanitize( Util.readStringFrom( _expected ) );
		String actual = sanitize( Util.readStringFrom( _actual ) );
		assertEquals( _actual.getPath() + " is not equivalent to " + _expected.getPath(), expected, actual );
	}

	private String sanitize( String _value )
	{
		return cut( _value, "<head>", "</head>" ) // Hpricot seems to sometimes reorder things in <head>
			.replaceAll( "\r\n", "\n" )
			.replaceAll( "\n\n", "\n" )
			.replaceAll( "\n\n", "\n" );
	}

	private String cut(String _value, String _start, String _end) {
		int is = _value.indexOf(_start);
		if (is < 0) return _value;
		int ie = _value.indexOf(_end);
		if (ie < 0) return _value;
		return _value.substring( 0, is ) + _value.substring( ie + _end.length() );
	}

	protected String[] sourcePaths() {
		return new String[] { "src/test/data" };
	}

	protected void runBase(String _name) throws Exception
	{
		File htmlSource = new File( "temp/doc/" + _name + ".htm" );
		File htmlExpected = new File( "src/test/data/" + _name + "_expected.htm" );
		File htmlTarget = new File( "temp/test/data/" + _name + "_out.htm" );
		htmlTarget.getParentFile().mkdirs();
		final JCite jcite = new JCite( sourcePaths(), true, false );
		jcite.setProjectPath( new File( "" ).getPath() );
		jcite.process( htmlSource, htmlTarget );

		assertEquivalentHtmlFiles( htmlExpected, htmlTarget );
	}

	protected void runVariant(String _origName, String _variantSuffix, String _origTag, String _variantTag) throws Exception
	{
		File origSource = new File( "temp/doc/"+ _origName + ".htm" );
		File htmlSource = new File( "temp/doc/"+ _origName + _variantSuffix + ".htm" );

		String html = Util.readStringFrom( origSource );
		html = html //
				.replace( "<pre><code>[" + _origTag + ":", "<pre class=\"example\">[" + _variantTag + ":" )
				.replace( "<pre>[" + _origTag + ":", "<pre class=\"example\">[" + _variantTag + ":" )
				.replace( "</code></pre>", "</pre>");
		Util.writeStringTo( html, htmlSource );
		try {
			runBase(_origName + _variantSuffix);
		}
		finally {
			htmlSource.delete();
			htmlSource.deleteOnExit();
		}
	}

}
