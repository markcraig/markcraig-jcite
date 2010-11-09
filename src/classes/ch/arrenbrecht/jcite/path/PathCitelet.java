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
package ch.arrenbrecht.jcite.path;

import java.io.IOException;

import ch.arrenbrecht.jcite.JCite;
import ch.arrenbrecht.jcite.JCiteError;
import ch.arrenbrecht.jcite.JCitelet;

public class PathCitelet extends JCitelet
{
	private static final String PATH_PRE = "<pre class=\"j-path\">";
	private static final String PATH_POST = "</pre>";
	private static final String SEP_PRE = "<span class=\"j-pathsep\">";
	private static final String SEP_POST = "</span>";


	public PathCitelet( JCite _jcite )
	{
		super( _jcite );
	}


	@Override
	protected String referencePrefix()
	{
		return "path";
	}


	@Override
	protected Citation cite( String _markup ) throws JCiteError, IOException
	{
		return new Citation( stripProjectPathFrom( findSourcePath( _markup ) ) );
	}


	@Override
	protected String format( Insertion _insertion )
	{
		// "/" is last to avoid it replacing </span>s.
		return PATH_PRE + formatSeparators( _insertion.text(), "\\", ":", "/" ) + PATH_POST;
	}


	private String formatSeparators( String _path, String... _separators )
	{
		String result = _path;
		for (String sep : _separators) {
			result = result.replace( sep, SEP_PRE + sep + SEP_POST );
		}
		return result;
	}

}
