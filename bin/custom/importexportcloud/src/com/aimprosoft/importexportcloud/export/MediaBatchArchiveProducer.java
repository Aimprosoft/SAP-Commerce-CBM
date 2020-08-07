package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.exceptions.ExportException;

import java.io.IOException;
import java.util.List;


public interface MediaBatchArchiveProducer
{
	void produceArchive(List<String> linesToWrite, List<String> mediaImportScript, int archiveNumber)
			throws IOException, ExportException;
}
