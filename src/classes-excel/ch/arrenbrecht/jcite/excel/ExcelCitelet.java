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
package ch.arrenbrecht.jcite.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ch.arrenbrecht.describable.DescriptionBuilder;
import ch.arrenbrecht.jcite.JCite;
import ch.arrenbrecht.jcite.JCiteError;
import ch.arrenbrecht.jcite.JCitelet;

import jxl.Cell;
import jxl.CellType;
import jxl.FormulaCell;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.formula.FormulaException;
import jxl.format.Alignment;
import jxl.format.CellFormat;
import jxl.format.Font;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;

public class ExcelCitelet extends JCitelet
{

	public ExcelCitelet(JCite _jcite)
	{
		super( _jcite );
	}


	@Override
	protected String markupTag()
	{
		return "xc";
	}


	@Override
	protected String insertionFor( String _markup ) throws JCiteError, IOException
	{
		String sourceFileName = _markup;
		String[] ranges = new String[ 0 ];
		final int posOfColon = _markup.indexOf( ':' );
		if (posOfColon >= 0) {
			sourceFileName = _markup.substring( 0, posOfColon );
			ranges = _markup.substring( posOfColon + 1 ).split( "," );
		}

		final File sourceFile = findSourceFile( sourceFileName );
		try {

			final WorkbookSettings xlsSettings = new WorkbookSettings();
			xlsSettings.setLocale( Locale.ENGLISH );
			xlsSettings.setExcelDisplayLanguage( "EN" );
			xlsSettings.setExcelRegionalSettings( "EN" );

			final Workbook workbook = jxl.Workbook.getWorkbook( sourceFile, xlsSettings );
			final DescriptionBuilder b = new DescriptionBuilder();

			new WorkbookFormatter( workbook, workbook.getSheet( 0 ), ranges ).convertSheet( b );

			return b.toString();
		}
		catch (BiffException e) {
			throw new JCiteError( e );
		}
	}


	private final class WorkbookFormatter
	{
		private final Workbook workbook;
		private final Sheet sheet;
		private final Collection<Range> scope = new ArrayList<Range>();
		private final int firstColumnInScope;
		private final int lastColumnInScope;
		private final Map<Cell, String> namedCells = new HashMap<Cell, String>();
		private final Map<Long, String> rangedCellColors = new HashMap<Long, String>();
		private final Map<String, Range[]> namedRanges = new HashMap<String, Range[]>();
		private final Map<String, String> namedRangeColors = new HashMap<String, String>();


		public WorkbookFormatter(Workbook _workbook, Sheet _sheet, String[] _rangeNames) throws JCiteError
		{
			super();
			this.workbook = _workbook;
			this.sheet = _sheet;
			if (_rangeNames != null && _rangeNames.length > 0) {
				setupScope( _rangeNames );
				this.firstColumnInScope = getFirstColumnInScope();
				this.lastColumnInScope = getLastColumnInScope();
			}
			else {
				this.firstColumnInScope = 0;
				this.lastColumnInScope = this.sheet.getColumns() - 1;
			}
			setupNames( _rangeNames );
		}


		private void setupScope( String[] _rangeNames ) throws JCiteError
		{
			for (final String rangeSpec : _rangeNames) {
				final String rangeName = (rangeSpec.endsWith( "+" )) ? rangeSpec.substring( 0, rangeSpec.length() - 1 )
						: rangeSpec;
				final Range[] rangeDef = this.workbook.findByName( rangeName );
				if (rangeDef == null) {
					throw new JCiteError( "Range " + rangeName + " not found in workbook." );
				}
				else {
					for (final Range range : rangeDef) {
						if (null != range) this.scope.add( range );
					}
				}
			}
		}

		private int getFirstColumnInScope()
		{
			int min = Integer.MAX_VALUE;
			for (final Range r : this.scope) {
				final int c = r.getTopLeft().getColumn();
				if (c < min) min = c;
			}
			return min;
		}

		private int getLastColumnInScope()
		{
			int max = 0;
			for (final Range r : this.scope) {
				final int c = r.getBottomRight().getColumn();
				if (c > max) max = c;
			}
			return max;
		}

