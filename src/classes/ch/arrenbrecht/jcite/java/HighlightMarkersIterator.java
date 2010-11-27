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

import ch.arrenbrecht.jcite.Constants;
import ch.arrenbrecht.jcite.FragmentLocator;

class HighlightMarkersIterator extends MarkerIterator
{

	public HighlightMarkersIterator( AbstractJavaCitelet _citelet )
	{
		super( _citelet );
	}

	@Override
	protected void visit( String _source, FragmentLocator _locator, String _fragment, StringBuilder _result )
	{
		insertSpaceBeforeIfNeeded( _source, _locator, _fragment, _result );

		_result.append( Constants.BEGIN_HIGHLIGHT ).append( _fragment ).append( Constants.END_HIGHLIGHT );

		insertSpaceAfterIfNeeded( _source, _locator, _fragment, _result );
	}

	private final void insertSpaceBeforeIfNeeded( String _source, FragmentLocator _locator, String _fragment,
			StringBuilder _result )
	{
		if (_locator.beginPrefix > 0 && _fragment.length() > 0) {
			insertSpaceIfNeeded( _source, _fragment, _result, _locator.beginPrefix - 1, 0, "=,;", "=" );
		}
	}

	private final void insertSpaceAfterIfNeeded( String _source, FragmentLocator _locator, String _fragment,
			StringBuilder _result )
	{
		if (_locator.endSuffix < _source.length() && _fragment.length() > 0) {
			insertSpaceIfNeeded( _source, _fragment, _result, _locator.endSuffix, _fragment.length() - 1, "=", "=})]>,;" );
		}
	}

	private final void insertSpaceIfNeeded( String _source, String _fragment, StringBuilder _result, int _outsideIndex,
			int _insideIndex, String _outsidesLikeLetters, String _insidesLikeLetters )
	{
		final char outside = _source.charAt( _outsideIndex );
		final char inside = _fragment.charAt( _insideIndex );
		if ((Character.isLetterOrDigit( outside ) || _outsidesLikeLetters.indexOf( outside ) >= 0)
				&& (Character.isLetterOrDigit( inside ) || _insidesLikeLetters.indexOf( inside ) >= 0)) {
			_result.append( ' ' );
		}
	}

}