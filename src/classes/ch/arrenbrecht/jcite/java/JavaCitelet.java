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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.arrenbrecht.jcite.Constants;
import ch.arrenbrecht.jcite.FileNotFoundError;
import ch.arrenbrecht.jcite.JCite;
import ch.arrenbrecht.jcite.JCiteError;
import ch.arrenbrecht.jcite.JCitelet;
import ch.arrenbrecht.jcite.UnclosedMarkupError;
import ch.arrenbrecht.jcite.Util;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.Java2HtmlConversionOptions;

public class JavaCitelet extends JCitelet
{
	static final Pattern stripPattern = newOptionPattern( "strip" );
	static final Pattern showPattern = newOptionPattern( "show" );
	static final Pattern omitPattern = newOptionPattern( "omit" );
	static final Pattern highlightPattern = newOptionPattern( "highlight" );

	private static final OmissionsIterator omissionsIterator = new OmissionsIterator();
	private static final StrippedMarkersIterator usedMarkersIterator = new StrippedMarkersIterator();
	private static final HighlightMarkersIterator highlightMarkersIterator = new HighlightMarkersIterator();

	static final Java2HtmlConversionOptions java2htmlOptions = Java2HtmlConversionOptions.getDefault();

	static final String BEGIN_HTML_HIGHLIGHT = "<span class=\"j-hl\">";
	static final String END_HTML_HIGHLIGHT = "</span>";


	private static Pattern newOptionPattern( String _optionName )
	{
		return Pattern.compile( ";\\s*" + _optionName + "\\s*([^;]*)" );
	}


	private boolean usePRE = true;


	public JavaCitelet(JCite _jcite)
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
	public String insertionFor( String _markup ) throws JCiteError, IOException
	{
		if (isVerbose()) {
			System.out.print( "  JCite citing " );
			System.out.println( _markup );
		}

		final Collection<String> strips = new ArrayList<String>();
		final Collection<String> shows = new ArrayList<String>();
		final Collection<String> omissions = new ArrayList<String>();
		final Collection<String> highlights = new ArrayList<String>();

		int endClassName = _markup.indexOf( ':' );
		int endFragmentName;
		int posOfSemicolon = _markup.indexOf( ';' );
		if (posOfSemicolon >= 0) {
			endFragmentName = posOfSemicolon;
			final String options = _markup.substring( posOfSemicolon );
			extractOptions( options, stripPattern, strips );
			extractOptions( options, showPattern, shows );
			extractOptions( options, omitPattern, omissions );
			extractOptions( options, highlightPattern, highlights );
		}
		else {
			endFragmentName = _markup.length();
		}

		// If not otherwise specified, make /**/ a highlight marker
		if (!strips.contains( "" ) && !omissions.contains( "" ) && !highlights.contains( "" ) && !shows.contains( "" )) {
			highlights.add( "" );
		}

		final String className = _markup.substring( 0, endClassName );
		final String fragmentName = _markup.substring( endClassName + 1, endFragmentName );
		final String classSource = getSourceForClass( className );
		String fragment = getFragmentFrom( classSource, fragmentName );

		fragment = stripIndentation( fragment );
		fragment = omissionsIterator.iterate( fragment, omissions );
		fragment = usedMarkersIterator.iterate( fragment, strips );
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


	private Map<String, String> sources = new HashMap<String, String>();

	private String getSourceForClass( String _className ) throws FileNotFoundError, IOException
	{
		final String cachedSource = this.sources.get( _className );
		if (null != cachedSource) return cachedSource;

		final String relativeClassSourcePath = _className.replace( '.', '/' ) + ".java";
		final File classSourceFile = findSourceFile( relativeClassSourcePath );
		final String newSource = normalizeLineEndings( Util.readStringFrom( classSourceFile ) );
		this.sources.put( _className, newSource );
		return newSource;
	}


	private String normalizeLineEndings( String _string )
	{
		return _string.replace( Constants.CRLF, "\n" ).replace( Constants.CR, '\n' );
	}


	private String getFragmentFrom( String _classSource, String _fragmentName ) throws FragmentNotFoundError,
			UnclosedMarkupError
	{
		final FragmentMarker[] markers = FragmentMarker.markersFor( _fragmentName );
		final StringBuffer fragments = new StringBuffer();
		final FragmentLocator locator = new FragmentLocator();
		int scanFrom = 0;
		boolean found = false;
		while (scanFrom < _classSource.length()) {
			if (FragmentMarker.findFragment( _classSource, scanFrom, markers, locator )) {
				String fragment = _classSource.substring( locator.beginFragment, locator.endFragment );
				fragments.append( fragment );
				scanFrom = locator.endSuffix;
				found = true;
			}
			else {
				break;
			}
		}
		if (!found) throw new FragmentNotFoundError( _classSource, _fragmentName );
		return fragments.toString();
	}


	private String stripIndentation( String _fragment )
	{
		int iIndents = 0;
		for (char ch : _fragment.toCharArray()) {
			if (ch == ' ' || ch == '\t') iIndents++;
			else break;
		}
		if (iIndents > 0) {
			final String indents = _fragment.substring( 0, iIndents );
			return _fragment.substring( iIndents ).replace( "\n" + indents, "\n" );
		}
		else {
			return _fragment;
		}
	}


	private String trimEmptyLines( String _fragment )
	{
		int iStart = 0;
		while (iStart < _fragment.length() && _fragment.charAt( iStart ) == '\n')
			iStart++;
		int iEnd = _fragment.length();
		while (iEnd > 0 && _fragment.charAt( iEnd - 1 ) == '\n')
			iEnd--;
		return _fragment.substring( iStart, iEnd );
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
