package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.service.ArchiveService;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class DefaultArchiveService implements ArchiveService
{
	private static final Logger LOGGER = Logger.getLogger(DefaultArchiveService.class);

	public void putIntoArchive(final ZipOutputStream zipOut, final File file) throws IOException
	{
		LOGGER.debug("Putting entry into archive");

		try (final FileInputStream fileInputStream = new FileInputStream(file))
		{
			final ZipEntry zipEntry = new ZipEntry(file.getName());
			zipOut.putNextEntry(zipEntry);

			final byte[] bytes = new byte[1024];
			int length;
			while ((length = fileInputStream.read(bytes)) >= 0)
			{
				zipOut.write(bytes, 0, length);
			}
		}
	}

	public void unzipArchive(final Path archivePath, final String pathToUnzip) throws IOException
	{
		LOGGER.debug("Unzipping the original archive");

		final File unzippedContent = new File(pathToUnzip);
		if (unzippedContent.mkdir())
		{
			final Path unzippedPath = unzippedContent.toPath();

			try (ZipFile archive = new ZipFile(archivePath.toString()))
			{
				final Enumeration<? extends ZipEntry> zipEntries = archive.entries();

				while (zipEntries.hasMoreElements())
				{
					final ZipEntry entry = zipEntries.nextElement();
					final Path fileToCreate = unzippedPath.resolve(entry.getName());
					Files.copy(archive.getInputStream(entry), fileToCreate, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	public void createArchive(final Path pathOfNewArchive, final String directoryFrom) throws IOException
	{
		LOGGER.debug("Creating final archive");

		try (FileOutputStream fileOutputStream = new FileOutputStream(pathOfNewArchive.toString());
			  ZipOutputStream zipOut = new ZipOutputStream(fileOutputStream))
		{
			List<File> files = Files.walk(Paths.get(directoryFrom))
					.map(Path::toFile)
					.collect(Collectors.toList());

			for (File file : files)
			{
				if (!file.isDirectory())
				{
					putIntoArchive(zipOut, file);
				}
			}
		}
	}
}
