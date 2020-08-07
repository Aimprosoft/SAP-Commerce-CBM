package com.aimprosoft.importexportcloud.export.impl;

import java.io.IOException;
import java.io.InputStream;
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

	private static final String ERROR_MESSAGE = "An error occurred while merging export zip files.";

	@Override
	public Path merge(final Path exportedMediaFilePath, final Path serviceFilePath, final Path exportImpexAndCSVsFilePath)
			throws ExportException
	{
		Path resultPath;
		if (exportedMediaFilePath == null)
		{
			addServiceFileToArchive(serviceFilePath, exportImpexAndCSVsFilePath);
			resultPath = exportImpexAndCSVsFilePath;
		}
		else
		{
			try (final FileSystem targetFileToMerge = FileSystems.newFileSystem(exportedMediaFilePath, null))
			{
				mergeZip(targetFileToMerge, exportImpexAndCSVsFilePath);

				resultPath = exportedMediaFilePath;
			}
			catch (final IOException e)
			{
				throw new ExportException(ERROR_MESSAGE, e);
			}
			addServiceFileToArchive(serviceFilePath, exportedMediaFilePath);
		}

		LOGGER.info("importscript.impex,service file and csv files have been added to export media zip file.");

		return resultPath;
	}

	private void mergeZip(final FileSystem targetFileToMerge, final Path sourceToMergePath) throws IOException
	{
		try (final ZipFile sourceTargetZip = new ZipFile(sourceToMergePath.toFile()))
		{
			final Enumeration<? extends ZipEntry> entries = sourceTargetZip.entries();

			while (entries.hasMoreElements())
			{
				final ZipEntry entry = entries.nextElement();

				final Path fileToAppend = targetFileToMerge.getPath(entry.getName());

				try ( InputStream inputStream = sourceTargetZip.getInputStream(entry);)
				{
					Files.copy(inputStream, fileToAppend);
				}
			}
		}
	}

	private void addServiceFileToArchive(Path serviceFilePath, Path archiveDataPath) throws ExportException
	{
		try (FileSystem zipFileSystem = FileSystems.newFileSystem(archiveDataPath, null))
		{
			final String fileName = serviceFilePath.getFileName().toString();
			final Path fileToAppend = zipFileSystem.getPath(fileName);
			Files.copy(Files.newInputStream(serviceFilePath), fileToAppend);
		}
		catch (IOException e)
		{
			throw new ExportException(ERROR_MESSAGE, e);
		}
	}
}
