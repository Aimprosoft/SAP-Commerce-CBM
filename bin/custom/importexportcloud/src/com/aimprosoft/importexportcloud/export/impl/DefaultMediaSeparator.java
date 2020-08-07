package com.aimprosoft.importexportcloud.export.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.MediaBatchArchiveProducer;
import com.aimprosoft.importexportcloud.export.MediaSeparator;
import com.aimprosoft.importexportcloud.service.ArchiveService;
import de.hybris.platform.util.Config;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class DefaultMediaSeparator implements MediaSeparator
{
	private static final Logger LOGGER = Logger.getLogger(DefaultMediaSeparator.class);

	private static final String MEDIA_IMPORT_SCRIPT = "(\"#% impex.setLocale.+|#.+[\\s]insert_update Media[\\s\\S]+?\"#.+\"\"Media.csv.+[\\s].+)";
	private static final String MEDIA_CONTAINER_IMPORT_SCRIPT = "(#.+[\\s]insert_update MediaContainer;[\\s\\S]+?\"#.+\"\"MediaContainer.csv.+[\\s].+)";
	private static final String UNZIPPED_CONTENT_FOLDER = "unzippedContent";
	private static final String MEDIA_CSV_FILE_NAME = "Media.csv";
	private static final String MEDIA_CONTAINER_CSV_FILE_NAME = "MediaContainer.csv";
	private static final String IMPORT_SCRIPT_FILE_NAME = "importscript.impex";
	private static final String FILE_SEPARATOR = File.separator;
	private static final String EOL = System.lineSeparator();

	private MediaBatchArchiveProducer mediaBatchArchiveProducer;
	private ArchiveService archiveService;

	public void separateMedia(final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			LOGGER.info("Start separating media process");

			final List<String> allMediaLines = getMediaLines(zipFileSystem);

			archiveService.unzipArchive(exportImpexAndCSVsFilePath, UNZIPPED_CONTENT_FOLDER);

			final String csvMediaHeader = allMediaLines.get(0);

			processSeparation(allMediaLines, zipFileSystem, csvMediaHeader);

			updateResultArchiveData();
			archiveService.createArchive(exportImpexAndCSVsFilePath, UNZIPPED_CONTENT_FOLDER);

			LOGGER.info("Separating media process has finished successfully");
		}
		catch (IOException e)
		{
			throw new ExportException("An error occurred while separating media process.", e);
		}
		finally
		{
			removeFilesFromTempContentFolder();
		}
	}

	private void processSeparation(final List<String> allMediaLines, final FileSystem zipFileSystem, final String csvMediaHeader)
			throws IOException, ExportException
	{
		final int batchMediaSize = Config.getInt("media.batch.size", 1000);
		final int totalNumberOfSubArchives = (int) Math.ceil((double) allMediaLines.size() / batchMediaSize);

		int archiveNumber = 1;
		for (int i = 1; i < allMediaLines.size(); i += batchMediaSize + 1)
		{
			int toIndex = allMediaLines.size() > i + batchMediaSize ? i + batchMediaSize : allMediaLines.size();
			final List<String> linesToWrite = allMediaLines.subList(i, toIndex);
			final List<String> mediaScriptToWrite =
					archiveNumber == 1 ? createImportMediaScript(zipFileSystem, true) :
							createImportMediaScript(zipFileSystem, false);

			linesToWrite.add(0, csvMediaHeader);
			mediaBatchArchiveProducer.produceArchive(linesToWrite, mediaScriptToWrite, archiveNumber);

			LOGGER.info(String.format("Created subarchives - %d, %d left", archiveNumber, totalNumberOfSubArchives - archiveNumber));

			archiveNumber++;
		}
	}

	private List<String> getMediaLines(final FileSystem zipFileSystem) throws IOException
	{
		final Path targetFile = zipFileSystem.getPath(MEDIA_CSV_FILE_NAME);
		return Files.readAllLines(targetFile);
	}

	private List<String> createImportMediaScript(final FileSystem zipFileSystem, boolean isMediaContainerNeeded) throws IOException
	{
		final Path targetFile = zipFileSystem.getPath(IMPORT_SCRIPT_FILE_NAME);
		final List<String> allLinesOfFile = Files.readAllLines(targetFile);

		return getMediaScript(allLinesOfFile, isMediaContainerNeeded);
	}

	private List<String> getMediaScript(final List<String> allScriptLines, final boolean isMediaContainerNeeded)
	{
		final List<String> mediaScript = new ArrayList<>();
		final String script = allScriptLines.stream().collect(Collectors.joining(EOL));
		Pattern pattern = Pattern.compile(MEDIA_IMPORT_SCRIPT);
		Matcher matcher = pattern.matcher(script);
		while (matcher.find())
		{
			mediaScript.add(matcher.group(0));
		}
		if (isMediaContainerNeeded)
		{
			getMediaContainerScript(script, mediaScript);
		}
		return mediaScript;
	}

	private void getMediaContainerScript(final String script, final List<String> mediaScript)
	{
		Pattern pattern = Pattern.compile(MEDIA_CONTAINER_IMPORT_SCRIPT);
		Matcher matcher = pattern.matcher(script);
		while (matcher.find())
		{
			mediaScript.add(matcher.group(0));
		}
	}

	private void removeFilesFromTempContentFolder() throws ExportException
	{
		LOGGER.debug("Deleting temporary directory");

		File tmpDirectory = new File(UNZIPPED_CONTENT_FOLDER);
		try
		{
			FileUtils.deleteDirectory(tmpDirectory);
		}
		catch (IOException e)
		{
			throw new ExportException("An error occurred while deleting temporary files", e);
		}
	}

	private void updateResultArchiveData() throws IOException
	{
		File importscript = new File(UNZIPPED_CONTENT_FOLDER + FILE_SEPARATOR + IMPORT_SCRIPT_FILE_NAME);
		final Path filePath = importscript.toPath();
		List<String> allScriptLines = Files.readAllLines(filePath);

		updateOriginScript(allScriptLines, importscript.toPath());
		removeFileFromTempContentFolder(MEDIA_CSV_FILE_NAME);
		removeFileFromTempContentFolder(MEDIA_CONTAINER_CSV_FILE_NAME);
	}

	private void updateOriginScript(List<String> allLinesOfScriptFile, Path impexscriptFile) throws IOException
	{
		String script = allLinesOfScriptFile.stream().collect(Collectors.joining(EOL));
		script = script.replaceAll(MEDIA_IMPORT_SCRIPT, "");
		script = script.replaceAll(MEDIA_CONTAINER_IMPORT_SCRIPT, "");
		List<String> linesToWrite = Arrays.asList(script.split(EOL));

		Files.write(impexscriptFile, linesToWrite, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	private void removeFileFromTempContentFolder(String fileName) throws IOException
	{
		File mediaCsv = new File(UNZIPPED_CONTENT_FOLDER + FILE_SEPARATOR + fileName);
		Files.delete(mediaCsv.toPath());
	}

	@Required
	public void setMediaBatchArchiveProducer(MediaBatchArchiveProducer mediaBatchArchiveProducer)
	{
		this.mediaBatchArchiveProducer = mediaBatchArchiveProducer;
	}

	@Required
	public void setArchiveService(ArchiveService archiveService)
	{
		this.archiveService = archiveService;
	}
}
