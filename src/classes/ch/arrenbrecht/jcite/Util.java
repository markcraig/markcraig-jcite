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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.regex.Pattern;


/**
 * Utility methods for JCite.
 *
 * @author peo
 */
public class Util
{


	public static int scanForwardTo( String _in, char _scanTo, int _startingAt )
	{
		int result = _startingAt;
		while (result < _in.length() && _in.charAt( result ) != _scanTo)
			result++;
		return result;
	}


	public static int scanBackTo( String _in, char _scanTo, int _startingAt )
	{
		int result = _startingAt;
		while (result >= 0 && _in.charAt( result ) != _scanTo)
			result--;
		return result;
	}


	public static String readStringFrom( File _source ) throws IOException
	{
		StringBuffer sb = new StringBuffer( 1024 );
		BufferedReader reader = new BufferedReader( new FileReader( _source ) );
		try {
			char[] chars = new char[ 1024 ];
			int red;
			while ((red = reader.read( chars )) > -1) {
				sb.append( chars, 0, red );
			}
		}
		finally {
			reader.close();
		}
		return sb.toString();
	}


	public static void writeStringTo( String _value, File _target ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( _target ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}


	public static int execAndPipeOutputToSystem( String... _args ) throws IOException, InterruptedException
	{
		final ProcessBuilder pb = new ProcessBuilder( _args );
		final Process p = pb.start();
		final int result = p.waitFor();
		printStream( p.getInputStream(), System.out );
		printStream( p.getErrorStream(), System.err );
		return result;
	}

	public static void printStream( InputStream _from, PrintStream _printTo ) throws IOException
	{
		final Reader in = new BufferedReader( new InputStreamReader( new BufferedInputStream( _from ) ) );
		while (in.ready())
			_printTo.write( in.read() );
	}


	public static void copy( File _src, File _tgt ) throws IOException
	{
		InputStream in = new BufferedInputStream( new FileInputStream( _src ) );
		try {
			OutputStream out = new BufferedOutputStream( new FileOutputStream( _tgt ) );
			try {
				while (in.available() > 0) {
					out.write( in.read() );
				}
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}
	}


	static interface FileVisitor
	{
		void visit( File _inputFile, File _outputFile ) throws IOException, JCiteError;
	}


	static void iterateFiles( File _inputFolder, String _pattern, File _outputFolder, boolean _recurse,
			FileVisitor _visitor ) throws IOException, JCiteError
	{
		final StringBuilder src = new StringBuilder();
		for (int i = 0; i < _pattern.length(); i++) {
			char c = _pattern.charAt( i );
			switch (c) {
				case '*':
					src.append( ".*" );
					break;
				case '?':
					src.append( "." );
					break;
				default:
					src.append( "\\x" );
					src.append( Integer.toHexString( c ) );
			}
		}
		final Pattern pattern = Pattern.compile( src.toString() );

		final File[] inputFiles = _inputFolder.listFiles( new FilenameFilter()
		{

			public boolean accept( File _dir, String _name )
			{
				return pattern.matcher( _name ).matches();
			}

		} );

		for (File inputFile : inputFiles) {
			if (inputFile.isFile()) {
				final File outputFile = new File( _outputFolder, inputFile.getName() );
				_visitor.visit( inputFile, outputFile );
			}
		}

		if (_recurse) {
			for (File dirOrFile : _inputFolder.listFiles()) {
				if (dirOrFile.isDirectory() && !dirOrFile.getName().equals( "." ) && !dirOrFile.getName().equals( ".." )) {
					final File subInputFolder = dirOrFile;
					final File subOutputFolder = new File( _outputFolder, subInputFolder.getName() );
					iterateFiles( subInputFolder, _pattern, subOutputFolder, _recurse, _visitor );
				}
			}
		}
	}


	static public final String escapeHTMLinPRE(String s) {
		final StringBuilder sb = new StringBuilder();
		final int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case 'à':
				sb.append("&agrave;");
				break;
			case 'À':
				sb.append("&Agrave;");
				break;
			case 'â':
				sb.append("&acirc;");
				break;
			case 'Â':
				sb.append("&Acirc;");
				break;
			case 'ä':
				sb.append("&auml;");
				break;
			case 'Ä':
				sb.append("&Auml;");
				break;
			case 'å':
				sb.append("&aring;");
				break;
			case 'Å':
				sb.append("&Aring;");
				break;
			case 'æ':
				sb.append("&aelig;");
				break;
			case 'Æ':
				sb.append("&AElig;");
				break;
			case 'ç':
				sb.append("&ccedil;");
				break;
			case 'Ç':
				sb.append("&Ccedil;");
				break;
			case 'é':
				sb.append("&eacute;");
				break;
			case 'É':
				sb.append("&Eacute;");
				break;
			case 'è':
				sb.append("&egrave;");
				break;
			case 'È':
				sb.append("&Egrave;");
				break;
			case 'ê':
				sb.append("&ecirc;");
				break;
			case 'Ê':
				sb.append("&Ecirc;");
				break;
			case 'ë':
				sb.append("&euml;");
				break;
			case 'Ë':
				sb.append("&Euml;");
				break;
			case 'ï':
				sb.append("&iuml;");
				break;
			case 'Ï':
				sb.append("&Iuml;");
				break;
			case 'ô':
				sb.append("&ocirc;");
				break;
			case 'Ô':
				sb.append("&Ocirc;");
				break;
			case 'ö':
				sb.append("&ouml;");
				break;
			case 'Ö':
				sb.append("&Ouml;");
				break;
			case 'ø':
				sb.append("&oslash;");
				break;
			case 'Ø':
				sb.append("&Oslash;");
				break;
			case 'ß':
				sb.append("&szlig;");
				break;
			case 'ù':
				sb.append("&ugrave;");
				break;
			case 'Ù':
				sb.append("&Ugrave;");
				break;
			case 'û':
				sb.append("&ucirc;");
				break;
			case 'Û':
				sb.append("&Ucirc;");
				break;
			case 'ü':
				sb.append("&uuml;");
				break;
			case 'Ü':
				sb.append("&Uuml;");
				break;
			case '®':
				sb.append("&reg;");
				break;
			case '©':
				sb.append("&copy;");
				break;
			case '€':
				sb.append("&euro;");
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

}
