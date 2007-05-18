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
import java.util.Iterator;
import java.util.List;

import sun.misc.Service;
import ch.arrenbrecht.jcite.Util.FileVisitor;
import ch.arrenbrecht.jcite.include.IncludeCitelet;
import ch.arrenbrecht.jcite.java.JavaCitelet;


public class JCite
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
	 * <code>-tt</code> puts the snippets within <code>\TT\></code> tags rather than
	 * <code>\<PRE\></code> tags.
	 * </p>
	 * 
	 * @param _args lists the command line arguments.
	 */
	public static void main( String... _args )
	{
		new JCite().runWith( _args );
	}


	private final Collection<JCitelet> citelets = new ArrayList<JCitelet>();
	private List<String> sourceFolders = new ArrayList<String>();
	private boolean pre = false;
	private boolean verbose = false;


	public JCite()
	{
		super();
		registerCitelet( new JavaCitelet( this ) );
		registerCitelet( new IncludeCitelet( this ) );

		final Iterator providers = Service.providers( JCiteletProvider.class );
		while (providers.hasNext()) {
			final JCiteletProvider provider = (JCiteletProvider) providers.next();
			registerCitelet( provider.getCitelet( this ) );
		}
	}

	public JCite(String[] _sourceFolders, boolean _usePRE, boolean _verbose)
	{
		this();
		setPRE( _usePRE );
		setVerbose( _verbose );
		for (String sourceFolder : _sourceFolders) {
			addSourceFolder( sourceFolder );
		}
	}


	private void registerCitelet( JCitelet _citelet )
	{
		this.citelets.add( _citelet );
	}


	private void runWith( String[] _args )
	{
		File input = null;
		File output = null;
		boolean quiet = false;
		boolean recurse = false;

		int iArg = -1;
		while (++iArg < _args.length) {
			String arg = _args[ iArg ];
			if (arg.equalsIgnoreCase( "-i" )) {
				input = new File( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "-o" )) {
				output = new File( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "-sp" )) {
				addSourceFolder( _args[ ++iArg ] );
			}
			else if (arg.equalsIgnoreCase( "-v" )) {
				setVerbose( true );
			}
			else if (arg.equalsIgnoreCase( "-q" )) {
				quiet = true;
			}
			else if (arg.equalsIgnoreCase( "-r" )) {
				recurse = true;
			}
			else if (arg.equalsIgnoreCase( "-license" )) {
				System.out.println( Constants.INTRO );
				System.out.println( Constants.LICENSE );
				quiet = true;
			}
			else if (arg.equalsIgnoreCase( "-?" ) || arg.equalsIgnoreCase( "-h" ) || arg.equalsIgnoreCase( "-help" )) {
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

		if (null == input) {
			System.out.println( "No input specified. Use -i <file>, or -h for help." );
		}
		else if (null == output) {
			System.out.println( "No output specified. Use -o <file>, or -h for help." );
		}
		else {
			try {
				if (isPattern( input.getName() )) {
					processPattern( input.getParentFile(), input.getName(), output, recurse );
				}
				else {
					process( input, output );
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit( 1 );
			}
		}
	}

	private final void printCitelets()
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


	boolean isVerbose()
	{
		return this.verbose;
	}

	final void setVerbose( boolean _verbose )
	{
		this.verbose = _verbose;
	}


	public final void addSourceFolder( String _sourceFolder )
	{
		this.sourceFolders.add( _sourceFolder );
	}

	List<String> sourceFolders()
	{
		return this.sourceFolders;
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


	public void process( File _source, File _target ) throws IOException, JCiteError
	{
		String source = Util.readStringFrom( _source );
		String target = process( source );
		final File parentDir = _target.getParentFile();
		if (null != parentDir) parentDir.mkdirs();
		Util.writeStringTo( target, _target );
	}


	public String process( String _source ) throws JCiteError, IOException
	{
		String result = _source;
		for (JCitelet citelet : this.citelets) {
			result = citelet.process( result );
		}
		return result;
	}


}
