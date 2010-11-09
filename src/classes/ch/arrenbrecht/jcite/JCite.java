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
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.discovery.tools.Service;

import ch.arrenbrecht.jcite.Util.FileVisitor;
import ch.arrenbrecht.jcite.include.IncludeCitelet;
import ch.arrenbrecht.jcite.java.JavaCitelet;
import ch.arrenbrecht.jcite.path.PathCitelet;
import ch.arrenbrecht.jcite.text.TextCitelet;

public final class JCite
{

	/**
	 * Launches JCite from the command line.
	 *
	 * <p>
	 * The command line arguments are as follows: <br>
	 * <code>-i file</code> defines the input file/pattern. <br>
	 * <code>-o file</code> defines the output file/folder. <br>
	 * <code>-r</code> specifies recursive descent into subfolders.<br>
	 * <code>-sp path</code> defines a source path (may be repeated). <br>
	 * <code>-pp path</code> defines the project path which is stripped off the front of cited
	 * paths. <br>
	 * <code>-tt</code> puts the snippets within <code>\TT\></code> tags rather than
	 * <code>\<PRE\></code> tags.
	 * </p>
	 *
	 * @param _args lists the command line arguments.
	 */
	public static void main( String... _args )
	{
		try {
			final JCite jcite = new JCite();

			jcite.runWith( _args );

			if (jcite.errorCount() > 0) {
				System.err.println();
				System.err.println( "" + jcite.errorCount() + " error(s) encountered." );
				System.exit( 1 );
			}
			if (jcite.warningsCount() > 0) {
				System.err.println();
				System.err.println( "" + jcite.warningsCount() + " warning(s) encountered." );
				System.exit( 2 );
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit( 1 );
		}
	}


	private final Collection<JCitelet> citelets = new ArrayList<JCitelet>();
	private List<String> sourceFolders = new ArrayList<String>();
	private String projectPath = "";
	private boolean pre = false;
	private boolean verbose = false;
	private Collection<String> tripUpCollection = null;
	private TripwireDatabase tripwires = null;
	private boolean acceptTripUps = false;
	private File diffPath = null;
	private String differ = "";


	public JCite()
	{
		super();
		registerCitelet( new JavaCitelet( this ) );
		registerCitelet( new TextCitelet( this ) );
		registerCitelet( new PathCitelet( this ) );
		registerCitelet( new IncludeCitelet( this ) );

		@SuppressWarnings("rawtypes") //
		final Enumeration providers = Service.providers( JCiteletProvider.class );
		while (providers.hasMoreElements()) {
			final JCiteletProvider provider = (JCiteletProvider) providers.nextElement();
			registerCitelet( provider.getCitelet( this ) );
		}
	}

	public JCite( String[] _sourceFolders, boolean _usePRE, boolean _verbose )
	{
		this();
		setPRE( _usePRE );
		setVerbose( _verbose );
		for (String sourceFolder : _sourceFolders) {
			addSourceFolder( sourceFolder );
		}
	}


	/** Visible to test code. */
	void registerCitelet( JCitelet _citelet )
	{
		this.citelets.add( _citelet );
	}


	void setTripUpCollection( Collection<String> _tripUps )
	{
		this.tripUpCollection = _tripUps;
	}


	void runWith( String... _args ) throws IOException, JCiteError
	{
		final Collection<File> inputs = new ArrayList<File>( 1 );
		File output = null;
		boolean quiet = false;
		boolean recurse = false;

		int iArg = -1;
		while (++iArg < _args.length) {
			String arg = _args[ iArg ];
			if (arg.equalsIgnoreCase( "-i" ) || arg.equalsIgnoreCase( "--input" )) {
				inputs.add( new File( _args[ ++iArg ] ) );
			}
			else if (arg.equalsIgnoreCase( "-o" ) || arg.equalsIgnoreCase( "--output" )) {
				output = new File( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "-sp" ) || arg.equalsIgnoreCase( "--source-path" )) {
				addSourceFolder( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "-pp" ) || arg.equalsIgnoreCase( "--project-path" )) {
				setProjectPath( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "-tw" ) || arg.equalsIgnoreCase( "--tripwire-path" )) {
				this.tripwires = new TripwireDatabase( new File( _args[ ++iArg ] ), true );
			}
			else if (arg.equalsIgnoreCase( "-twf" ) || arg.equalsIgnoreCase( "--tripwire-file" )) {
				this.tripwires = new TripwireDatabase( new File( _args[ ++iArg ] ), false );
			}
			else if (arg.equalsIgnoreCase( "-ac" ) || arg.equalsIgnoreCase( "--accept-changes" )) {
				this.acceptTripUps = true;
			}
			else if (arg.equalsIgnoreCase( "-dp" ) || arg.equalsIgnoreCase( "--diff-path" )) {
				this.diffPath = new File( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "--differ" )) {
				this.differ = _args[ ++iArg ];
			}
			else if (arg.equalsIgnoreCase( "-v" ) || arg.equalsIgnoreCase( "--verbose" )) {
				setVerbose( true );
			}
			else if (arg.equalsIgnoreCase( "-q" ) || arg.equalsIgnoreCase( "--quiet" )) {
				quiet = true;
			}
			else if (arg.equalsIgnoreCase( "-r" ) || arg.equalsIgnoreCase( "--recursive" )) {
				recurse = true;
			}
			else if (arg.equalsIgnoreCase( "-license" ) || arg.equalsIgnoreCase( "--license" )) {
				System.out.println( Constants.INTRO );
				System.out.println( Constants.LICENSE );
				quiet = true;
			}
			else if (arg.equalsIgnoreCase( "-?" )
					|| arg.equalsIgnoreCase( "-h" ) || arg.equalsIgnoreCase( "-help" ) || arg.equalsIgnoreCase( "--help" )) {
				System.out.println( Constants.INTRO );
				System.out.println( Constants.HELP );
				quiet = true;
			}
			else if (argHandledByCitelet( arg )) {
				// ok
			}
			else {
				System.out.println( Constants.INTRO );
				System.out.println( "Invalid command line option: " + arg );
				System.out.println( Constants.HELP );
				quiet = true;
			}
		}

		if (!quiet) {
			System.out.println( Constants.INTRO );
			printCitelets();
		}

		setupTripwires( quiet );

		if (0 == inputs.size()) {
			System.out.println( "No input specified. Use -i <file>, or -h for help." );
		}
		else if (null == output) {
			System.out.println( "No output specified. Use -o <file>, or -h for help." );
		}
		else {
			for (File input : inputs) {
				if (!quiet) {
					System.out.println();
					System.out.println( "Processing " + input.getPath() );
				}
				if (isPattern( input.getName() )) {
					processPattern( input.getParentFile(), input.getName(), output, recurse );
				}
				else {
					process( input, output );
				}
			}
		}

		finalizeTripwires( quiet );

		if (citationCount() > 0) {
			System.out.println();
			System.out.println( "" + citationCount() + " citations processed." );
		}
	}

	public void setupTripwires( boolean quiet ) throws IOException
	{
		if (null != this.tripwires) {
			if (!quiet) {
				System.out.println();
				System.out.println( "Loading tripwire data from " + this.tripwires.toString() );
			}
			this.tripwires.load();
		}
	}

	public void finalizeTripwires( boolean quiet ) throws IOException
	{
		if (null != this.tripwires && errorCount() + warningsCount() == 0) {
			if (!quiet) {
				System.out.println();
				System.out.println( "Saving tripwire data to " + this.tripwires.toString() );
			}
			this.tripwires.save();
		}
	}

	final void printCitelets()
	{
		for (JCitelet c : this.citelets) {
			System.out.println( "Using " + c.getClass().getName() );
		}
	}

	private boolean argHandledByCitelet( String _arg )
	{
		for (JCitelet citelet : this.citelets) {
			if (citelet.argHandled( _arg )) return true;
		}
		return false;
	}

	private boolean isPattern( String _name )
	{
		return _name.indexOf( '*' ) >= 0 || _name.indexOf( '?' ) >= 0;
	}


	public final boolean isPRE()
	{
		return this.pre;
	}

	public final void setPRE( boolean _pre )
	{
		this.pre = _pre;
	}


	final boolean isVerbose()
	{
		return this.verbose;
	}

	final void setVerbose( boolean _verbose )
	{
		this.verbose = _verbose;
	}


	public final TripwireDatabase getTripwires()
	{
		return this.tripwires;
	}

	public final void setTripwires( TripwireDatabase _tripwires )
	{
		this.tripwires = _tripwires;
	}

	public final boolean getAcceptTripUps()
	{
		return this.acceptTripUps;
	}

	public final void setAcceptTripUps( boolean _acceptTripUps )
	{
		this.acceptTripUps = _acceptTripUps;
	}

	public File getDiffPath()
	{
		return this.diffPath;
	}

	public void setDiffPath( File _diffPath )
	{
		this.diffPath = _diffPath;
	}

	public void setDiffer( String _differ )
	{
		this.differ = _differ;
	}


	public final void addSourceFolder( String _sourceFolder )
	{
		this.sourceFolders.add( _sourceFolder );
	}

	List<String> sourceFolders()
	{
		return this.sourceFolders;
	}

	public String getProjectPath()
	{
		return this.projectPath;
	}

	public void setProjectPath( String _projectPath )
	{
		this.projectPath = _projectPath;
	}


	public void processPattern( File _inputFolder, String _pattern, File _outputFolder, boolean _recurse )
			throws IOException, JCiteError
	{
		Util.iterateFiles( _inputFolder, _pattern, _outputFolder, _recurse, new FileVisitor()
		{

			public void visit( File _inputFile, File _outputFile ) throws IOException, JCiteError
			{
				if (isVerbose()) {
					System.out.print( "JCite processing " );
					System.out.println( _inputFile.getPath() );
				}
				process( _inputFile, _outputFile );
			}

		} );
	}


	private File currentSourceFile = null;
	private String currentSourceText = null;
	private String currentCitationPrefix = null;

	public void process( File _source, File _target ) throws IOException, JCiteError
	{
		this.currentSourceFile = _source;
		try {
			String source = Util.readStringFrom( _source );
			String target = process( source );
			final File parentDir = _target.getParentFile();
			if (null != parentDir) parentDir.mkdirs();
			Util.writeStringTo( target, _target );
		}
		finally {
			this.currentSourceFile = null;
		}
	}

	public String process( String _source ) throws JCiteError
	{
		this.currentSourceText = _source;
		try {
			String result = _source;
			for (JCitelet citelet : this.citelets) {
				this.currentCitationPrefix = citelet.referencePrefix();
				try {
					result = citelet.process( result );
				}
				finally {
					this.currentCitationPrefix = null;
				}
			}
			return result;
		}
		finally {
			this.currentSourceText = null;
		}
	}


	void checkTripwires( String _citation, String _value, int _sourceIndex ) throws IOException
	{
		if (null != this.tripwires) {
			final String citation = this.currentCitationPrefix + ":" + _citation;
			final String name = this.tripwires.sanitizeName( citation );
			final String value = this.tripwires.sanitizeValue( _value );
			if (!this.tripwires.check( name, value )) {
				if (this.acceptTripUps) {
					this.tripwires.update( name, value );
				}
				else {
					if (null != this.tripUpCollection) {
						this.tripUpCollection.add( tripUpLocation( _sourceIndex ) );
					}
					else {
						logTripUp( citation, name, value, _sourceIndex );
					}
				}
			}
		}
	}

	void logTripUp( String _citation, String _name, String _value, int _sourceIndex ) throws IOException
	{
		warn( "Cited text changed in " + tripUpLocation( _sourceIndex ) + " for " + _citation );
		if (null != this.diffPath) {
			diff( this.tripwires.get( _name ), _value, this.currentSourceFile.getName() + "_" + _sourceIndex );
		}
	}

	private void diff( String _was, String _is, String _baseName ) throws IOException
	{
		this.diffPath.mkdirs();
		final File wasFile = new File( this.diffPath, _baseName + ".was" );
		final File isFile = new File( this.diffPath, _baseName + ".is" );
		Util.writeStringTo( _was, wasFile );
		Util.writeStringTo( _is, isFile );
		if (null != this.differ && !"".equals( this.differ )) {
			try {
				Util.execAndPipeOutputToSystem( this.differ, quotedName( wasFile ), quotedName( isFile ) );
			}
			catch (InterruptedException e) {
				// swallow
			}
		}
	}

	private String quotedName( File _file ) throws IOException
	{
		return _file.getCanonicalPath();
	}

	private String tripUpLocation( int _sourceIndex )
	{
		return this.currentSourceFile.getName() + ":" + indexToLineNumber( _sourceIndex, this.currentSourceText );
	}

	void logCitationError( Exception _e, @SuppressWarnings("unused") String _markup, int _sourceIndex )
	{
		final String errMsg = _e.getMessage();
		final StringBuilder msg = (null == errMsg) ? new StringBuilder( _e.getClass().getName() ) : new StringBuilder(
				errMsg );
		if (null != this.currentSourceFile) {
			msg.append( " In file " ).append( this.currentSourceFile.getPath() );
			msg.append( ':' ).append( indexToLineNumber( _sourceIndex, this.currentSourceText ) );
		}
		error( msg.toString() );
	}

	private int indexToLineNumber( int _index, String _text )
	{
		final int index = Math.min( _index, _text.length() - 1 );
		int result = 1;
		for (int i = 0; i <= index; i++) {
			switch (_text.charAt( i )) {
				case '\n':
					result++;
					break;
			}
		}
		return result;
	}


	private int warningsCount = 0;

	public void warn( String _message )
	{
		System.err.print( "WARNING: " );
		System.err.println( _message );
		this.warningsCount++;
	}

	public int warningsCount()
	{
		return this.warningsCount;
	}


	private int errorCount = 0;

	public void error( String _message )
	{
		System.err.print( "ERROR: " );
		System.err.println( _message );
		this.errorCount++;
	}

	public int errorCount()
	{
		return this.errorCount;
	}


	private int citationCount = 0;

	@SuppressWarnings("unused") //
	public void logCitation( String _markup, int _sourceIndex )
	{
		this.citationCount++;
	}

	public int citationCount()
	{
		return this.citationCount;
	}


}
