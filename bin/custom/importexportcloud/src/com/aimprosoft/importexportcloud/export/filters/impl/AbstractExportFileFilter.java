package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;


public abstract class AbstractExportFileFilter implements ExportFileFilter
{
	private static final String HYBRIS_TEMP_DIR = "HYBRIS_TEMP_DIR";
	private static final Logger LOG = Logger.getLogger(AbstractExportFileFilter.class);

	@Override
	public abstract void filter(ExportTaskInfoModel exportTaskInfoModel, Path exportImpexAndCSVsFilePath) throws ExportException;

	protected void copyIntoTargetZip(List<String> filteredLines, Path fileToCopyPath) throws IOException
	{
		final String tmpDirectoryPath = System.getProperty(HYBRIS_TEMP_DIR);
		final Path tempDirectory = Paths.get(tmpDirectoryPath);
		final Path tempFile = Files.createTempFile(tempDirectory, fileToCopyPath.getFileName().toString(), "temp");

		Files.write(tempFile, filteredLines, StandardOpenOption.CREATE);

		Files.copy(tempFile, fileToCopyPath, StandardCopyOption.REPLACE_EXISTING);

		Files.delete(tempFile);

		logDebug(LOG, "Temporary file has been created %s with pass %s. It has been copied to %s",
				tempFile.getFileName().toString(), tempFile.toAbsolutePath().toString(), fileToCopyPath);
	}

	protected List<String> filterFileLines(Path filePath, Predicate<String> predicate) throws IOException
	{
		final List<String> filteredLines = new ArrayList<>();
		try (Stream<String> streamOfLines = Files.lines(filePath);)
		{
			final List<String> lines = streamOfLines.collect(Collectors.toList());

			filteredLines.add(lines.get(0));   //this is header of impex file

			final List<String> filteredLinesByCriteria = lines.stream()
					.filter(line -> predicate.test(line))
					.collect(Collectors.toList());

			filteredLines.addAll(filteredLinesByCriteria);
		}
		return filteredLines;
	}

	protected void logDebug(final Logger logger, final String message, Object... args)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(String.format(message, args));
		}
	}
}
