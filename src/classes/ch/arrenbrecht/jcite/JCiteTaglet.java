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
import java.util.Map;

import ch.arrenbrecht.jcite.java.JavaCitelet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * A JavaDoc taglet to cite source code into API documentation using the "@jcite" tag.
 *
 * @author peo
 */
public class JCiteTaglet implements Taglet
{
	private static final String NAME = ".jcite";

	@SuppressWarnings({ "unchecked", "rawtypes" }) //
	public static void register( Map tagletMap )
	{
		final JCiteTaglet tag = new JCiteTaglet();
		final Taglet t = (Taglet) tagletMap.get( tag.getName() );
		if (t != null) {
			tagletMap.remove( tag.getName() );
		}
		tagletMap.put( tag.getName(), tag );
	}

	public String getName()
	{
		return NAME;
	}

	public boolean inField()
	{
		return true;
	}

	public boolean inConstructor()
	{
		return true;
	}

	public boolean inMethod()
	{
		return true;
	}

	public boolean inOverview()
	{
		return true;
	}

	public boolean inPackage()
	{
		return true;
	}

	public boolean inType()
	{
		return true;
	}

	public boolean isInlineTag()
	{
		return true;
	}

	public String toString( Tag[] tags )
	{
		assert false : "toString(Tag[]) is not supported";
		return null;
	}


	private final JCite jcite = new JCite();
	private final JavaCitelet citer = new JavaCitelet( this.jcite );

	public JCiteTaglet()
	{
		super();
		this.jcite.setPRE( true );
		this.jcite.setVerbose( "true".equalsIgnoreCase( System.getProperty( "jciteverbose" ) ) );

		addSourcePath( System.getProperty( "jcitesourcepath" ) );
	}

	private void addSourcePath( String _pathList )
	{
		if (_pathList != null) {
			final String[] sourcePaths = _pathList.split( File.pathSeparator );
			for (String sourcePath : sourcePaths) {
				this.jcite.addSourceFolder( sourcePath );
			}
		}
	}

	public String toString( Tag _tag )
	{
		try {
			String markup = _tag.text();
			final int posOfColon = markup.indexOf( ':' );
			if (posOfColon < 0) {
				markup = getSourceClassNameOf( _tag ) + ":" + markup;
			}
			else if (markup.substring( 0, posOfColon - 1 ).indexOf( '.' ) < 0) {
				markup = getSourcePackagePathOf( _tag ) + markup;
			}
			return this.citer.format( this.citer.cite( markup ) );
		}
		catch (Exception e) {
			String msg = e.getMessage();
			if (null == msg) msg = e.getClass().getName();
			printError( _tag.position(), "JCite: " + msg );
			return "<p><pre style='color:red'>" + msg + "</pre></p>";
		}
	}

	private String getSourceClassNameOf( Tag _tag )
	{
		ClassDoc classDoc;
		final Doc doc = _tag.holder();
		if (doc instanceof ClassDoc) {
			classDoc = (ClassDoc) doc;
		}
		else if (doc instanceof ProgramElementDoc) {
			ProgramElementDoc eltDoc = (ProgramElementDoc) doc;
			classDoc = eltDoc.containingClass();
		}
		else {
			printError( _tag.position(), "JCite: cannot resolve unqualified citation: " + _tag.text() );
			return "";
		}
		return classDoc.qualifiedName();
	}

	private String getSourcePackagePathOf( Tag _tag )
	{
		final String className = getSourceClassNameOf( _tag );
		final int posOfLastDot = className.lastIndexOf( '.' );
		if (posOfLastDot >= 0) {
			return className.substring( 0, posOfLastDot + 1 );
		}
		else {
			return "";
		}
	}

	private void printError( SourcePosition _pos, String _text ) {
		System.err
			.append("ERROR: ").append(_text).append(" at ").append(_pos.toString())
			.println();
		System.exit(1);
	}

}
