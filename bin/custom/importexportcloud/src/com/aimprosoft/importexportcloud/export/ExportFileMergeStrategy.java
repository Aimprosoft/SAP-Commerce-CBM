package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.exceptions.ExportException;

import java.nio.file.Path;


public interface ExportFileMergeStrategy
{
	/**
	 * Appends all the files from source zip file to target zip file.
	 *
	 * @param mergedTarget  zip file to append files
	 * @param sourceToMerge zip file to get files to append from
	 * @return {@code mergedTarget}
	 * @throws ExportException if error occurs while merging
	 */
	Path merge(Path mergedTarget, Path sourceToMerge) throws ExportException;
}
