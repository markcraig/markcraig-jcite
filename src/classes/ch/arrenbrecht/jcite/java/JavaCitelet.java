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
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.arrenbrecht.jcite.Constants;
import ch.arrenbrecht.jcite.FileNotFoundError;
import ch.arrenbrecht.jcite.FragmentMarker;
import ch.arrenbrecht.jcite.JCite;
import ch.arrenbrecht.jcite.JCiteError;
import ch.arrenbrecht.jcite.TextBasedCitelet;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.Java2HtmlConversionOptions;

public class JavaCitelet extends TextBasedCitelet
{
	static final Pattern stripPattern = newOptionPattern( "strip" );
	static final Pattern showPattern = newOptionPattern( "show" );
	static final Pattern omitPattern = newOptionPattern( "omit" );
	static final Pattern highlightPattern = newOptionPattern( "highlight" );

	private final OmissionsIterator omissionsIterator = new OmissionsIterator( this );
	private final StrippedMarkersIterator stripMarkersIterator = new StrippedMarkersIterator( this );
	private final HighlightMarkersIterator highlightMarkersIterator = new HighlightMarkersIterator( this );

	static final Java2HtmlConversionOptions java2htmlOptions = Java2HtmlConversionOptions.getDefault();

	static final String BEGIN_HTML_HIGHLIGHT = "<span class=\"j-hl\">";
	static final String END_HTML_HIGHLIGHT = "</span>";


	private static Pattern newOptionPattern( String _optionName )
	{
		return Pattern.compile( ";\\s*" + _optionName + "\\s*([^;]*)" );
	}


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
	protected String markupTag()
	{
		return "jc";
	}


	@Override
	protected FragmentMarker[] markersFor( String _fragmentName )
	{
		if (null == _fragmentName || 0 == _fragmentName.length()) {
			return new FragmentMarker[] { new InlineMarker( _fragmentName ),
					new CompactInlineMarker( _fragmentName ) };
		}
		return new FragmentMarker[] { new BlockMarker( _fragmentName ), new InlineMarker( _fragmentName ),
				new CompactInlineMarker( _fragmentName ) };
	}


	@Override
	public String formattingFor( Insertion _insertion ) throws JCiteError
	{
		final Collection<String> strips = new ArrayList<String>();
		final Collection<String> shows = new ArrayList<String>();
		final Collection<String> omissions = new ArrayList<String>();
		final Collection<String> highlights = new ArrayList<String>();

		if (_insertion instanceof AnnotatedCitation) {
			final AnnotatedCitation ann = (AnnotatedCitation) _insertion;
			final String options = ann.annotation();
			extractOptions( options, stripPattern, strips );
			extractOptions( options, showPattern, shows );
			extractOptions( options, omitPattern, omissions );
			extractOptions( options, highlightPattern, highlights );
		}

		// If not otherwise specified, make /**/ a highlight marker
		if (!strips.contains( "" ) && !omissions.contains( "" ) && !highlights.contains( "" ) && !shows.contains( "" )) {
			highlights.add( "" );
		}

		String fragment = _insertion.text();
		fragment = stripIndentation( fragment );
		fragment = omissionsIterator.iterate( fragment, omissions );
		fragment = stripMarkersIterator.iterate( fragment, strips );
		fragment = highlightMarkersIterator.iterate( fragment, highlights );
		fragment = trimEmptyLines( fragment );
		fragment = formatAsHtml( fragment );
		fragment = replaceHighlightsWithHtml( fragment );

		return fragment;
	}


	private void extractOptions( String _source, Pattern _pattern, Collection<String> _matches )
	{
		Matcher m = _pattern.matcher( _source );
		while (m.find()) {
			_matches.add( m.group( 1 ) );
		}
	}


	@Override
	protected String getSourceForFile( String _className ) throws FileNotFoundError, IOException
	{
		return super.getSourceForFile( _className.replace( '.', '/' ) + ".java" );
	}


	private String formatAsHtml( String _fragment )
	{
		if (_fragment == null) {
			return null;
		}

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


	private String replaceHighlightsWithHtml( String _fragment )
	{
		String result = _fragment;
		result = result.replace( Constants.BEGIN_HIGHLIGHT, BEGIN_HTML_HIGHLIGHT );
		result = result.replace( Constants.END_HIGHLIGHT, END_HTML_HIGHLIGHT );
		return result;
	}


}
