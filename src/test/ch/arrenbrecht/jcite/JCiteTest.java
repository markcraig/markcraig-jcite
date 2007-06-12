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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JCiteTest extends AbstractJCiteTest
{


	public void testTargetWithoutFolderSpec()
	{
		final String tmp = "index.html.tmp-copy-created-by-test";
		JCite.main( "-i", "index.html", "-o", tmp );
		new File( tmp ).delete();
	}


	public void testTripwireWithFolder() throws Exception
	{
		new TripwireTest( true ).run();
	}

	public void testTripwireWithFile() throws Exception
	{
		new TripwireTest( false ).run();
	}


	@SuppressWarnings("unqualified-field-access")
	private final class TripwireTest
	{
		private final boolean dbIsFolder;
		private final File IN = new File( "src/test/data" );
		private final File OUT = new File( "build/test/data" );
		private final File DB = new File( OUT, "tripwire" );
		private final File SRC_PATH = new File( OUT, "src" );
		private final File SRC = new File( SRC_PATH, "TripSource.java" );
		private final String DOC_NAME = "Tripwired.htm";
		private final File DOC = new File( OUT, DOC_NAME );
		private final File GEN = new File( OUT, DOC_NAME + ".gen.htm" );
		private final String TRIP1 = "jc:TripSource ---- demo";
		private final String TRIP2 = "jc:TripSource ---- aaa";
		private final String TRIP3 = "jc:TripSource ---- zzz";
		private final String CIT1 = "void doIt() { doIt(); }";
		private final String CIT1a = CIT1.replace( "doIt", "didIt" );
		private String src;
		private String doc;

		public TripwireTest(boolean _dbIsFolder) throws IOException
		{
			this.dbIsFolder = _dbIsFolder;
			this.src = Util.readStringFrom( new File( IN, "TripSource.java.txt" ) );
			this.doc = Util.readStringFrom( new File( IN, DOC_NAME ) );
		}

		public void run() throws Exception
		{
			setupInitialSource();
			cleanExistingDb();
			runWithEmptyDb();
			runAgain();
			changeThenRunAgain();
			runAgainAcceptingChanges();
			runAgainAfterChange();
			addCitationThenRunAgain();
			dropCitationThenRunAgain();
		}

		private void setupInitialSource() throws Exception
		{
			OUT.mkdirs();
			SRC_PATH.mkdirs();
			Util.writeStringTo( this.doc, DOC );
			Util.writeStringTo( this.src, SRC );
		}

		private void cleanExistingDb()
		{
			if (DB.exists()) {
				if (DB.isDirectory()) {
					for (final File f : DB.listFiles()) {
						f.delete();
					}
				}
				DB.delete();
			}
		}

		private void runWithEmptyDb() throws Exception
		{
			assertDb();
			final Collection<String> tripUps = jcite();
			assertEquals( "Tripped", 0, tripUps.size() );
			assertDb( TRIP1, CIT1 );
		}

		private void runAgain() throws Exception
		{
			final Collection<String> tripUps = jcite();
			assertEquals( "Tripped", 0, tripUps.size() );
			assertDb( TRIP1, CIT1 );
		}

		private void changeThenRunAgain() throws Exception
		{
			this.src = this.src.replace( "doIt", "didIt" );
			Util.writeStringTo( this.src, SRC );
			final Collection<String> tripUps = jcite();
			assertStrings( tripUps, "Tripwired.htm:4" );
			assertDb( TRIP1, CIT1 );
		}

		private void runAgainAcceptingChanges() throws Exception
		{
			final Collection<String> tripUps = jcite( "-ac" );
			assertEquals( "Tripped", 0, tripUps.size() );
			assertDb( TRIP1, CIT1a );
		}

		private void runAgainAfterChange() throws Exception
		{
			final Collection<String> tripUps = jcite();
			assertEquals( "Tripped", 0, tripUps.size() );
			assertDb( TRIP1, CIT1a );
		}

		private void addCitationThenRunAgain() throws Exception
		{
			final String newDoc = this.doc.replace( "</pre>",
					"</pre>\n<pre>[jc:TripSource:---- zzz]</pre>\n<pre>[jc:TripSource:---- aaa]</pre>" );
			Util.writeStringTo( newDoc, DOC );
			final Collection<String> tripUps = jcite();
			assertEquals( "Tripped", 0, tripUps.size() );
			assertDb( TRIP1, CIT1a, TRIP2, "void aaa() {}", TRIP3, "void zzz() {}" );
		}

		private void dropCitationThenRunAgain() throws Exception
		{
			Util.writeStringTo( this.doc, DOC );
			final Collection<String> tripUps = jcite();
			assertEquals( "Tripped", 0, tripUps.size() );
			assertDb( TRIP1, CIT1a );
		}


		final String[] ARGS = new String[] { "-i", DOC.getPath(), "-o", GEN.getPath(), "-tw", DB.getPath(), "-sp",
				SRC_PATH.getPath() };

		private Collection<String> jcite( String... _args ) throws Exception
		{
			if (!dbIsFolder) {
				ARGS[ 4 ] = "-twf";
			}

			final JCite jc = new JCite();
			final Collection<String> tripUps = new ArrayList<String>();
			jc.setTripUpCollection( tripUps );
			if (_args.length > 0) {
				final List<String> argList = new ArrayList<String>( ARGS.length + _args.length );
				argList.addAll( Arrays.asList( ARGS ) );
				argList.addAll( Arrays.asList( _args ) );
				jc.runWith( argList.toArray( new String[ argList.size() ] ) );
			}
			else {
				jc.runWith( ARGS );
			}
			return tripUps;
		}


		private void assertDb( String... _namesAndValues ) throws Exception
		{
			if (this.dbIsFolder) {
				assertDbFolder( _namesAndValues );
			}
			else {
				assertDbFile( _namesAndValues );
			}
		}


		private void assertDbFolder( String[] _namesAndValues ) throws Exception
		{
			if (_namesAndValues.length == 0) {
				assertNull( DB.listFiles() );
			}
			else {
				assertEquals( _namesAndValues.length / 2, DB.listFiles().length );
				int i = 0;
				while (i < _namesAndValues.length) {
					String name = _namesAndValues[ i++ ];
					String value = _namesAndValues[ i++ ];
					assertFileContains( value, new File( this.DB, TripwireDatabase.sanitizeNameInFolder( name ) + ".txt" ) );
				}
			}
		}

		private void assertDbFile( String[] _namesAndValues ) throws Exception
		{
			// TODO Auto-generated method stub

		}

		private void assertFileContains( String _expected, File _file ) throws IOException
		{
			assertFileExists( _file );
			assertEquals( _expected, Util.readStringFrom( _file ) );
		}

		private void assertFileExists( File _file )
		{
			assertTrue( _file.getPath() + " does not exist", _file.exists() );
		}

		private void assertStrings( Collection<String> _actual, String... _expected )
		{
			assertEquals( _expected.length, _actual.size() );
			int i = 0;
			for (final String actual : _actual) {
				assertEquals( _expected[ i++ ], actual );
			}
		}

	}


}
