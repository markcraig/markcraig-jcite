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
package ch.arrenbrecht.jcite.text;

import ch.arrenbrecht.jcite.BlockMarker;
import ch.arrenbrecht.jcite.FragmentMarker;
import ch.arrenbrecht.jcite.JCite;
import ch.arrenbrecht.jcite.JCiteError;
import ch.arrenbrecht.jcite.TextBasedCitelet;

public abstract class AbstractTextCitelet extends TextBasedCitelet
{

	public AbstractTextCitelet( JCite _jcite )
	{
		super( _jcite );
	}


	@Override
	protected FragmentMarker[] markersFor( String _fragmentName )
	{
		final String tag = _fragmentName.startsWith( "xml!" )? "<!--" + _fragmentName.substring( 4 ) + "-->\n"
				: _fragmentName + "\n";
		return new FragmentMarker[] { new BlockMarker( tag ) };
	}


	@Override
	public final String format( Insertion _insertion ) throws JCiteError
	{
		return format( _insertion, "<pre>", "</pre>" );
	}

	@Override
	public String format( Insertion _insertion, String _beginTag, String _endTag ) throws JCiteError
	{
		String fragment = _insertion.text();
		fragment = escapeXML( fragment );
		fragment = stripIndentation( fragment );
		fragment = trimEmptyLines( fragment );
		fragment = formatAsHtml( fragment, _beginTag, _endTag );
		return fragment;
	}


	protected abstract String formatAsHtml( String _fragment, String _beginTag, String _endTag );

}
