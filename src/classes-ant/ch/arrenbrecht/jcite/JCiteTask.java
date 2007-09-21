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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;


/**
 * ANT task for citing source material into documentation.
 * 
 * @author peo
 */
public final class JCiteTask extends MatchingTask
{
	private File srcDir;
	private File destDir = null;
	private File file;
	private Path sourcePath;
	private File projectPath = null;
	private boolean usePRE = true;
	private boolean verbose = false;
	private boolean failOnWarning = false;
	private File tripwireFile = null;
	private File tripwirePath = null;
	private String tripwireNewLine = null;
	private boolean acceptChanges = false;
	private File diffPath = null;
	private String differ = "";


	public JCiteTask()
	{
		super();
		/*
		 * TODO Reroute logging calls through JCite with different logging levels so I can redirect
		 * them here.
		 */
	}


	public final void setSrcDir( File _srcDir )
	{
		this.srcDir = _srcDir;
	}

	public final void setDestDir( File _destDir )
	{
		this.destDir = _destDir;
	}

	public final void setFile( File _file )
	{
		this.file = _file;
	}

	public Path getSourcePath()
	{
		return this.sourcePath;
	}

	public Path createSourcePath()
	{
		if (this.sourcePath == null) {
			this.sourcePath = new Path( getProject() );
		}
		return this.sourcePath.createPath();
	}

	public void setSourcePathRef( Reference r )
	{
		createSourcePath().setRefid( r );
	}
	
	public File getProjectPath()
	{
		return this.projectPath;
	}
	
	public void setProjectPath( File _projectPath )
	{
		this.projectPath = _projectPath;
	}

	public void setFailOnWarning( boolean _failOnWarning )
	{
		this.failOnWarning = _failOnWarning;
	}

	public void setUsePRE( boolean _usePRE )
	{
		this.usePRE = _usePRE;
	}

	public void setVerbose( boolean _verbose )
	{
		this.verbose = _verbose;
	}

	public void setTripwireFile( File _tripwireFile )
	{
		this.tripwireFile = _tripwireFile;
	}

	public void setTripwirePath( File _tripwirePath )
	{
		this.tripwirePath = _tripwirePath;
	}

	public void setTripwireNewLine( String _tripwireNewLine )
	{
		this.tripwireNewLine = _tripwireNewLine.replace( "\\r", "\r" ).replace( "\\n", "\n" );
	}

	public void setAcceptChanges( boolean _acceptChanges )
	{
		this.acceptChanges = _acceptChanges;
	}

	public void setDiffPath( File _diffPath )
	{
		this.diffPath = _diffPath;
	}

	public void setDiffer( String _differ )
	{
		this.differ = _differ;
	}


	@Override
	public void execute() throws BuildException
	{
		try {
			validate();

			final String[] sourcePath = (this.sourcePath != null)? this.sourcePath.list() : new String[ 0 ];
			final JCite jcite = new JCite( sourcePath, this.usePRE, this.verbose );

			if (this.verbose) {
				jcite.printCitelets();
			}

			if (null != this.projectPath) {
				log( "Project path is " + this.projectPath, Project.MSG_VERBOSE );
				jcite.setProjectPath( this.projectPath.getPath() );
			}
			if (this.tripwireFile != null) {
				log( "Using tripwires from file " + this.tripwireFile, Project.MSG_VERBOSE );
				jcite.setTripwires( new TripwireDatabase( this.tripwireFile, false, this.tripwireNewLine ) );
			}
			else if (this.tripwirePath != null) {
				log( "Using tripwires from folder " + this.tripwirePath, Project.MSG_VERBOSE );
				jcite.setTripwires( new TripwireDatabase( this.tripwirePath, true, this.tripwireNewLine ) );
			}
			jcite.setAcceptTripUps( this.acceptChanges );
			jcite.setDiffPath( this.diffPath );
			jcite.setDiffer( this.differ );
			jcite.setupTripwires( !this.verbose );

			final DirectoryScanner ds = super.getDirectoryScanner( this.srcDir );
			final String[] files = ds.getIncludedFiles();
			for (final String file : files) {
				try {
					log( "Processing " + file, Project.MSG_VERBOSE );
					jcite.process( new File( this.srcDir, file ), new File( this.destDir, file ) );
				}
				catch (JCiteError e) {
					throw new BuildException( e );
				}
			}

			jcite.finalizeTripwires( !this.verbose );

			log( "" + jcite.citationCount() + " citations processed." );
			if (jcite.warningsCount() > 0) {
				final String msg = "" + jcite.warningsCount() + " warnings encountered.";
				log( msg, Project.MSG_WARN );
				if (this.failOnWarning) {
					throw new BuildException( msg );
				}
			}
			if (jcite.errorCount() > 0) {
				final String msg = "" + jcite.errorCount() + " errors encountered.";
				log( msg, Project.MSG_ERR );
				throw new BuildException( msg );
			}
		}
		catch (IOException e) {
			throw new BuildException( e );
		}
	}


	/**
	 * Validates arguments to the task prior to its execution. Adapted from ANT's FixCRLF task.
	 */
	private void validate()
	{
		if (this.file != null) {
			if (this.srcDir != null) {
				throw new BuildException( "cannot set srcdir and file simultaneously!" );
			}
			// patch file into the fileset
			this.fileset.setFile( this.file );
			// set our parent dir
			this.srcDir = this.file.getParentFile();
		}
		if (this.srcDir == null) {
			throw new BuildException( "srcdir attribute must be set!" );
		}
		if (!this.srcDir.exists()) {
			throw new BuildException( "srcdir does not exist!" );
		}
		if (!this.srcDir.isDirectory()) {
			throw new BuildException( "srcdir is not a directory!" );
		}
		if (this.destDir != null) {
			if (!this.destDir.exists()) {
				throw new BuildException( "destdir does not exist!" );
			}
			if (!this.destDir.isDirectory()) {
				throw new BuildException( "destdir is not a directory!" );
			}
		}
	}


}
