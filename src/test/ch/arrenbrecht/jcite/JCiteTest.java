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

import java.util.Collection;

import junit.framework.TestCase;

public class JCiteTest extends TestCase
{

	public void testDummy() throws Exception
	{
		// This class defines sample source only.
	}


	// ---- sampleMethodSrc
	// ---- sampleMethod
	/**
	 * This is a <b>sample</b> method. Just like {@link #sampleCode()}.
	 * 
	 * @author Peter Arrenbrecht
	 */
	void sampleMethod()
	{

		// This is a quick test of JCite.
		sampleMethod();
		if ("a" == "b") System.out.println( "Oops!" );
		try {
			int a = 1;
			System.out.println( a );
		}
		finally {
			System.out.println( "Done." );
		}

	}


	// ---- sampleMethod
	// ---- sampleMethodSrc


	// ---- fragmentSrc
	void sampleCode()
	{

		// This is not part of the sample.
		int a;

		// ---- fragment1
		if (1 == 2) a = 0;
		else a = 1;
		// ---- fragment1

		int b;
		// ---- fragment2
		// Part 1
		if (a == 1) {
			b = 0;
		}
		else {
			b = 1;
		}
		// ---- fragment2

		int c;

		// ---- fragment2
		// Part 2
		c = a + b;
		// ---- fragment2

		System.out.println( c );
		System.out./* -lineFragment- */println( "Hello, world!" )/* -lineFragment- */;
	}
	// ---- fragmentSrc


	// ---- sampleHighlightingSrc
	void sampleHighlighting()
	{
		// ---- sampleHighlighting
		int a = 0;
		// -hl-
		int b = 1;
		// -hl-
		int c = a + 2 /* -hl- */* b/* -hl- */;
		System.out.println( c );
		// ---- sampleHighlighting
	}
	// ---- sampleHighlightingSrc


	// ---- simpleHighlightingSrc
	void simpleHighlighting()
	{
		int b = 1;
		// ---- simpleHighlighting
		int c = 2 /**/* b/**/;
		System.out.println( c );
		// ---- simpleHighlighting
	}

	// ---- simpleHighlightingSrc


	// ---- fixedHighlightingSrc
	abstract class Foo
	{
		// ---- fixedHighlighting
		abstract/**/int/**/bar();
		abstract/**/int[]/**/baz();
		abstract/**/Collection<String>/**/duh();
		void doh()
		{
			int /**/var/**/= 0;
		}
		// ---- fixedHighlighting
	}
	// ---- fixedHighlightingSrc


	// ---- sampleOmissionSrc
	void sampleOmission()
	{
		// ---- sampleOmission
		int a = 0;
		// -try-
		try {
			// -try-
			something();
			// -try-
		}
		finally {
			cleanup();
		}
		// -try-
		System.out.println( a );
		// ---- sampleOmission
	}
	// ---- sampleOmissionSrc


	private void something()
	{
		// Dummy
	}


	private void cleanup()
	{
		// Dummy
	}


}
