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


public abstract class FragmentMarker
{


	public static boolean findFragment( String _in, int _startingAt, FragmentMarker[] _markers, FragmentLocator _locator )
			throws UnclosedMarkupError
	{
		for (FragmentMarker marker : _markers) {
			if (marker.find( _in, _startingAt, _locator )) return true;
		}
		return false;
	}


	public String prefix;
	public String suffix;


	public boolean isBlock()
	{
		return false;
	}


	public final boolean find( String _in, int _startingAt, FragmentLocator _locator ) throws UnclosedMarkupError
	{
		final int beginPrefix = _in.indexOf( this.prefix, _startingAt );
		if (beginPrefix >= 0) {
			_locator.beginPrefix = beginPrefix;
			_locator.beginFragment = beginPrefix + this.prefix.length();
			int beginSuffix = _in.indexOf( this.suffix, _locator.beginFragment );
			if (beginSuffix < 0)
				throw new UnclosedMarkupError( getClass().getSimpleName() + " can't find '" + this.suffix
						+ "' in (position=" + _locator.beginFragment + "):\n"
						+ _in.substring( _locator.beginFragment ) );
			_locator.endFragment = beginSuffix;
			if (isBlock()) {
				_locator.endFragment = Util.scanBackTo( _in, '\n', _locator.endFragment ) + 1;
			}
			_locator.endSuffix = beginSuffix + this.suffix.length();
			_locator.marker = this;
			return true;
		}
		return false;
	}
}