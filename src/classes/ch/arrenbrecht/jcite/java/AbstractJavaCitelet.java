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
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.arrenbrecht.jcite.BlockMarker;
import ch.arrenbrecht.jcite.Constants;
import ch.arrenbrecht.jcite.FileNotFoundError;
import ch.arrenbrecht.jcite.FragmentMarker;
import ch.arrenbrecht.jcite.InlineMarker;
import ch.arrenbrecht.jcite.JCite;
import ch.arrenbrecht.jcite.JCiteError;
import ch.arrenbrecht.jcite.TextBasedCitelet;

abstract class AbstractJavaCitelet extends TextBasedCitelet
{
	static final Pattern stripPattern = newOptionPattern( "strip" );
	static final Pattern showPattern = newOptionPattern( "show" );
	static final Pattern omitPattern = newOptionPattern( "omit" );
	static final Pattern highlightPattern = newOptionPattern( "highlight" );

	private final OmissionsIterator omissionsIterator = new OmissionsIterator( this );
	private final StrippedMarkersIterator stripMarkersIterator = new StrippedMarkersIterator( this );
	private final HighlightMarkersIterator highlightMarkersIterator = new HighlightMarkersIterator( this );

	static final String BEGIN_HTML_HIGHLIGHT = "<span class=\"j-hl\">";
	static final String END_HTML_HIGHLIGHT = "</span>";


	private static Pattern newOptionPattern( String _optionName )
	{
		return Pattern.compile( ";\\s*" + _optionName + "\\s*([^;]*)" );
	}


	public AbstractJavaCitelet( JCite _jcite )
	{
		super( _jcite );
	}


	@Override
	protected FragmentMarker[] markersFor( String _fragmentName )
	{
		final FragmentMarker inlineMarker = new InlineMarker( "/* " + _fragmentName + " */" );
		final FragmentMarker compactInlineMarker = new InlineMarker( "/*" + _fragmentName + "*/" );
		if (null == _fragmentName || 0 == _fragmentName.length()) {
			return new FragmentMarker[] { inlineMarker, compactInlineMarker };
		}
		final FragmentMarker blockMarker = new BlockMarker( "// " + _fragmentName + "\n" );
		return new FragmentMarker[] { blockMarker, inlineMarker, compactInlineMarker };
	}


	@Override
	public final String format( Insertion _insertion ) throws JCiteError
	{
		return format( _insertion, "<pre>", "</pre>" );
	}

	@Override
	protected String format(Insertion _insertion, String _beginTag, String _endTag) throws JCiteError
	{
		final Collection<String> strips = new ArrayList<String>();
		final Collection<String> shows = new ArrayList<String>();
		final Collection<String> omissions = new ArrayList<String>();
		final Collection<String> highlights = new ArrayList<String>();

		if (_insertion instanceof AnnotatedCitation) {
			final AnnotatedCitation ann = (AnnotatedCitation) _insertion;
			final String options = ';' + ann.annotation(); // make patterns work for first occurrence too
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
		fragment = formatAsHtml( fragment, _beginTag, _endTag );
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


	private String formatAsHtml( String _fragment, String _beginTag, String _endTag )
	{
		if (_fragment == null) {
			return null;
		}
		return javaToHtml( _fragment, _beginTag, _endTag );
	}


	protected abstract String javaToHtml( String _fragment, String _beginTag, String _endTag );


	private String replaceHighlightsWithHtml( String _fragment )
	{
		String result = _fragment;
		result = result.replace( Constants.BEGIN_HIGHLIGHT, BEGIN_HTML_HIGHLIGHT );
		result = result.replace( Constants.END_HIGHLIGHT, END_HTML_HIGHLIGHT );
		return result;
	}


}
