package local.rdps.svja.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This class contains a large number of constants used in handling, validating, and returning files.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class FileConstants {
	/**
	 * Represents file type categories
	 *
	 * @since 1.0
	 */
	public enum FileTypeCategory {
		DOCUMENT_FILES, FRAMEWORK_FILES, IMAGE_FILES, IMMUTABLE_DOCUMENT_FILES, VIDEO_FILES
	}

	public static final String CHARSET = "utf-8";
	public static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";
	public static final String CONTENT_TYPE_IMAGE_BMP = "image/bmp";
	public static final String CONTENT_TYPE_IMAGE_GIF = "image/gif";
	public static final String CONTENT_TYPE_IMAGE_JPG = "image/jpeg";
	public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
	public static final String CONTENT_TYPE_IMAGE_TIFF = "image/tiff";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_OPEN_XML_FORMATS = "application/vnd.openxmlformats";
	public static final String CONTENT_TYPE_PDF = "application/pdf";
	public static final String CONTENT_TYPE_PPS = "application/vnd.ms-pps";
	public static final String CONTENT_TYPE_PPSX = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
	public static final String CONTENT_TYPE_PPT = "application/vnd.ms-powerpoint";
	public static final String CONTENT_TYPE_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String CONTENT_TYPE_TEXT = "text/plain";
	public static final String CONTENT_TYPE_VIDEO_AVI = "video/avi";
	public static final String CONTENT_TYPE_VIDEO_FLV = "video/x-flv";
	public static final String CONTENT_TYPE_VIDEO_M1V = "video/mpeg";
	public static final String CONTENT_TYPE_VIDEO_M2V = "video/mpeg";
	public static final String CONTENT_TYPE_VIDEO_MOV = "video/quicktime";
	public static final String CONTENT_TYPE_VIDEO_MP4 = "video/mp4";
	public static final String CONTENT_TYPE_VIDEO_MPE = "video/mpeg";
	public static final String CONTENT_TYPE_VIDEO_MPEG = "video/mpeg";
	public static final String CONTENT_TYPE_VIDEO_MPG = "video/mpeg";
	public static final String CONTENT_TYPE_VIDEO_QT = "video/quicktime";
	public static final String CONTENT_TYPE_VIDEO_WMV = "video/x-ms-wmv";
	public static final String CONTENT_TYPE_WORD = "application/ms-word";
	public static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String CONTENT_TYPE_XML = "application/xml";
	public static final String CONTENT_TYPE_ZIP = "application/zip";
	public static final String EXTENSION_AVI = "avi";
	public static final String EXTENSION_BMP = "bmp";
	public static final String EXTENSION_CSV = "csv";
	public static final String EXTENSION_DOC = "doc";
	public static final String EXTENSION_DOCX = "docx";
	public static final String EXTENSION_FLV = "flv";
	public static final String EXTENSION_GIF = "gif";
	public static final String EXTENSION_JPEG = "jpeg";
	public static final String EXTENSION_JPG = "jpg";
	public static final String EXTENSION_MOV = "mov";
	public static final String EXTENSION_MP4 = "mp4";
	public static final String EXTENSION_MPEG = "mpeg";
	public static final String EXTENSION_MPG = "mpg";
	public static final String EXTENSION_PDF = "pdf";
	public static final String EXTENSION_PNG = "png";
	public static final String EXTENSION_PPS = "pps";
	public static final String EXTENSION_PPSX = "ppsx";
	public static final String EXTENSION_PPT = "ppt";
	public static final String EXTENSION_PPTX = "pptx";
	public static final String EXTENSION_TIF = "tif";
	public static final String EXTENSION_TIFF = "tiff";
	public static final String EXTENSION_WMV = "wmv";
	public static final String EXTENSION_XLS = "xls";
	public static final String EXTENSION_XLSX = "xlsx";
	public static final String EXTENSION_XML = "xml";
	public static final String EXTENSION_ZIP = "zip";
	/**
	 * Maps file types to content types
	 *
	 * @since 1.0
	 */
	public static final @NotNull Map<String, String> FILE_CONTENT_TYPES = FileConstants.getFileContentTypesMap();
	public static final String FILE_DOWNLOAD_COOKIE_NAME = "fileDownload";
	/**
	 * Maps file types to file type categories
	 *
	 * @since 1.0
	 */
	public static final Map<FileTypeCategory, List<String>> FILE_UPLOAD_TYPES_MAP = FileConstants
			.populateFileUploadTypesMap();
	public static final int MAX_DOWNLOAD_FILENAME_LENGTH = 250;
	public static final String SITEMAP_API_FILENAME = "api_sitemap.xml";
	public static final String SITEMAP_MASTER_FILE_FILENAME = "sitemap.xml";
	public static final String SITEMAP_PROJECTS_FILENAME = "projects_sitemap.xml";
	public static final String SITEMAP_STATIC_FILENAME = "static_sitemap.xml";

	/**
	 * This is the size buffers should be when transferring files and other data across the network (it is 33.55MB)
	 *
	 * @since 1.0
	 */
	public static final int TRANSFER_BUFFER_SIZE = 2 << 25;

	private static @NotNull Map<String, String> getFileContentTypesMap() {
		if (Objects.nonNull(FileConstants.FILE_CONTENT_TYPES) && !FileConstants.FILE_CONTENT_TYPES.isEmpty())
			return FileConstants.FILE_CONTENT_TYPES;

		return Map.ofEntries(Map.entry(FileConstants.EXTENSION_XLS, FileConstants.CONTENT_TYPE_EXCEL),
				Map.entry(FileConstants.EXTENSION_DOC, FileConstants.CONTENT_TYPE_WORD),
				Map.entry(FileConstants.EXTENSION_PPT, FileConstants.CONTENT_TYPE_PPT),
				Map.entry(FileConstants.EXTENSION_PPS, FileConstants.CONTENT_TYPE_PPS),
				Map.entry(FileConstants.EXTENSION_PDF, FileConstants.CONTENT_TYPE_PDF),
				Map.entry(FileConstants.EXTENSION_XLSX, FileConstants.CONTENT_TYPE_XLSX),
				Map.entry(FileConstants.EXTENSION_PPTX, FileConstants.CONTENT_TYPE_PPTX),
				Map.entry(FileConstants.EXTENSION_PPSX, FileConstants.CONTENT_TYPE_PPSX),
				Map.entry(FileConstants.EXTENSION_DOCX, FileConstants.CONTENT_TYPE_DOCX),
				Map.entry(FileConstants.EXTENSION_PNG, FileConstants.CONTENT_TYPE_IMAGE_PNG),
				Map.entry(FileConstants.EXTENSION_JPEG, FileConstants.CONTENT_TYPE_IMAGE_JPG),
				Map.entry(FileConstants.EXTENSION_JPG, FileConstants.CONTENT_TYPE_IMAGE_JPG),
				Map.entry(FileConstants.EXTENSION_TIF, FileConstants.CONTENT_TYPE_IMAGE_TIFF),
				Map.entry(FileConstants.EXTENSION_TIFF, FileConstants.CONTENT_TYPE_IMAGE_TIFF),
				Map.entry(FileConstants.EXTENSION_GIF, FileConstants.CONTENT_TYPE_IMAGE_GIF),
				Map.entry(FileConstants.EXTENSION_BMP, FileConstants.CONTENT_TYPE_IMAGE_BMP),
				Map.entry(FileConstants.EXTENSION_MOV, FileConstants.CONTENT_TYPE_VIDEO_MOV),
				Map.entry(FileConstants.EXTENSION_AVI, FileConstants.CONTENT_TYPE_VIDEO_AVI),
				Map.entry(FileConstants.EXTENSION_MPEG, FileConstants.CONTENT_TYPE_VIDEO_MPEG),
				Map.entry(FileConstants.EXTENSION_MPG, FileConstants.CONTENT_TYPE_VIDEO_MPG),
				Map.entry(FileConstants.EXTENSION_MP4, FileConstants.CONTENT_TYPE_VIDEO_MP4),
				Map.entry(FileConstants.EXTENSION_FLV, FileConstants.CONTENT_TYPE_VIDEO_FLV),
				Map.entry(FileConstants.EXTENSION_WMV, FileConstants.CONTENT_TYPE_VIDEO_WMV),
				Map.entry(FileConstants.EXTENSION_ZIP, FileConstants.CONTENT_TYPE_ZIP));
	}

	/**
	 * <p>
	 * Populates the file types map.
	 * </p>
	 *
	 * @return A mapping of {@link FileTypeCategory} instances against the corresponding extensions (file types)
	 * @since 1.0
	 */
	private static Map<FileTypeCategory, List<String>> populateFileUploadTypesMap() {
		final Map<FileTypeCategory, List<String>> fileUploadTypesMap = new HashMap<>();

		fileUploadTypesMap.put(FileTypeCategory.IMAGE_FILES,
				List.of(FileConstants.EXTENSION_BMP, FileConstants.EXTENSION_GIF, FileConstants.EXTENSION_JPEG,
						FileConstants.EXTENSION_JPG, FileConstants.EXTENSION_PNG, FileConstants.EXTENSION_TIF,
						FileConstants.EXTENSION_TIFF));

		fileUploadTypesMap.put(FileTypeCategory.VIDEO_FILES,
				List.of(FileConstants.EXTENSION_WMV, FileConstants.EXTENSION_AVI, FileConstants.EXTENSION_FLV,
						FileConstants.EXTENSION_MP4, FileConstants.EXTENSION_MPG, FileConstants.EXTENSION_MPEG,
						FileConstants.EXTENSION_MOV));

		fileUploadTypesMap.put(FileTypeCategory.DOCUMENT_FILES,
				List.of(FileConstants.EXTENSION_PDF, FileConstants.EXTENSION_DOCX, FileConstants.EXTENSION_DOC,
						FileConstants.EXTENSION_PPT, FileConstants.EXTENSION_PPTX, FileConstants.EXTENSION_PPS,
						FileConstants.EXTENSION_PPSX, FileConstants.EXTENSION_XLS, FileConstants.EXTENSION_XLSX));

		fileUploadTypesMap.put(FileTypeCategory.IMMUTABLE_DOCUMENT_FILES,
				List.of(FileConstants.EXTENSION_PDF, FileConstants.EXTENSION_PPS, FileConstants.EXTENSION_PPSX));

		fileUploadTypesMap.put(FileTypeCategory.FRAMEWORK_FILES,
				List.of(FileConstants.EXTENSION_PDF, FileConstants.EXTENSION_PPT, FileConstants.EXTENSION_PPTX,
						FileConstants.EXTENSION_PPS, FileConstants.EXTENSION_PPSX, FileConstants.EXTENSION_BMP,
						FileConstants.EXTENSION_GIF, FileConstants.EXTENSION_JPEG, FileConstants.EXTENSION_JPG,
						FileConstants.EXTENSION_PNG, FileConstants.EXTENSION_TIF, FileConstants.EXTENSION_TIFF));

		return Collections.unmodifiableMap(fileUploadTypesMap);
	}

	/**
	 * <p>
	 * Uses the file type categories map to return a list of file types under a given category
	 * </p>
	 *
	 * @param fileTypeCategory A file type category that we would like to get a listing for
	 * @return A {@link List} containing the file types allowed based on the given category
	 * @since 1.0
	 */
	public static @NotNull List<String> getValidFileUploadTypes(final @NotNull FileTypeCategory fileTypeCategory) {
		if (!FileConstants.FILE_UPLOAD_TYPES_MAP.containsKey(fileTypeCategory)
				|| Objects.nonNull(FileConstants.FILE_UPLOAD_TYPES_MAP.get(fileTypeCategory)))
			return new ArrayList<>();
		return FileConstants.FILE_UPLOAD_TYPES_MAP.get(fileTypeCategory);
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 *
	 * @since 1.0
	 */
	private FileConstants() {
		// Do nothing
	}
}
