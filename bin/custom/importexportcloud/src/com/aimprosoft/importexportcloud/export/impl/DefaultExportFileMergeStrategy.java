package com.aimprosoft.importexportcloud.export.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.ExportFileMergeStrategy;


public class DefaultExportFileMergeStrategy implements ExportFileMergeStrategy
{
	private static final Logger LOGGER = Logger.getLogger(DefaultExportFileMergeStrategy.class);

	@Override
	public Path merge(final Path mergedTarget, final Path sourceToMerge) throws ExportException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(mergedTarget, null);
				final ZipFile exportImpexAndCSVsZipFile = new ZipFile(sourceToMerge.toFile()))
		{
			final Enumeration<? extends ZipEntry> entries = exportImpexAndCSVsZipFile.entries();
			while (entries.hasMoreElements())
			{
				final ZipEntry entry = entries.nextElement();

				final Path fileToAppend = zipFileSystem.getPath(entry.getName());

				Files.copy(exportImpexAndCSVsZipFile.getInputStream(entry), fileToAppend);
			}
		}
		catch (final IOException e)
		{
			throw new ExportException("An error occurred while merging export zip files.", e);
		}

		LOGGER.info("importscript.impex and csv files have been added to export media zip file.");

		return mergedTarget;
	}
}
