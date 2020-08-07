package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.exceptions.ExportException;

import java.nio.file.Path;


public interface ExportFileMergeStrategy
{
	/**
	 * Appends all the files from source zip file to target zip file.
	 *
	 * @param exportedMediaFilePath - path to archive with media content
	 * @param serviceFilePath       - path to serviceFilePath
	 * @param originalArchive       zip file to which append
	 * @return {@code mergedTarget}
	 * @throws ExportException if error occurs while merging
	 */
	Path merge(Path exportedMediaFilePath, Path serviceFilePath, Path originalArchive) throws ExportException;
}
