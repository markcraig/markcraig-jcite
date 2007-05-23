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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

final class TripwireDatabase
{
	private final File path;
	private final boolean saveAsFolder;
	private final SortedMap<String, String> wires = new TreeMap<String, String>();
	private final SortedMap<String, String> wiresAsLoaded = new TreeMap<String, String>();

	public TripwireDatabase(File _path, boolean _isFolder)
	{
		super();
		this.path = _path;
		this.saveAsFolder = _isFolder;
	}


	public void load() throws IOException
	{
		if (this.saveAsFolder) {
			loadFromFolder();
		}
		else {
			loadFromFile();
		}
	}

	public void save() throws IOException
	{
		if (this.saveAsFolder) {
			saveToFolder();
		}
		else {
			saveToFile();
		}
	}


	private static final String ENTRY_SEP = "___§_§___";
	private static final String VALUE_SEP = "__§§__";

	private void loadFromFile() throws IOException
	{
		if (this.path.exists()) {
			final String text = Util.readStringFrom( this.path );
			final String[] entries = text.split( "\\n" + ENTRY_SEP + "\\n" );
			final String valueSep = "\\n" + VALUE_SEP + "\\n";
			for (final String entry : entries) {
				final String[] nameAndValue = entry.split( valueSep );
				this.wiresAsLoaded.put( nameAndValue[ 0 ], nameAndValue[ 1 ] );
			}
		}
	}

	private void saveToFile() throws IOException
	{
		final BufferedWriter writer = new BufferedWriter( new FileWriter( this.path ) );
		try {
			for (final Entry<String, String> wire : this.wires.entrySet()) {
				writer.append( wire.getKey() );
				writer.newLine();
				writer.append( VALUE_SEP );
				writer.newLine();
				writer.append( wire.getValue() );
				writer.newLine();
				writer.append( ENTRY_SEP );
				writer.newLine();
			}
		}
		finally {
			writer.close();
		}
	}


	private void loadFromFolder() throws IOException
	{
		final File[] files = this.path.listFiles( new TextFileFilter() );
		if (null != files) {
			for (final File file : files) {
				final String text = Util.readStringFrom( file );
				final String name = file.getName().substring( 0, file.getName().length() - 4 );
				this.wiresAsLoaded.put( name, text );
			}
		}
	}

	private void saveToFolder() throws IOException
	{
		this.path.mkdirs();
		final Iterator<Entry<String, String>> hads = this.wiresAsLoaded.entrySet().iterator();
		Entry<String, String> had = hads.hasNext() ? hads.next() : null;
		for (final Entry<String, String> have : this.wires.entrySet()) {
			final String key = have.getKey();
			final String value = have.getValue();

			while (null != had && had.getKey().compareTo( key ) < 0) {
				fileForWire( had.getKey() ).delete();
				had = hads.hasNext() ? hads.next() : null;
			}

			if (null != had && had.getKey().equals( key )) {
				if (!had.getValue().equals( value )) {
					Util.writeStringTo( value, fileForWire( key ) );
				}
				had = hads.hasNext() ? hads.next() : null;
			}
			else {
				Util.writeStringTo( value, fileForWire( key ) );
			}

		}

		while (null != had) {
			new File( this.path, had.getKey() + ".txt" ).delete();
			had = hads.hasNext() ? hads.next() : null;
		}
	}

	private File fileForWire( final String _name )
	{
		return new File( this.path, _name + ".txt" );
	}


	private static final char[] ILLEGAL_CHARS = "\\/:,<>'\"\t\n\f\r".toCharArray();

	public static String sanitizeName( String _name )
	{
		String result = _name;
		for (char c : ILLEGAL_CHARS) {
			result = result.replace( c, ' ' );
		}
		return result.trim();
	}

	public boolean check( String _name, String _value )
	{
		final String found = this.wiresAsLoaded.get( _name );
		if (null == found || found.equals( _value )) {
			this.wires.put( _name, _value );
			return true;
		}
		else {
			this.wires.put( _name, found );
			return false;
		}
	}

	public void update( String _name, String _value )
	{
		this.wires.put( _name, _value );
	}


	private static final class TextFileFilter implements FilenameFilter
	{
		private static final Pattern NAME_FILTER = Pattern.compile( ".*\\.txt", Pattern.CASE_INSENSITIVE );

		@Override
		public boolean accept( File _dir, String _name )
		{
			return NAME_FILTER.matcher( _name ).matches();
		}

	}


	@Override
	public String toString()
	{
		return (this.saveAsFolder ? "folder " : "file ") + this.path.getPath();
	}

}