		private boolean isInScope( int _row )
		{
			if (this.scope.size() == 0) return true;
			for (final Range r : this.scope) {
				if (r.getTopLeft().getRow() <= _row && _row <= r.getBottomRight().getRow()) return true;
			}
			return false;
		}

		private boolean isInScope( Cell _cell )
		{
			if (this.scope.size() == 0) return true;
			for (final Range r : this.scope) {
				if (contains( r, _cell )) return true;
			}
			return false;
		}

		private boolean isInScope( Range _range )
		{
			if (this.scope.size() == 0) return true;
			for (final Range r : this.scope) {
				if (intersects( r, _range )) return true;
			}
			return false;
		}

		private boolean intersects( Range a, Range b )
		{
			return lessOrEqual( b.getTopLeft(), a.getBottomRight() ) && lessOrEqual( a.getTopLeft(), b.getBottomRight() );
		}

		private boolean contains( Range r, Cell c )
		{
			return lessOrEqual( r.getTopLeft(), c ) && lessOrEqual( c, r.getBottomRight() );
		}

		private boolean lessOrEqual( Cell a, Cell b )
		{
			return a.getRow() <= b.getRow() && a.getColumn() <= b.getColumn();
		}


		private void setupNames( String[] _except )
		{
			final String[] rangeNames = this.workbook.getRangeNames();
			Arrays.sort( rangeNames );
			int nextRangeNo = 1;
			for (String rangeName : rangeNames) {
				if (!rangeName.equals( "ERROR" )) { // this is how JExcelAPI flags naming errors
					if (!contains( _except, rangeName )) {
						final Range[] rangeDef = this.workbook.findByName( rangeName );
						if (rangeDef.length > 1) {
							setupNamedRange( rangeName, rangeDef, nextRangeNo++ );
						}
						else if (rangeDef.length == 1) {
							final Range range = rangeDef[ 0 ];
							if (range.getTopLeft().equals( range.getBottomRight() )) {
								setupNamedCell( rangeName, range.getTopLeft() );
							}
							else {
								setupNamedRange( rangeName, rangeDef, nextRangeNo++ );
							}
						}
					}
				}
			}
		}

		private boolean contains( String[] _except, String _rangeName )
		{
			for (String r : _except) {
				if (r.equals( _rangeName )) return true;
			}
			return false;
		}

		private void setupNamedCell( String _name, Cell _cell )
		{
			if (isInScope( _cell )) {
				this.namedCells.put( _cell, _name );
			}
		}

		private void setupNamedRange( String _rangeName, Range[] _rangeDef, int _rangeNumber )
		{
			final String rangeColor = "xl-r" + _rangeNumber;
			boolean inScope = false;
			for (Range r : _rangeDef) {
				if (isInScope( r )) {
					inScope = true;
					for (int iRow = r.getTopLeft().getRow(); iRow <= r.getBottomRight().getRow(); iRow++) {
						for (int iCol = r.getTopLeft().getColumn(); iCol <= r.getBottomRight().getColumn(); iCol++) {
							this.rangedCellColors.put( cellToLong( iCol, iRow ), rangeColor );
						}
					}
				}
			}
			if (inScope) {
				this.namedRanges.put( _rangeName, _rangeDef );
				this.namedRangeColors.put( _rangeName, rangeColor );
			}
		}

		private Long cellToLong( int _col, int _row )
		{
			return Long.valueOf( _row * 1024L + _col );
		}

