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
package ch.arrenbrecht.jcite.java;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import ch.arrenbrecht.jcite.JCite;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.Java2HtmlConversionOptions;

/** Cites Java source code and adds syntax highlighting. */
public class JavaCitelet extends AbstractJavaCitelet
{
	static final Java2HtmlConversionOptions java2htmlOptions = Java2HtmlConversionOptions.getDefault();


	private boolean usePRE = true;


	public JavaCitelet( JCite _jcite )
	{
		super( _jcite );
	}


	@Override
	protected boolean argHandled( String _arg )
	{
		if (_arg.equalsIgnoreCase( "-tt" )) {
			this.usePRE = false;
			return true;
		}
		return false;
	}


	@Override
	protected String referencePrefix()
	{
		return "jc";
	}


	@Override protected String javaToHtml( String _fragment, String _beginTag, String _endTag )
	{
		StringReader stringReader = new StringReader( _fragment );
		JavaSource source = null;
		try {
			source = new JavaSourceParser( java2htmlOptions ).parse( stringReader );
		}
		catch (IOException e) {
			return null;
		}

		JavaSource2XHtmlFragmentConverter converter = new JavaSource2XHtmlFragmentConverter( source );
		converter.setConversionOptions( java2htmlOptions );
		converter.setPRE( this.usePRE );
		StringWriter writer = new StringWriter();
		try {
			converter.convert( writer );
		}
		catch (IOException e) {
			return null;
		}
		return writer.toString();
	}


}
