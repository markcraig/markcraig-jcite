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
import java.util.Locale;
import java.util.TimeZone;

public class JCiteExcelTest extends AbstractJCiteTest
{
	private Locale prevLocale;
	private TimeZone prevTimeZone;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.prevLocale = Locale.getDefault();
		this.prevTimeZone = TimeZone.getDefault();
		Locale.setDefault( Locale.US );
		TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );
	}

	@Override
	protected void tearDown() throws Exception
	{
		Locale.setDefault( this.prevLocale );
		TimeZone.setDefault( this.prevTimeZone );
		super.tearDown();
	}


	public void testExcel() throws Exception
	{
		if (!new File( "lib/jxl.jar" ).exists()) return;

		File htmlSource = new File( "temp/doc/excel.htm" );
		File htmlExpected = new File( "src/test/data/excel_expected.htm" );
		File htmlTarget = new File( "temp/test/data/excel_out.htm" );
		htmlTarget.getParentFile().mkdirs();
		new JCite( (new String[] { "src/test/data" }), true, false ).process( htmlSource, htmlTarget );

		assertEquivalentHtmlFiles( htmlExpected, htmlTarget );
	}

	public void testTripwireWithFolder() throws Exception
	{
		new TripwireTest().run();
	}


	@SuppressWarnings("unqualified-field-access")
	private final class TripwireTest
	{
		private final File IN = new File( "src/test/data" );
		private final File OUT = new File( "temp/test/data" );
		private final File DB = new File( OUT, "tripwire" );
		private final File SRC_PATH = new File( OUT, "src" );
		private final String DOC_NAME = "Tripwired_Excel.htm";
		private final File DOC = new File( OUT, DOC_NAME );
		private final File GEN = new File( OUT, DOC_NAME + ".gen.htm" );
		private final String TRIP1 = "xc:TripSource.xls";
		private String doc;

		public TripwireTest() throws IOException
		{
			this.doc = Util.readStringFrom( new File( IN, DOC_NAME ) );
		}

		public void run() throws Exception
		{
			setupInitialSource();
			cleanExistingDb();
			runWithEmptyDb();
		}

		private void setupInitialSource() throws Exception
		{
			OUT.mkdirs();
			SRC_PATH.mkdirs();
			Util.writeStringTo( this.doc, DOC );
			Util.copy( new File( IN, "TripSource.xls" ), new File( SRC_PATH, "TripSource.xls" ) );
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
			assertDb( TRIP1 );
		}

		final String[] ARGS = new String[] { "-i", DOC.getPath(), "-o", GEN.getPath(), "-tw", DB.getPath(), "-sp",
				SRC_PATH.getPath() };

		private Collection<String> jcite( String... _args ) throws Exception
		{
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


		private void assertDb( String... _names ) throws Exception
		{
			assertDbFolder( _names );
		}


		private void assertDbFolder( String[] _names ) throws Exception
		{
			if (_names.length == 0) {
				assertNull( DB.listFiles() );
			}
			else {
				assertEquals( _names.length, DB.listFiles().length );
				int i = 0;
				while (i < _names.length) {
					String name = _names[ i++ ];
					assertFileExists( new File( this.DB, TripwireDatabase.sanitizeNameInFolder( name ) + ".txt" ) );
				}
			}
		}

		private void assertFileExists( File _file )
		{
			assertTrue( _file.getPath() + " does not exist", _file.exists() );
		}

	}


}
