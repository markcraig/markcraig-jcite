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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class UtilTest extends TestCase
{

	public void testReadStringFrom() throws IOException
	{
		File htmlSource = new File( "src/doc/simplestyle.html" );
		File htmlTarget = new File( "build/test/data/simplestyle_copy.html" );
		htmlTarget.getParentFile().mkdirs();
		String source = Util.readStringFrom( htmlSource );
		Util.writeStringTo( source, htmlTarget );

		BufferedReader sourceReader = new BufferedReader( new FileReader( htmlSource ) );
		BufferedReader targetReader = new BufferedReader( new FileReader( htmlTarget ) );
		int sourceChar;
		while ((sourceChar = sourceReader.read()) != -1)
			assertEquals( sourceChar, targetReader.read() );
		assertEquals( -1, targetReader.read() );
	}


	public void testIterateFiles() throws Exception
	{
		final String[] paths = new String[] { "root.txt", "other.txt", "other.nomatch", "sub/one.txt", "sub/sub/two.txt",
				"skip/nomatch.csv", "subsub/sub/match.txt" };

		final File root = new File( "build/test/data/input" );
		root.deleteOnExit();
		root.mkdirs();
		for (String path : paths) {
			final File f = new File( root, path );
			f.getParentFile().mkdirs();
			f.createNewFile();
		}

		final File outRoot = new File( "build/test/data/output" );

		assertFiles( root, outRoot, paths, new int[] { 0, 1 }, false );
		assertFiles( root, outRoot, paths, new int[] { 0, 1, 3, 4, 6 }, true );
	}


	private void assertFiles( File _inRoot, File _outRoot, String[] _paths, int[] _is, boolean _recurse )
			throws Exception
	{
		final Map<String, String> files = new HashMap<String, String>();

		Util.iterateFiles( _inRoot, "*.txt", _outRoot, _recurse, new Util.FileVisitor()
		{

			public void visit( File _inputFile, File _outputFile )
			{
				files.put( _inputFile.getPath(), _outputFile.getPath() );
			}

		} );

		assertEquals( _is.length, files.size() );
		for (int i : _is) {
			final String path = _paths[ i ];
			final File inFile = new File( _inRoot, path );
			final File outFile = new File( _outRoot, path );
			assertEquals( outFile.getPath(), files.get( inFile.getPath() ) );
		}
	}

}
