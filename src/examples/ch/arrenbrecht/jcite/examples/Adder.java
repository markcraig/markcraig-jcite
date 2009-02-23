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
package ch.arrenbrecht.jcite.examples;

// ---- noPath
/**
 * Class that adds a specific value to other values. Typical usage: {@.jcite AdderTest:-- useCase}
 *
 * Note how we can omit the package specification of the target class if it resides in the same
 * package as this class. If the class is in another package, we have to specify it:
 * {@.jcite ch.arrenbrecht.jcite.examples.otherpackage.SomethingElse:-- fromOtherPackage}
 *
 * @author peo
 */
// ---- noPath
public class Adder
{
	private final int addend;


	/**
	 * Creates a new adder.
	 *
	 * @param addThis the number added by {@link #addTo(int)}.
	 */
	public Adder(int addThis)
	{
		super();
		this.addend = addThis;
	}


	/**
	 * Adds the number with which this Adder was constructed to the argument. Typical usage:
	 * {@.jcite AdderTest:-- useCase}
	 *
	 * @param x the number to add this Adder's number to.
	 * @return the sum.
	 */
	public int addTo( int x )
	{
		return x + this.addend;
	}


	// ---- noClass
	/**
	 * Adds two numbers. Example: {@.jcite AdderTest:-- add}
	 *
	 * Note how, in the parameter documentation, we can cite from the current class's source
	 * directly, that is, omitting the target class part.
	 *
	 * @param a first addend, as in {@.jcite -- add; highlight a; strip b}
	 * @param b second addend, as in {@.jcite -- add; highlight b; strip a}
	 */
	public static int add( int a, int b )
	{
		// -- add
		return /*a*/a/*a*/ + /*b*/b/*b*/;
		// -- add
	}
	// ---- noClass


	// ---- fullPath
	/**
	 * Returns the addend which was passed to the constructor. As in:
	 * {@.jcite ch.arrenbrecht.jcite.examples.AdderTest:-- getAddend}
	 *
	 * @return the addend.
	 */
	// ---- fullPath
	public int getAddend()
	{
		return this.addend;
	}

}
