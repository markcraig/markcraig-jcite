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

/**
 * You can extend JCite by implementing your own {@code JCitelet}.
 * @see JCitePlainExtensionAnnotatedTest
 */
public abstract class JCitelet
{
	private final JCite jcite;


	public JCitelet( JCite _jcite )
	{
		super();
		this.jcite = _jcite;
	}


	private static final String PRE_START = "<pre>";
	private static final String PRE_END = "</pre>";


	public String process( String _source ) throws JCiteError
	{
		return processInlines( processCitations( _source ) );
	}

	protected String processCitations( String _source ) throws JCiteError
	{
		return processElements( _source, referenceStartTag(), referenceEndTag(), new ElementVisitor()
		{

			public Insertion insertionFor( String _markup ) throws JCiteError, IOException
			{
				return cite( _markup );
			}

			public String formatInsertion( Insertion _insertion, String _beginTag, String _endTag ) throws JCiteError, IOException
			{
				return format( _insertion, _beginTag, _endTag );
			}

		} );
	}

	protected String processInlines( String _source ) throws JCiteError
	{
		return processElements( _source, inlineStartTag(), inlineEndTag(), new ElementVisitor()
		{

			public Insertion insertionFor( String _citation )
			{
				return inline( _citation );
			}

			public String formatInsertion( Insertion _insertion, String _beginTag, String _endTag ) throws JCiteError, IOException
			{
				return format( _insertion, _beginTag, _endTag );
			}

		} );
	}


	protected final String processElements( String _source, String _markupStartTag, String _markupEndTag,
			ElementVisitor _visitor ) throws JCiteError
	{
		final int markupStartTagLength = _markupStartTag.length();
		int processedUpto = 0;
		int beginMarkup = _source.indexOf( _markupStartTag, processedUpto );
		if (beginMarkup > 0) {
			final StringBuilder result = new StringBuilder( _source.length() );
			do {
				final int endMarkup = _source.indexOf( _markupEndTag, beginMarkup );
				if (endMarkup < 0)
					throw new UnclosedMarkupError( "Can't find " + _markupEndTag + " after " + beginMarkup );
				final String markup = _source.substring( beginMarkup + markupStartTagLength, endMarkup );
				int beginDeletion = Util.scanBackTo( _source, '<', beginMarkup );
				int endDeletion = Util.scanForwardTo( _source, '>', endMarkup );
				if (beginDeletion >= PRE_START.length()
						&& _source.substring( beginDeletion - PRE_START.length(), beginDeletion ).equals( PRE_START )
						&& _source.substring( endDeletion + 1, endDeletion + 1 + PRE_END.length() ).equals( PRE_END )) {
					beginDeletion -= PRE_START.length();
					endDeletion += PRE_END.length();
				}
				final String beginTag = _source.substring(beginDeletion, beginMarkup);
				final String endTag = _source.substring(endMarkup + 1, endDeletion + 1);
				result.append( _source.substring( processedUpto, beginDeletion ) );

				try {
					final Insertion insertion = _visitor.insertionFor( markup );
					final String formatted = _visitor.formatInsertion( insertion, beginTag, endTag );
					result.append( formatted );
					this.jcite.checkTripwires( markup, insertion.text(), beginMarkup );
					this.jcite.logCitation( markup, beginMarkup );
				}
				catch (JCiteError e) {
					this.jcite.logCitationError( e, markup, beginMarkup );
					result.append( _source.substring( beginDeletion, endDeletion + 1 ) );
				}
				catch (IOException e) {
					this.jcite.logCitationError( e, markup, beginMarkup );
					result.append( _source.substring( beginDeletion, endDeletion + 1 ) );
				}

				processedUpto = endDeletion + 1;
			} while ((beginMarkup = _source.indexOf( _markupStartTag, processedUpto )) > 0);
			result.append( _source.substring( processedUpto ) );
			return result.toString();
		}
		else {
			return _source;
		}

	}

	public static abstract class Insertion
	{
		private final String text;

		public Insertion( String _text )
		{
			this.text = _text;
		}

		public String text()
		{
			return text;
		}
	}

	public static final class Inlined extends Insertion
	{
		public Inlined(String _source)
		{
			super(_source);
		}
	}

	public static class Citation extends Insertion
	{
		public Citation(String _text)
		{
			super(_text);
		}
	}

	public static class AnnotatedCitation extends Citation
	{
		private final String annotation;

		public AnnotatedCitation(String _text, String _annotation)
		{
			super(_text);
			this.annotation = _annotation;
		}

		public String annotation() {
			return annotation;
		}
	}

	protected interface ElementVisitor
	{
		Insertion insertionFor( String _markup ) throws JCiteError, IOException;
		String formatInsertion( Insertion _insertion, String _beginTag, String _endTag ) throws JCiteError, IOException;
	}


	protected boolean argHandled( @SuppressWarnings("unused") String _arg )
	{
		return false;
	}


	protected abstract String referencePrefix();

	protected abstract Citation cite( String _reference ) throws JCiteError, IOException;

	protected Inlined inline(String _text)
	{
		return new Inlined( _text );
	}

	@SuppressWarnings("unused") //
	protected String format( Insertion _insertion, String _beginTag, String _endTag ) throws JCiteError, IOException
	{
		return format( _insertion );
	}

	protected abstract String format( Insertion _insertion ) throws JCiteError, IOException;


	protected String referenceStartTag()
	{
		return "[" + referencePrefix() + ":";
	}

	protected String referenceEndTag()
	{
		return "]";
	}

	protected String inlineStartTag()
	{
		return "<pre jcite=\"" + referencePrefix() + "\">";
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

	protected String findSourcePath( String _relativeFilePath ) throws FileNotFoundError
	{
		for (String sourcePath : sourceFolders()) {
			final String fullPath = sourcePath + '/' + _relativeFilePath;
			final File file = new File( fullPath );
			if (file.exists()) return fullPath;
		}
		throw new FileNotFoundError( "File " + _relativeFilePath + " not found." );
	}

	protected String stripProjectPathFrom( String _path )
	{
		final String p = this.jcite.getProjectPath();
		if (p != null && p != "" && _path.startsWith( p )) {
			final String rel = _path.substring( p.length() );
			final String sep = File.separator;
			if (rel.startsWith( sep )) {
				return rel.substring( sep.length() );
			}
			return rel;
		}
		return _path;
	}

}
