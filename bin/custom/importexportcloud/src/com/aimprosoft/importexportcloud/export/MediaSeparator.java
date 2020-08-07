package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.exceptions.ExportException;

import java.nio.file.Path;

public interface MediaSeparator
{
	void separateMedia(Path exportImpexAndCSVsFilePath) throws ExportException;
}
