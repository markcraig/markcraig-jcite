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
import java.util.List;


public abstract class JCitelet
{
	private final JCite jcite;


	public JCitelet(JCite _jcite)
	{
		super();
		this.jcite = _jcite;
	}


	private static final String PRE_START = "<pre>";
	private static final String PRE_END = "</pre>";


	public String process( String _source ) throws JCiteError, IOException
	{
		return processInlines( processCitations( _source ) );
	}

	protected String processCitations( String _source ) throws JCiteError, IOException
	{
		return processElements( _source, markupStartTag(), markupEndTag(), new ElementVisitor()
		{

			public String insertionFor( String _markup ) throws JCiteError, IOException
			{
				return citationFor( _markup );
			}

		} );
	}

	protected String processInlines( String _source ) throws JCiteError, IOException
	{
		return processElements( _source, inlineStartTag(), inlineEndTag(), new ElementVisitor()
		{

			public String insertionFor( String _citation ) throws JCiteError, IOException
			{
				return formattingFor( "", _citation );
			}

		} );
	}


	protected final String processElements( String _source, String _markupStartTag, String _markupEndTag,
			ElementVisitor _visitor ) throws JCiteError, IOException
	{
		final int markupStartTagLength = _markupStartTag.length();
		int processedUpto = 0;
		int beginMarkup = _source.indexOf( _markupStartTag, processedUpto );
		if (beginMarkup > 0) {
			final StringBuilder result = new StringBuilder( _source.length() );
			do {
				final int endMarkup = _source.indexOf( _markupEndTag, beginMarkup );
				if (endMarkup < 0) throw new UnclosedMarkupError();
				final String markup = _source.substring( beginMarkup + markupStartTagLength, endMarkup );
				final String insertion = _visitor.insertionFor( markup );
				int beginDeletion = Util.scanBackTo( _source, '<', beginMarkup );
				int endDeletion = Util.scanForwardTo( _source, '>', endMarkup );
				if (beginDeletion >= PRE_START.length()
						&& _source.substring( beginDeletion - PRE_START.length(), beginDeletion ).equals( PRE_START )
						&& _source.substring( endDeletion + 1, endDeletion + 1 + PRE_END.length() ).equals( PRE_END )) {
					beginDeletion -= PRE_START.length();
					endDeletion += PRE_END.length();
				}

				result.append( _source.substring( processedUpto, beginDeletion ) );
				result.append( insertion );

				processedUpto = endDeletion + 1;
			} while ((beginMarkup = _source.indexOf( _markupStartTag, processedUpto )) > 0);
			result.append( _source.substring( processedUpto ) );
			return result.toString();
		}
		else {
			return _source;
		}

	}

	protected interface ElementVisitor
	{
		String insertionFor( String _markup ) throws JCiteError, IOException;
	}


	protected boolean argHandled( String _arg )
	{
		return false;
	}


	protected abstract String markupTag();

	protected abstract String citationFor( String _markup ) throws JCiteError, IOException;

	protected abstract String formattingFor( String _markup, String _cited ) throws JCiteError, IOException;


	protected String markupStartTag()
	{
		return "[" + markupTag() + ":";
	}

	protected String markupEndTag()
	{
		return "]";
	}

	protected String inlineStartTag()
	{
		return "<pre jcite=\"" + markupTag() + "\">";
	}

	protected String inlineEndTag()
	{
		return "</pre>";
	}


	protected final boolean isVerbose()
	{
		return this.jcite.isVerbose();
	}

	protected final List<String> sourceFolders()
	{
		return this.jcite.sourceFolders();
	}

	protected File findSourceFile( String _relativeFilePath ) throws FileNotFoundError
	{
		for (String sourcePath : sourceFolders()) {
			final String fullPath = sourcePath + '/' + _relativeFilePath;
			final File file = new File( fullPath );
			if (file.exists()) return file;
		}
		throw new FileNotFoundError( "File " + _relativeFilePath + " not found." );
	}

}
