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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class AbstractJavaCiteletTest extends TestCase
{


	public void testHighlightPattern()
	{
		Pattern p = AbstractJavaCitelet.highlightPattern;
		Matcher m1 = p.matcher( "; omit -try-; highlight -b-; highlight -c-; omit -d-" );
		assertTrue( m1.find() );
		assertEquals( "-b-", m1.group( 1 ) );
		assertTrue( m1.find() );
		assertEquals( "-c-", m1.group( 1 ) );
		assertFalse( m1.find() );
		Matcher m2 = p.matcher( "; omit -b-" );
		assertFalse( m2.lookingAt() );
	}


	public void testOmitPattern()
	{
		Pattern p = AbstractJavaCitelet.omitPattern;
		Matcher m1 = p.matcher( "; omit -try-; highlight -b-; highlight -c-; omit -d-" );
		assertTrue( m1.find() );
		assertEquals( "-try-", m1.group( 1 ) );
		assertTrue( m1.find() );
		assertEquals( "-d-", m1.group( 1 ) );
		assertFalse( m1.find() );
		Matcher m2 = p.matcher( "; highlight -b-" );
		assertFalse( m2.lookingAt() );
	}



}
