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
import java.util.HashMap;
import java.util.Map;

public abstract class TextBasedCitelet extends JCitelet
{

	protected TextBasedCitelet( JCite _jcite )
	{
		super( _jcite );
	}


	@Override
	public Citation cite( String _reference ) throws JCiteError, IOException
	{
		if (isVerbose()) {
			System.out.print( "  JCite citing " );
			System.out.println( _reference );
		}

		int endFileName = _reference.indexOf( ':' );
		int endFragmentName = _reference.length();
		if (endFileName < 0) {
			endFileName = endFragmentName;
		}
		else {
			int posOfSemicolon = _reference.indexOf( ';' );
			if (posOfSemicolon >= 0) {
				endFragmentName = posOfSemicolon;
			}
		}

		final String fileName = _reference.substring( 0, endFileName );
		final String fileSource = getSourceForFile( fileName );

		String fragment = fileSource;
		if (endFileName < endFragmentName) {
			final String fragmentName = _reference.substring( endFileName + 1, endFragmentName );
			fragment = getFragmentFrom( fileSource, fragmentName );
		}

		String instructions = "";
		if (endFragmentName < _reference.length()) {
			instructions = _reference.substring( endFragmentName + 1 ).trim();
		}

		return (instructions.length() > 0)
			? new AnnotatedCitation( fragment, instructions )
			: new Citation(fragment);
	}

	@Override protected Inlined inline( String _citation )
	{
		if (isVerbose()) {
			System.out.print( "  JCite citing inline element " );
			System.out.println( _citation );
		}
		return super.inline( _citation );
	}


	private Map<String, String> sources = new HashMap<String, String>();

	protected String getSourceForFile( String _fileName ) throws FileNotFoundError, IOException
	{
		final String cachedSource = this.sources.get( _fileName );
		if (null != cachedSource) return cachedSource;

		final File sourceFile = findSourceFile( _fileName );
		final String newSource = normalizeLineEndings( Util.readStringFrom( sourceFile ) );
		this.sources.put( _fileName, newSource );
		return newSource;
	}


	private String normalizeLineEndings( String _string )
	{
		return (_string.replace( Constants.CRLF, "\n" ).replace( Constants.CR, '\n' )
			// remove trailing spaces
			+ '\n').replaceAll( "\\x20+\\n", "\n" );
	}


	protected String getFragmentFrom( String _sourceText, String _fragmentName ) throws FragmentNotFoundError,
			UnclosedMarkupError
	{
		final FragmentMarker[] markers = markersFor( _fragmentName );
		final StringBuffer fragments = new StringBuffer();
		final FragmentLocator locator = new FragmentLocator();
		int scanFrom = 0;
		boolean found = false;
		while (scanFrom < _sourceText.length()) {
			if (FragmentMarker.findFragment( _sourceText, scanFrom, markers, locator )) {
				String fragment = _sourceText.substring( locator.beginFragment, locator.endFragment );
				fragments.append( fragment );
				scanFrom = locator.endSuffix;
				found = true;
			}
			else {
				break;
			}
		}
		if (!found) throw new FragmentNotFoundError( _sourceText, _fragmentName );
		return fragments.toString();
	}


	protected abstract FragmentMarker[] markersFor( String _fragmentName );


	// -- helpers
	protected final String stripIndentation( String _fragment )
	// -- helpers
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


	// -- helpers
	protected final String trimEmptyLines( String _fragment )
	// -- helpers
	{
		int iStart = 0;
		while (iStart < _fragment.length() && _fragment.charAt( iStart ) == '\n')
			iStart++;
		int iEnd = _fragment.length();
		while (iEnd > 0 && _fragment.charAt( iEnd - 1 ) == '\n')
			iEnd--;
		return _fragment.substring( iStart, iEnd );
	}


	// -- helpers
	protected final String escapeXML( String _fragment )
	// -- helpers
	{
		return _fragment.replace( "<", "&lt;" ).replace( ">", "&gt;" );
	}


}
