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

public interface Constants
{
	public static final char CR = 13;
	public static final char LF = 10;
	public static final String CRLF = String.valueOf( new char[] { CR, LF } );

	public static final String BEGIN_HIGHLIGHT = String.valueOf( (char) 1 );
	public static final String END_HIGHLIGHT = String.valueOf( (char) 2 );

	
	public static final Object INTRO = "JCite 1.9, Copyright (C) 2006 Peter Arrenbrecht\n"
			+ "JCite comes with ABSOLUTELY NO WARRANTY; for details\n"
			+ "run 'jcite -license'.\n";

	public static final Object HELP = "The command line arguments are as follows:\n" + 
			" -i <file>   defines the input file.\n" + 
			" -o <file>   defines the output file.\n" + 
			" -sp <path>  defines a source path (may be specified more than once).\n" +
			" -tt         use <TT> tags instead of <PRE> for the generated HTML.\n" +
			" -v          be verbose - lists citation markers found.\n" +
			" -q          be quiet - suppresses startup notice.\n" +
			" -license    displays the license terms.\n";

	public static final Object LICENSE = " * Copyright (c) 2006 Peter Arrenbrecht\r\n" + 
			" * All rights reserved.\r\n" + 
			" *\r\n" + 
			" * Redistribution and use in source and binary forms, with or without \r\n" + 
			" * modification, are permitted provided that the following conditions \r\n" + 
			" * are met:\r\n" + 
			" *\r\n" + 
			" * - Redistributions of source code must retain the above copyright \r\n" + 
			" *   notice, this list of conditions and the following disclaimer.\r\n" + 
			" *   \r\n" + 
			" * - Redistributions in binary form must reproduce the above copyright \r\n" + 
			" *   notice, this list of conditions and the following disclaimer in the \r\n" + 
			" *   documentation and/or other materials provided with the distribution.\r\n" + 
			" *   \r\n" + 
			" * - The names of the contributors may not be used to endorse or promote \r\n" + 
			" *   products derived from this software without specific prior written \r\n" + 
			" *   permission.\r\n" + 
			" *\r\n" + 
			" * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \r\n" + 
			" * \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT \r\n" + 
			" * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR \r\n" + 
			" * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT \r\n" + 
			" * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, \r\n" + 
			" * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT \r\n" + 
			" * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, \r\n" + 
			" * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY \r\n" + 
			" * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT \r\n" + 
			" * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE \r\n" + 
			" * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\r\n" + 
			" * \r\n" + 
			" * Contact information:\r\n" + 
			" * Peter Arrenbrecht\r\n" + 
			" * http://www.arrenbrecht.ch/jcite\r\n";

}