		private void convertSheet( DescriptionBuilder _b )
		{
			final DescriptionBuilder b = _b;

			b.appendLine( "<table class=\"xl\">" );
			b.indent();

			b.appendLine( "<thead>" );
			b.indent();
			b.appendLine( "<tr>" );
			b.indent();
			b.appendLine( "<td/>" );
			for (int iCol = this.firstColumnInScope; iCol <= this.lastColumnInScope; iCol++) {
				b.append( "<td>" );
				b.append( (char) ('A' + iCol) );
				b.appendLine( "</td>" );
			}
			b.outdent();
			b.appendLine( "</tr>" );
			b.outdent();
			b.appendLine( "</thead>" );

			b.appendLine( "<tbody>" );
			b.indent();
			for (int iRow = 0; iRow < this.sheet.getRows(); iRow++) {
				final Cell[] row = this.sheet.getRow( iRow );
				if (isInScope( iRow )) {
					b.appendLine( "<tr>" );
					b.indent();

					b.append( "<td class=\"xl-row\">" );
					b.append( iRow + 1 );
					b.appendLine( "</td>" );

					for (int iCol = this.firstColumnInScope; iCol <= this.lastColumnInScope; iCol++) {
						String clazz = "";
						String attribs = "";
						String contents = "";
						String annotations = "";

						final Cell cell = (iCol < row.length) ? row[ iCol ] : null;
						if (cell != null && isInScope( cell )) {
							contents = cell.getContents();

							final CellType cellType = cell.getType();
							final CellFormat cellFormat = cell.getCellFormat();
							if (cellFormat != null) {
								final Alignment cellAlignment = cellFormat.getAlignment();
								if (cellAlignment == Alignment.LEFT) attribs += " style=\"text-align: left\"";
								if (cellAlignment == Alignment.RIGHT) attribs += " style=\"text-align: right\"";
								if (cellAlignment == Alignment.CENTRE) attribs += " style=\"text-align: center\"";
								if (cellAlignment == Alignment.JUSTIFY) attribs += " style=\"text-align: justify\"";
								Font font = cellFormat.getFont();
								if (font != null) {
									if (font.getUnderlineStyle() == UnderlineStyle.SINGLE) {
										contents = "<span style=\"text-decoration: underline;\">" + contents + "</span>";
									}
									if (font.getBoldWeight() > 400) {
										contents = "<b>" + contents + "</b>";
									}
								}
							}
							if (cellType == CellType.NUMBER || cellType == CellType.NUMBER_FORMULA) {
								clazz += " xl-num";
							}
							else if (cellType == CellType.DATE || cellType == CellType.DATE_FORMULA) {
								clazz += " xl-date";
							}

							if (cell instanceof FormulaCell) {
								final FormulaCell formulaCell = (FormulaCell) cell;
								try {
									annotations += "<br/><span class=\"xl-exp\">=" + formulaCell.getFormula() + "</span>";
								}
								catch (FormulaException e) {
									annotations += "<br/><span class=\"xl-exp\">FORMULA ERROR: " + e.getMessage() + "</span>";
								}
							}

							final String cellName = this.namedCells.get( cell );
							if (cellName != null) {
								annotations += "<br/><span class=\"xl-name\">(" + cellName + ")</span>";
							}

						}

						final String cellColor = this.rangedCellColors.get( cellToLong( iCol, iRow ) );
						if (cellColor != null) {
							clazz += " " + cellColor;
						}

						if (!clazz.equals( "" )) {
							attribs += " class=\"" + clazz.substring( 1 ) + "\"";
						}

						b.append( "<td" );
						b.append( attribs );
						b.append( ">" );
						b.append( contents );
						b.append( annotations );
						b.appendLine( "</td>" );
					}

					b.outdent();
					b.appendLine( "</tr>" );
				}
			}
			b.outdent();
			b.appendLine( "</tbody>" );

			b.appendLine( "</table>" );

			for (String rangeName : this.namedRanges.keySet()) {
				final String rangeColor = this.namedRangeColors.get( rangeName );
				final Range[] rangeDef = this.namedRanges.get( rangeName );
				b.append( "<br/><span class=\"" );
				b.append( rangeColor );
				b.append( "\">" );
				boolean first = true;
				for (Range r : rangeDef) {
					if (!first) {
						b.append( ' ' );
						first = false;
					}
					b.append( (char) ('A' + r.getTopLeft().getColumn()) );
					b.append( r.getTopLeft().getRow() + 1 );
					b.append( ':' );
					b.append( (char) ('A' + r.getBottomRight().getColumn()) );
					b.append( r.getBottomRight().getRow() + 1 );
				}
				b.append( "</span> <span class=\"xl-name\">(" );
				b.append( rangeName );
				b.append( ")</span>" );
			}

		}

	}

}
