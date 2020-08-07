package com.aimprosoft.importexportcloud.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;


public interface ArchiveService
{
	void putIntoArchive(ZipOutputStream zipOut, File file) throws IOException;

	void unzipArchive(Path archivePath, String pathToUnzip) throws IOException;

	void createArchive(Path archivePath, String directoryFrom) throws IOException;
}
