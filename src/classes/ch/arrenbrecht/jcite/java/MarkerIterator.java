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

import java.util.Collection;

import ch.arrenbrecht.jcite.FragmentLocator;
import ch.arrenbrecht.jcite.FragmentMarker;
import ch.arrenbrecht.jcite.UnclosedMarkupError;

abstract class MarkerIterator
{
	private final JavaCitelet citelet;

	public MarkerIterator( JavaCitelet _citelet )
	{
		this.citelet = _citelet;
	}

	public String iterate( String _fragment, Collection<String> _markerNames ) throws UnclosedMarkupError
	{
		String result = _fragment;
		for (String markerName : _markerNames) {
			final StringBuilder builder = new StringBuilder();
			final FragmentMarker[] markers = this.citelet.markersFor( markerName );
			final FragmentLocator locator = new FragmentLocator();
			int scanFrom = 0;
			while (scanFrom < result.length()) {
				if (FragmentMarker.findFragment( result, scanFrom, markers, locator )) {
					final String prefix = result.substring( scanFrom, locator.beginPrefix );
					final String infix = result.substring( locator.beginFragment, locator.endFragment );
					builder.append( prefix );
					visit( result, locator, infix, builder );
					scanFrom = locator.endSuffix;
				}
				else {
					break;
				}
			}
			if (scanFrom > 0) {
				builder.append( result.substring( scanFrom ) );
				result = builder.toString();
			}
		}
		return result;
	}


	protected abstract void visit( String _source, FragmentLocator _locator, String _fragment, StringBuilder _result );

}