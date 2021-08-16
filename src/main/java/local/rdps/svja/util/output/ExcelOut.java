package local.rdps.svja.util.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTBorder;
import org.xlsx4j.sml.CTBorders;
import org.xlsx4j.sml.CTCellAlignment;
import org.xlsx4j.sml.CTCellStyle;
import org.xlsx4j.sml.CTCellStyleXfs;
import org.xlsx4j.sml.CTCellStyles;
import org.xlsx4j.sml.CTCellXfs;
import org.xlsx4j.sml.CTColor;
import org.xlsx4j.sml.CTFill;
import org.xlsx4j.sml.CTFills;
import org.xlsx4j.sml.CTFont;
import org.xlsx4j.sml.CTFontFamily;
import org.xlsx4j.sml.CTFontName;
import org.xlsx4j.sml.CTFontScheme;
import org.xlsx4j.sml.CTFontSize;
import org.xlsx4j.sml.CTFonts;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.CTUnderlineProperty;
import org.xlsx4j.sml.CTVerticalAlignFontProperty;
import org.xlsx4j.sml.CTXf;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Col;
import org.xlsx4j.sml.Cols;
import org.xlsx4j.sml.ObjectFactory;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.STFontScheme;
import org.xlsx4j.sml.STHorizontalAlignment;
import org.xlsx4j.sml.STPatternType;
import org.xlsx4j.sml.STVerticalAlignment;
import org.xlsx4j.sml.SheetData;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.construct.OutputMechanism;
import local.rdps.svja.util.ConversionUtils;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.FileVo;

