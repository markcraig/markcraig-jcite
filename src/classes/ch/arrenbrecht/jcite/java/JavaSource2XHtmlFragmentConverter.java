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
package ch.arrenbrecht.jcite.java;

import java.io.BufferedWriter;
import java.io.IOException;

import de.java2html.converter.JavaSourceConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceType;
import de.java2html.util.HTMLTools;


/**
 * Algorithm and stuff for converting a de.java2html.javasource.JavaSource object to an XHTML
 * fragment representation.
 * 
 * @author Peter Arrenbrecht, based on work by <a href="mailto:Jan.Tisje@gmx.de">Jan Tisje</a>
 * @version 1.0
 */
public class JavaSource2XHtmlFragmentConverter extends JavaSourceConverter
{
	protected final static String TAG_START = "<span class=\"j-";
	protected final static String TAG_END = "\">";
	protected final static String TAG_CLOSE = "</span>";
	protected final static String[] SOURCE_TYPE_NAMES = new String[] { "bkg", "lin", "blk", "cmt", "key", "str", "chr",
			"num", "sym", "typ", "", "jdoc-key", "jdoc-html", "jdoc-link", "jdoc", "undef" };

	protected boolean pre = true;
	protected String lineEnd = "";
	protected String space = " ";
	protected String sourceCode;
	protected JavaSourceType[] sourceTypes;


	public void setPRE( boolean _pre )
	{
		this.pre = _pre;
		if (_pre) {
			this.lineEnd = "";
			this.space = " "; // normal space
		}
		else {
			this.lineEnd = "<br />";
			this.space = "&#xA0;";
		}
	}


	public JavaSource2XHtmlFragmentConverter()
	{
		super();
	}


	public JavaSource2XHtmlFragmentConverter(JavaSource source)
	{
		super( source );
	}


	@Override
	public String getDocumentHeader()
	{
		return "";
	}


	@Override
	public String getDocumentFooter()
	{
		return "";
	}


	@Override
	public String getBlockSeparator()
	{
		return "";
	}


	@Override
	public String getDefaultFileExtension()
	{
		return "xhtml";
	}


	@Override
	public void convert( BufferedWriter writer ) throws IOException
	{
		if (this.source == null) {
			throw new IllegalStateException( "Trying to write out converted code without having source set." );
		}

		this.sourceCode = this.source.getCode();
		this.sourceTypes = this.source.getClassification();

		if (this.pre) writer.write( "<pre class=\"java\">" );
		else writer.write( "<tt class=\"java\">" );

		int start = 0;
		int end = 0;

		final int length = this.sourceTypes.length;
		while (start < length) {

			while (end < length - 1
					&& (this.sourceTypes[ end + 1 ] == this.sourceTypes[ start ] || this.sourceTypes[ end + 1 ] == JavaSourceType.BACKGROUND)) {
				++end;
			}
			toXml( start, end, writer );
			start = end + 1;
			end = start;
		}

		if (this.pre) writer.write( "</pre>" );
		else writer.write( "</tt>" );
	}


	protected void toXml( int start, int end, BufferedWriter writer ) throws IOException
	{
		final String sourceTypeName = SOURCE_TYPE_NAMES[ this.sourceTypes[ start ].getID() ];
		if (!sourceTypeName.equals( "" )) writer.write( TAG_START + sourceTypeName + TAG_END );

		String t = HTMLTools.encode( this.sourceCode, start, end + 1, "\r\n " );

		for (int i = 0; i < t.length(); ++i) {
			char ch = t.charAt( i );
			if (ch == ' ') if ((i < t.length() - 1) && (t.charAt( i + 1 ) == ' ')) writer.write( this.space );
			else writer.write( " " );
			else if (ch == '\n') writer.write( this.lineEnd + "\n" );
			else if (ch != '\r') writer.write( ch );
		}

		if (!sourceTypeName.equals( "" )) writer.write( TAG_CLOSE );
	}


}