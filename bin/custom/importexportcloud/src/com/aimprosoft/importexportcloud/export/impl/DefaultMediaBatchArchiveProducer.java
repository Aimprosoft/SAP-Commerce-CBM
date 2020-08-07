package com.aimprosoft.importexportcloud.export.impl;

import com.aimprosoft.importexportcloud.export.MediaBatchArchiveProducer;
import com.aimprosoft.importexportcloud.service.ArchiveService;
import de.hybris.platform.util.CSVReader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;


public class DefaultMediaBatchArchiveProducer implements MediaBatchArchiveProducer
{
	private static final Logger LOGGER = Logger.getLogger(DefaultMediaBatchArchiveProducer.class);

	private static final String MEDIA_CSV_FILE_NAME = "Media.csv";
	private static final String UNZIPPED_CONTENT_FOLDER = "unzippedContent";
	private static final String IMPORT_SCRIPT_FILE_NAME = "importscript.impex";

	private static final String FILE_SEPARATOR = File.separator;
	private static final String EOL = System.lineSeparator();
	private ArchiveService archiveService;

	public void produceArchive(final List<String> linesToWrite, final List<String> mediaImportScript, final int archiveNumber)
			throws IOException
	{
		LOGGER.debug(String.format("Start creating %d media archive", archiveNumber));

		final String mediaArchiveName = UNZIPPED_CONTENT_FOLDER + FILE_SEPARATOR + "Media" + archiveNumber + ".zip";
		try (final FileOutputStream fileOutputStream = new FileOutputStream(mediaArchiveName);
			  final ZipOutputStream zipOut = new ZipOutputStream(fileOutputStream))
		{
			if (archiveNumber == 1)
			{
				final File mediaContainer = getMediaFile("MediaContainer.csv");
				archiveService.putIntoArchive(zipOut, mediaContainer);
			}

			final File mediaCsvFile = new File(MEDIA_CSV_FILE_NAME);
			Files.write(mediaCsvFile.toPath(), linesToWrite);

			final File mediaScript = new File(IMPORT_SCRIPT_FILE_NAME);
			Files.write(mediaScript.toPath(), mediaImportScript);

			putContentIntoArchive(zipOut, mediaCsvFile, mediaScript, linesToWrite);
		}

		LOGGER.debug(String.format("Finish creating %d media archive", archiveNumber));
	}

	private void putContentIntoArchive(final ZipOutputStream zipOut, final File mediaCsvFile, final File mediaScript,
			final List<String> linesToWrite)
			throws IOException
	{
		archiveService.putIntoArchive(zipOut, mediaCsvFile);
		archiveService.putIntoArchive(zipOut, mediaScript);

		final List<String> fileNames = getMediaFileNames(linesToWrite);
		for (String fileName : fileNames)
		{
			LOGGER.debug(String.format("Putting file \"%s\" into media Archive", fileName));

			File mediaFile = getMediaFile(fileName);
			archiveService.putIntoArchive(zipOut, mediaFile);
			removeMediaFileFromRootDir(fileName);
		}
	}

	private void removeMediaFileFromRootDir(final String mediaName) throws IOException
	{
		LOGGER.debug("Removing tmp data " + mediaName);

		final File mediaFile = new File(UNZIPPED_CONTENT_FOLDER + FILE_SEPARATOR + mediaName);
		Files.delete(mediaFile.toPath());
	}

	private File getMediaFile(final String fileName)
	{
		return new File(UNZIPPED_CONTENT_FOLDER + FILE_SEPARATOR + fileName);
	}

	private List<String> getMediaFileNames(final List<String> listOfLinesToWrite)
	{
		final List<String> mediaFileNames = new ArrayList<>();
		final String linesToWrite = String.join(EOL, listOfLinesToWrite);

		final CSVReader reader = new CSVReader(linesToWrite);
		while (reader.readNextLine())
		{
			String fileName = reader.getLine().get(0);
			mediaFileNames.add(fileName);
		}
		return mediaFileNames;
	}

	@Required
	public void setArchiveService(ArchiveService archiveService)
	{
		this.archiveService = archiveService;
	}
}