/**
 * <p>
 * A class that makes Excel exports.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ExcelOut implements OutputMechanism {
	private static final class XLSX {
		private static final Logger logger = LogManager.getLogger();
		private static final ObjectFactory smlObjectFactory = Context.getsmlObjectFactory();
		private SpreadsheetMLPackage fSpreadsheetMlPackage;
		private List<WorksheetPart> fWorksheetParts;
		private Styles stylesPart;

		/**
		 * <p>
		 * Returns the alphabetic column ID corresponding to the given column number.
		 * </p>
		 *
		 * @param columnNumber
		 *            A column number
		 * @return The alphabetic column ID corresponding to the given column number
		 */
		private static @NotNull String calcColRef(final long columnNumber) {
			final StringBuilder ce = new StringBuilder(16);
			long m = columnNumber;

			while (m > 26L) {
				final long e;
				if (m > 26L)
					if ((m % 26L) != 0L) {
						final long t = m;
						m /= 26L;
						e = t % 26L;
					} else {
						e = 26L;
						m = (m / 26L) - 1L;
					}
				else {
					e = m;
					m = 0L;
				}

				if (e > 0L) {
					ce.insert(0, (char) (e + 64L));
				}
			}

			if (m > 0L) {
				ce.insert(0, (char) (m + 64L));
			}

			return ce.toString();
		}

		private static CTStylesheet createBlankStylesheet() {
			final CTStylesheet ss = XLSX.smlObjectFactory.createCTStylesheet();

			// setup the fonts
			ss.setFonts(XLSX.smlObjectFactory.createCTFonts());
			ss.getFonts().setCount(Long.valueOf(0));
			// setup the fills
			ss.setFills(XLSX.smlObjectFactory.createCTFills());
			ss.getFills().setCount(Long.valueOf(0));
			// setup the borders
			ss.setBorders(XLSX.smlObjectFactory.createCTBorders());
			ss.getBorders().setCount(Long.valueOf(0));
			// setup the cell styleXfs
			ss.setCellStyleXfs(XLSX.smlObjectFactory.createCTCellStyleXfs());
			ss.getCellStyleXfs().setCount(Long.valueOf(0));
			// setup the cell Xfs
			ss.setCellXfs(XLSX.smlObjectFactory.createCTCellXfs());
			ss.getCellXfs().setCount(Long.valueOf(0));
			// setup the cell styles
			ss.setCellStyles(XLSX.smlObjectFactory.createCTCellStyles());
			ss.getCellStyles().setCount(Long.valueOf(0));
			// setup dxfs
			ss.setDxfs(XLSX.smlObjectFactory.createCTDxfs());
			ss.getDxfs().setCount(Long.valueOf(0));

			return ss;
		}

		/**
		 * <p>
		 * Calculates the alphanumeric column-row cell ID (e.g. A1, B3, AA78) corresponding to the given column, row
		 * coordinate.
		 * </p>
		 *
		 * @param columnNumber
		 *            A column number
		 * @param rowNumber
		 *            A row number
		 * @return The alphanumeric column-row cell ID (e.g. A1, B3, AA78) corresponding to the given column, row
		 *         coordinate
		 */
		private static @NotNull String getCellId(final long columnNumber, final long rowNumber) {
			return XLSX.calcColRef(columnNumber) + rowNumber;
		}

		/**
		 * <p>
		 * Create border style.
		 * </p>
		 *
		 * @return The border style
		 */
		public static CTBorder createBorderStyle() {
			final CTBorder border = XLSX.smlObjectFactory.createCTBorder();
			border.setTop(XLSX.smlObjectFactory.createCTBorderPr());
			border.setLeft(XLSX.smlObjectFactory.createCTBorderPr());
			border.setRight(XLSX.smlObjectFactory.createCTBorderPr());
			border.setBottom(XLSX.smlObjectFactory.createCTBorderPr());
			return border;
		}

		/**
		 * <p>
		 * Create a cell style.
		 * </p>
		 *
		 * @param name
		 *            The style name
		 * @param xfId
		 *            cellStyleXfs Id
		 * @return The cell style
		 */
		public static CTCellStyle createCellStyle(final @NotNull String name, final long xfId) {
			final CTCellStyle cellStyle = XLSX.smlObjectFactory.createCTCellStyle();
			cellStyle.setName(name);
			cellStyle.setXfId(xfId);

			return cellStyle;
		}

		/**
		 * <p>
		 * Create column size.
		 * </p>
		 *
		 * @param startColumnId
		 *            The min
		 * @param endColumnId
		 *            The max
		 * @param columnWidth
		 *            The column wifth
		 * @return Set the column wfith
		 */
		public static Col createColumnSize(final long startColumnId, final long endColumnId, final double columnWidth) {
			final Col col = XLSX.smlObjectFactory.createCol();

			col.setCustomWidth(Boolean.TRUE);
			col.setWidth(Double.valueOf(columnWidth));

			col.setMin(startColumnId);
			col.setMax(endColumnId);

			return col;
		}

		/**
		 * <p>
		 * Create a fill style.
		 * </p>
		 *
		 * @param cellColor
		 *            The colour to fill
		 * @return A fill stype
		 */
		public static CTFill createFillStyle(final @NotNull CharSequence cellColor) {
			final byte[] colorBytes = ConversionUtils.hexStringToByteArray(cellColor);

			final CTFill newFill = XLSX.smlObjectFactory.createCTFill();
			newFill.setPatternFill(XLSX.smlObjectFactory.createCTPatternFill());
			newFill.getPatternFill().setPatternType(STPatternType.SOLID);
			newFill.getPatternFill().setFgColor(XLSX.smlObjectFactory.createCTColor());
			newFill.getPatternFill().getFgColor().setRgb(colorBytes);
			newFill.getPatternFill().setBgColor(XLSX.smlObjectFactory.createCTColor());
			newFill.getPatternFill().getBgColor().setIndexed(Long.valueOf(64));

			return newFill;
		}

		/**
		 * <p>
		 * Create a font style.
		 * </p>
		 *
		 * @param fontSize
		 * @param fontName
		 * @param fontColor
		 * @param bold
		 * @param italic
		 * @param underline
		 * @param strikethrough
		 * @param verticalAlign
		 * @return
		 */
		public static CTFont createFontStyle(final double fontSize, final @NotNull String fontName,
				final @NotNull CharSequence fontColor, final boolean bold, final boolean italic,
				final Optional<? extends CTUnderlineProperty> underline, final boolean strikethrough,
				final Optional<? extends CTVerticalAlignFontProperty> verticalAlign) {
			final byte[] fontColorBytes = ConversionUtils.hexStringToByteArray(fontColor);

			final CTFont newFont = XLSX.smlObjectFactory.createCTFont();
			final CTColor newColor = XLSX.smlObjectFactory.createCTColor();
			newColor.setRgb(fontColorBytes);
			final JAXBElement<CTColor> newFontColorElement = XLSX.smlObjectFactory.createCTFontColor(newColor);
			newFont.getNameOrCharsetOrFamily().add(newFontColorElement);

			final CTFontSize newSize = XLSX.smlObjectFactory.createCTFontSize();
			newSize.setVal(fontSize);
			final JAXBElement<CTFontSize> newFontSizeElement = XLSX.smlObjectFactory.createCTFontSz(newSize);
			newFont.getNameOrCharsetOrFamily().add(newFontSizeElement);

			final CTFontName newName = XLSX.smlObjectFactory.createCTFontName();
			newName.setVal(fontName);
			final JAXBElement<CTFontName> newFontNameElement = XLSX.smlObjectFactory.createCTFontName(newName);
			newFont.getNameOrCharsetOrFamily().add(newFontNameElement);

			final CTFontFamily newFamily = XLSX.smlObjectFactory.createCTFontFamily();
			newFamily.setVal(2);
			final JAXBElement<CTFontFamily> newFontFamilyElement = XLSX.smlObjectFactory.createCTFontFamily(newFamily);
			newFont.getNameOrCharsetOrFamily().add(newFontFamilyElement);

			final CTFontScheme newScheme = XLSX.smlObjectFactory.createCTFontScheme();
			newScheme.setVal(STFontScheme.MINOR);
			final JAXBElement<CTFontScheme> newFontSchemeElement = XLSX.smlObjectFactory.createCTFontScheme(newScheme);
			newFont.getNameOrCharsetOrFamily().add(newFontSchemeElement);

			if (bold) {
				newFont.getNameOrCharsetOrFamily()
						.add(XLSX.smlObjectFactory.createCTFontB(XLSX.smlObjectFactory.createCTBooleanProperty()));
			}
			if (italic) {
				newFont.getNameOrCharsetOrFamily()
						.add(XLSX.smlObjectFactory.createCTFontI(XLSX.smlObjectFactory.createCTBooleanProperty()));
			}
			underline.ifPresent(ctUnderlineProperty -> newFont.getNameOrCharsetOrFamily()
					.add(XLSX.smlObjectFactory.createCTFontU(ctUnderlineProperty)));
			if (strikethrough) {
				newFont.getNameOrCharsetOrFamily()
						.add(XLSX.smlObjectFactory.createCTFontStrike(XLSX.smlObjectFactory.createCTBooleanProperty()));
			}
			verticalAlign.ifPresent(ctVerticalAlignFontProperty -> newFont.getNameOrCharsetOrFamily()
					.add(XLSX.smlObjectFactory.createCTFontVertAlign(ctVerticalAlignFontProperty)));

			return newFont;
		}

		/**
		 * <p>
		 * Create xf.
		 * </p>
		 *
		 * @param numberFormatId
		 * @param fontId
		 * @param fillId
		 * @param borderId
		 * @param center
		 * @param top
		 * @param wrap
		 * @param xfId
		 * @return
		 */
		public static CTXf createXf(final long numberFormatId, final long fontId, final long fillId,
				final long borderId, final boolean center, final boolean top, final boolean wrap,
				final Optional<Long> xfId) {
			final CTXf xf = XLSX.smlObjectFactory.createCTXf();
			xf.setNumFmtId(Long.valueOf(numberFormatId));
			xf.setFontId(Long.valueOf(fontId));
			xf.setFillId(Long.valueOf(fillId));
			xf.setBorderId(Long.valueOf(borderId));
			xfId.ifPresent(xf::setXfId);

			if (center || top || wrap) {
				final CTCellAlignment a = XLSX.smlObjectFactory.createCTCellAlignment();
				if (center) {
					a.setHorizontal(STHorizontalAlignment.CENTER);
				}
				if (top) {
					a.setVertical(STVerticalAlignment.TOP);
				}
				if (wrap) {
					a.setWrapText(Boolean.valueOf(true));
				}
				xf.setAlignment(a);
			}

			return xf;
		}

		/**
		 * <p>
		 * Default constructor.
		 * </p>
		 */
		XLSX() {
			try {
				this.fSpreadsheetMlPackage = SpreadsheetMLPackage.createPackage();
				this.fWorksheetParts = new ArrayList<>(8);
				// create the styles part
				this.stylesPart = new Styles(new PartName("/xl/styles.xml"));
				this.fSpreadsheetMlPackage.getWorkbookPart().addTargetPart(this.stylesPart);

				final CTStylesheet ss = XLSX.createBlankStylesheet();
				this.stylesPart.setJaxbElement(ss);

				// add default style
				addStyle(
						Optional.of(XLSX.createFontStyle(12, "Calibri", "FF000000", false, false, Optional.empty(),
								false, Optional.empty())),
						Optional.of(XLSX.createFillStyle("FFFFFFFF")), Optional.of(XLSX.createBorderStyle()), false,
						false, false);
				addFillStyle(XLSX.createFillStyle("FFFFFFFF"));
			} catch (final @NotNull Exception e) {
				XLSX.logger.error(e.getMessage(), e);
			}
		}

		/**
		 * <p>
		 * Add a border style to the document.
		 * </p>
		 *
		 * @param border
		 * @return borderId
		 * @throws Docx4JException
		 */
		private long addBorderStyle(final @NotNull CTBorder border) throws Docx4JException {
			final CTStylesheet st = this.stylesPart.getContents();
			final CTBorders borders = st.getBorders();
			final Long borderIndex = borders.getCount();
			borders.getBorder().add(border);
			borders.setCount(Long.valueOf(borders.getCount().longValue() + 1));

			return borderIndex.longValue();
		}

		/**
		 * <p>
		 * Add a cell style.
		 * </p>
		 *
		 * @param cellStyle
		 * @return
		 * @throws Docx4JException
		 */
		private long addCellStyle(final @NotNull CTCellStyle cellStyle) throws Docx4JException {
			final CTStylesheet st = this.stylesPart.getContents();
			final CTCellStyles cellStyles = st.getCellStyles();
			final Long cellStyleIndex = cellStyles.getCount();
			cellStyles.getCellStyle().add(cellStyle);
			cellStyles.setCount(Long.valueOf(cellStyles.getCount().longValue() + 1));

			return cellStyleIndex.longValue();
		}

		/**
		 * <p>
		 * Add cell style xf.
		 * </p>
		 *
		 * @param xf
		 * @return cellStyleXf index
		 * @throws Docx4JException
		 */
		private long addCellStyleXf(final @NotNull CTXf xf) throws Docx4JException {
			final CTStylesheet st = this.stylesPart.getContents();
			final CTCellStyleXfs cellStyleXfs = st.getCellStyleXfs();
			final Long xfIndex = cellStyleXfs.getCount();
			cellStyleXfs.getXf().add(xf);
			cellStyleXfs.setCount(Long.valueOf(cellStyleXfs.getCount().longValue() + 1));

			return xfIndex.longValue();
		}

		/**
		 * <p>
		 * Add cell xf.
		 * </p>
		 *
		 * @param xf
		 * @return cellXf index
		 * @throws Docx4JException
		 */
		private long addCellXf(final @NotNull CTXf xf) throws Docx4JException {
			final CTStylesheet st = this.stylesPart.getContents();
			final CTCellXfs cellXfs = st.getCellXfs();
			final Long xfIndex = cellXfs.getCount();
			cellXfs.getXf().add(xf);
			cellXfs.setCount(Long.valueOf(cellXfs.getCount().longValue() + 1));

			return xfIndex.longValue();
		}

		/**
		 * <p>
		 * Add a fill style to the document.
		 * </p>
		 *
		 * @param fill
		 * @return fillId
		 * @throws Docx4JException
		 */
		private long addFillStyle(final @NotNull CTFill fill) throws Docx4JException {
			final CTStylesheet st = this.stylesPart.getContents();
			final CTFills fills = st.getFills();
			final Long fillIndex = fills.getCount();
			fills.getFill().add(fill);
			fills.setCount(Long.valueOf(fills.getCount().longValue() + 1));

			return fillIndex.longValue();
		}

		/**
		 * <p>
		 * Add a font style to the document.
		 * </p>
		 *
		 * @param font
		 * @return fontId
		 * @throws Docx4JException
		 */
		private long addFontStyle(final @NotNull CTFont font) throws Docx4JException {
			final CTStylesheet st = this.stylesPart.getContents();
			final CTFonts fonts = st.getFonts();
			final long fontIndex = fonts.getCount().longValue();
			fonts.setCount(Long.valueOf(fonts.getCount().longValue() + 1));
			fonts.getFont().add(font);

			return fontIndex;
		}

		/**
		 * <p>
		 * Returns the indicated Cell of the indicated Row of the indicated Worksheet.
		 * </p>
		 *
		 * @param worksheetNumber
		 *            The number of the desired Worksheet
		 * @param rowNumber
		 *            The number of the desired Row
		 * @param cellNumber
		 *            The number of the desired Cell
		 * @return The indicated Cell of the indicated Row of the indicated Worksheet
		 * @throws IllegalArgumentException
		 *             if no such Cell yet exists in the indicated Worksheet
		 */
		@Nullable
		Cell getCell(final int worksheetNumber, final int rowNumber, final int cellNumber) {
			final Cell cell;

			try {
				final Row row = getRow(worksheetNumber, rowNumber);
				cell = row.getC().get(cellNumber - 1);
			} catch (final @NotNull Exception ignored) {
				final String message = "No such cell: worksheet=" + worksheetNumber + " row=" + rowNumber + " cell="
						+ cellNumber;
				throw (new IllegalArgumentException(message));
			}

			return cell;
		}

		/**
		 * <p>
		 * Returns the Row of the indicated Worksheet having the given row number.
		 * </p>
		 *
		 * @param worksheetNumber
		 *            The number of the desired Worksheet
		 * @param rowNumber
		 *            The number of the desired Row
		 * @return The Row of the indicated Worksheet having the given row number, or null if there is no such Row
		 * @throws IllegalArgumentException
		 *             if no such Row yet exists in the indicated Worksheet
		 */
		@Nullable
		Row getRow(final int worksheetNumber, final int rowNumber) {
			final Row row;

			try {
				final SheetData sheetData = this.fWorksheetParts.get(worksheetNumber - 1).getContents().getSheetData();
				final List<Row> rows = sheetData.getRow();
				row = rows.get(rowNumber - 1);
			} catch (final @NotNull Exception ignored) {
				final String message = "No such row: worksheet=" + worksheetNumber + " row=" + rowNumber;
				throw (new IllegalArgumentException(message));
			}

			return row;
		}

		/**
		 * <p>
		 * Adds a new cell to the indicated row of the indicated worksheet.
		 * </p>
		 *
		 * @param worksheetNumber
		 * @param rowNumber
		 * @param cellValue
		 * @param styleId
		 */
		public void addCell(final int worksheetNumber, final int rowNumber, @NotNull final CharSequence cellValue,
				final long styleId) {
			final Row row = getRow(worksheetNumber, rowNumber);

			final Cell cell = XLSX.smlObjectFactory.createCell();
			CharSequence cellData = ValidationUtils.isEmpty(cellValue) ? CommonConstants.EMPTY_STRING : cellValue;
			if (cellData.length() > 32700) {
				cellData = cellData.subSequence(0, 32700);
			}

			if (ValidationUtils.isNumber(cellData)) {
				cell.setT(STCellType.N);
			} else {
				cell.setT(STCellType.STR);
			}
			cell.setV(cellData.toString());

			// If we are using a template, use the same styles
			cell.setS(Long.valueOf(styleId));

			final int currentNumCells = row.getC().size();
			cell.setR(XLSX.getCellId((currentNumCells + 1), rowNumber));

			row.getC().add(cell);
		}

		/**
		 * <p>
		 * Adds a new row to the indicated Worksheet.
		 * </p>
		 *
		 * @param worksheetNumber
		 *            The number of the Worksheet to which to add a new row
		 * @return The number of the new Row
		 * @throws Docx4JException
		 */
		public int addRow(final int worksheetNumber) throws Docx4JException {
			return addRow(worksheetNumber, 0L);
		}

		/**
		 * <p>
		 * Adds a new Row to the indicated Worksheet, and applies to it the custom style having the given style ID.
		 * </p>
		 *
		 * @param worksheetNumber
		 *            The number of the Worksheet to which to add a new row
		 * @param styleId
		 *            The ID of the custom style to apply to the new Row
		 * @return The number of the new Row
		 * @throws Docx4JException
		 */
		public int addRow(final int worksheetNumber, final long styleId) throws Docx4JException {
			final SheetData sheetData = this.fWorksheetParts.get(worksheetNumber - 1).getContents().getSheetData();
			final Row row = XLSX.smlObjectFactory.createRow();

			if (styleId > 0L) {
				row.setCustomFormat(Boolean.TRUE);
				row.setS(Long.valueOf(styleId));
			}
			sheetData.getRow().add(row);

			return sheetData.getRow().size();
		}

		/**
		 * <p>
		 * Add a style to the document.
		 * </p>
		 *
		 * @param font
		 * @param fill
		 * @param border
		 * @param center
		 * @param top
		 * @param wrap
		 *
		 * @return
		 * @throws Docx4JException
		 */
		public long addStyle(final @NotNull Optional<? extends CTFont> font,
				final @NotNull Optional<? extends CTFill> fill, final @NotNull Optional<? extends CTBorder> border,
				final boolean center, final boolean top, final boolean wrap) throws Docx4JException {

			final long numFmtId = 0L;
			long fontId = 0L;
			long fillId = 0L;
			long borderId = 0L;
			final long cellStyleXfId;
			final long styleIndex;

			if (font.isPresent()) {
				fontId = addFontStyle(font.get());
			}
			if (fill.isPresent()) {
				fillId = addFillStyle(fill.get());
			}
			if (border.isPresent()) {
				borderId = addBorderStyle(border.get());
			}
			// create the cell style xfs entry
			cellStyleXfId = addCellStyleXf(
					XLSX.createXf(numFmtId, fontId, fillId, borderId, center, top, wrap, Optional.empty()));
			// create the cell xfs entry
			styleIndex = addCellXf(XLSX.createXf(numFmtId, fontId, fillId, borderId, center, top, wrap,
					Optional.of(Long.valueOf(cellStyleXfId))));
			// create the cell style entry
			addCellStyle(XLSX.createCellStyle("style " + (cellStyleXfId + 1), cellStyleXfId));

			return styleIndex;
		}

		/**
		 * <p>
		 * Adds a worksheet to the workbook of this spreadsheet.
		 * </p>
		 *
		 * @param wsName
		 *
		 * @return index of the new worksheet
		 */
		public int addWorksheet(final String wsName) {
			int numWorksheets = this.fWorksheetParts.size();
			numWorksheets++;

			final String partname = "/xl/worksheets/sheet" + numWorksheets + ".xml";
			final String title;
			if (ValidationUtils.isEmpty(wsName)) {
				title = "Sheet" + numWorksheets;
			} else {
				title = wsName;
			}

			try {
				final WorksheetPart t = this.fSpreadsheetMlPackage.createWorksheetPart(new PartName(partname), title,
						numWorksheets);
				this.fWorksheetParts.add(t);
			} catch (@NotNull final InvalidFormatException | JAXBException e) {
				XLSX.logger.error(e.getMessage(), e);
			} catch (final @NotNull Exception e) {
				XLSX.logger.error(e.getMessage(), e);
				return -1;
			}

			return numWorksheets;
		}

		/**
		 * <p>
		 * Set the column sizes for the worksheet.
		 * </p>
		 *
		 * @param worksheetNumber
		 * @param columns
		 * @throws Docx4JException
		 */
		public void addWorksheetColumnSizes(final int worksheetNumber, final Col... columns) throws Docx4JException {
			final List<Cols> colList = this.fWorksheetParts.get(worksheetNumber - 1).getContents().getCols();
			final Cols cols;
			if (colList.isEmpty()) {
				// create new cols list and add it to the worksheet
				cols = XLSX.smlObjectFactory.createCols();
				colList.add(cols);
			} else {
				cols = colList.get(0);
			}
			// add col entries into the cols list
			for (final Col column : columns) {
				cols.getCol().add(column);
			}
		}

		/**
		 * <p>
		 * Saves (the SpreadsheetMlPackage of) this EXTENSION_XLSX to the given output File.
		 * </p>
		 *
		 * @param outputFile
		 *            The File to which to save (the SpreadsheetMlPackage of) this EXTENSION_XLSX
		 * @throws Docx4JException
		 *             if the save attempt fails
		 */
		public void save(final @NotNull File outputFile) throws Docx4JException {
			this.fSpreadsheetMlPackage.save(outputFile);
		}
	}

	/**
	 * <p>
	 * A Worksheet class used to represent a single worksheet in a workbook.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	public final class Worksheet {
		private final class Row {
			private final Logger logger = LogManager.getLogger();
			private int rowId;

			Row() {
				try {
					this.rowId = ExcelOut.this.xlsx.addRow(Worksheet.this.worksheetId);
				} catch (final Docx4JException e) {
					this.logger.error("There was a problem with the DocX4J API: {}", e.getMessage());
				}
			}

			/**
			 * <p>
			 * Adds any number of cells to a Row.
			 * </p>
			 *
			 * @param styleId
			 *            The ID of the style.
			 * @param cellData
			 *            The data to append.
			 */
			void addCells(final long styleId, final String... cellData) {
				if (ValidationUtils.isEmpty((Object[]) cellData))
					return;
				Arrays.stream(cellData).map(cell -> Objects.isNull(cell) ? "" : cell).forEach(
						data -> ExcelOut.this.xlsx.addCell(Worksheet.this.worksheetId, this.rowId, data, styleId));
			}

			/**
			 * <p>
			 * Adds any number of cells to a Row.
			 * </p>
			 *
			 * @param cellData
			 *            The data to append.
			 */
			void addCells(final String... cellData) {
				if (ValidationUtils.isEmpty((Object[]) cellData))
					return;
				Arrays.stream(cellData).map(cell -> Objects.isNull(cell) ? "" : cell).forEach(data -> ExcelOut.this.xlsx
						.addCell(Worksheet.this.worksheetId, this.rowId, data, ExcelOut.this.standardStyleId));
			}

		}

		private final Logger logger = LogManager.getLogger();
		final int worksheetId;

		Worksheet(final @NotNull String name, final int numberOfColumns) {
			this.worksheetId = ExcelOut.this.xlsx.addWorksheet(name);
			try {
				ExcelOut.this.xlsx.addWorksheetColumnSizes(this.worksheetId,
						XLSX.createColumnSize(1, numberOfColumns, 40));
			} catch (final Docx4JException e) {
				this.logger.error("There was a problem with the DocX4J API: {}", e.getMessage());
			}
		}

		/**
		 * <p>
		 * Adds a styled header row to the worksheet with the provided cell data.
		 * </p>
		 *
		 * @param cellData
		 */
		public void addHeaderRow(final String... cellData) {
			final Row row = new Row();
			row.addCells(ExcelOut.this.shadedStyleId, cellData);
		}

		/**
		 * <p>
		 * Adds a row to the worksheet with the provided cell data.
		 * </p>
		 *
		 * @param cellData
		 */
		public void addRow(final String... cellData) {
			final Row row = new Row();
			row.addCells(cellData);
		}
	}

	private static final String columnColor = "FF16365C";
	private static final String columnTextColor = "FFFFFFFF";
	private static final Logger logger = LogManager.getLogger();
	private Path finalPath;
	private java.io.File reportFile;
	private int worksheetNumber = 1;
	private final Collection<Worksheet> worksheets = new ArrayList<>(8);
	long shadedStyleId;
	long standardStyleId;
	final XLSX xlsx = new XLSX();

	/**
	 * <p>
	 * This constructor creates a new instance of ExcelOut using a temporary file.
	 * </p>
	 */
	public ExcelOut() {
		setShading();

		// Set up a temporary file
		final FileAttribute<Set<PosixFilePermission>> permissions = PosixFilePermissions
				.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
		try {
			this.finalPath = Files.createTempFile("export-", "-main.xlsx", permissions);
			this.reportFile = this.finalPath.toFile();
			this.reportFile.deleteOnExit();
		} catch (final IOException e) {
			ExcelOut.logger.fatal(
					"There was an error trying to create the temporary file that would store our email's body: {}",
					e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Creates a new instance of ExcelOut for the Utility Suite.
	 * </p>
	 *
	 * @param exportFile
	 * @throws IOException
	 */
	public ExcelOut(final @NotNull File exportFile) throws IOException {
		setShading();

		if (exportFile == null) {
			ExcelOut.logger.error("File required to create excel export, falling back to default.");
			final FileAttribute<Set<PosixFilePermission>> permissions = PosixFilePermissions
					.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
			this.finalPath = Files.createTempFile("export", "xlsx", permissions);
			this.reportFile = this.finalPath.toFile();
			this.reportFile.deleteOnExit();
		} else {
			this.finalPath = exportFile.toPath();
			this.reportFile = exportFile;
		}
	}

	/**
	 * <p>
	 * This method sets up the cell styles.
	 * </p>
	 */
	private void setShading() {
		try {
			this.shadedStyleId = this.xlsx.addStyle(
					Optional.of(XLSX.createFontStyle(11, "Calibri", ExcelOut.columnTextColor, false, false,
							Optional.empty(), false, Optional.empty())),
					Optional.of(XLSX.createFillStyle(ExcelOut.columnColor)), Optional.empty(), true, true, true);
			this.xlsx.addStyle(Optional.of(XLSX.createFontStyle(16, "Calibri", "FF000000", false, true,
					Optional.empty(), false, Optional.empty())), Optional.empty(), Optional.empty(), false, false,
					false);
			this.standardStyleId = this.xlsx.addStyle(Optional.of(XLSX.createFontStyle(12, "Calibri", "FF000000", false,
					false, Optional.empty(), false, Optional.empty())), Optional.empty(), Optional.empty(), true, true,
					true);
		} catch (final Docx4JException e) {
			ExcelOut.logger.error("There was a problem with the DocX4J API: {}", e.getMessage());
		}
	}

	/**
	 * <p>
	 * Adds a worksheet to the workbook.
	 * </p>
	 *
	 * @param name
	 *            Name of the worksheet
	 * @param numberOfColumns
	 * @return
	 */
	public Worksheet addWorksheet(final @NotNull String name, final int numberOfColumns) {
		// Create our worksheet
		final Worksheet ws = new Worksheet(ValidationUtils.isEmpty(name) ? ("Sheet " + this.worksheets.size()) : name,
				numberOfColumns);
		this.worksheets.add(ws);

		return ws;
	}

	@Override
	public void close() {
		if (Objects.isNull(this.reportFile)) {
			this.reportFile = this.finalPath.toFile();
		}
		export();
	}

	/**
	 * <p>
	 * This method creates a new worksheet and sets us to it.
	 * </p>
	 */
	@Override
	public void createTemporaryFile(final String filename) {
		this.worksheetNumber = this.xlsx.addWorksheet(filename);
	}

	/**
	 * <p>
	 * Prepares the Excel file for export.
	 * </p>
	 *
	 * @return FileVo pointing to the file to be exported
	 */
	public @Nullable FileVo export() {
		try {
			this.xlsx.save(this.reportFile);
			final FileVo file = new FileVo();
			file.setFile(this.reportFile.getAbsolutePath());
			return file;
		} catch (final Docx4JException e) {
			ExcelOut.logger.error("There was a problem with the DocX4J API: {}", e.getMessage());
		}

		return null;
	}

	/**
	 * <p>
	 * This method sets the final output file path to which all data will ultimately be written.
	 * </p>
	 *
	 * @param filename
	 *            The path, including the file name
	 * @return This instance
	 */
	public ExcelOut setFile(final String filename) {
		if (Objects.nonNull(this.finalPath)) {
			this.finalPath.toFile().delete();
		}

		if (!ValidationUtils.isEmpty(filename)) {
			this.finalPath = new File(filename).toPath().toAbsolutePath();
		}

		return this;
	}

	/**
	 * <p>
	 * This method writes to the current worksheet.
	 * </p>
	 */
	@Override
	public void writeLineToMainOutput(final String... cells) {
		if (Objects.nonNull(cells)) {
			final int rowNum;
			try {
				rowNum = this.xlsx.addRow(this.worksheetNumber);
				final long style;
				if (rowNum == 1) {
					style = this.shadedStyleId;
				} else {
					style = this.standardStyleId;
				}
				for (final String cell : cells) {
					this.xlsx.addCell(this.worksheetNumber, rowNum, cell, style);
				}
			} catch (final Docx4JException e) {
				ExcelOut.logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * <p>
	 * This method is equivalent to {@link #writeLineToMainOutput(String...)}.
	 * </p>
	 */
	@Override
	public void writeLineToTemporaryFile(final String... cells) {
		writeLineToMainOutput(cells);
	}
}
