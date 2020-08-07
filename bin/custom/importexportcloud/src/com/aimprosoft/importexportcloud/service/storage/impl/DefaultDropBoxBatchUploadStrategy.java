
package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.service.storage.DropBoxBatchUploadStrategy;
import com.dropbox.core.DbxException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class DefaultDropBoxBatchUploadStrategy implements DropBoxBatchUploadStrategy
{
	private static final Logger LOGGER = Logger.getLogger(DefaultDropBoxBatchUploadStrategy.class);

	// Adjust the chunk size based on your network speed and reliability. Larger chunk sizes will
	// result in fewer network requests, which will be faster. But if an error occurs, the entire
	// chunk will be lost and have to be re-uploaded. Use a multiple of 4MiB for your chunk size.
	static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
	private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;

	@Override
	public void uploadBatch(DbxClientV2 dbxClient, String dropboxPath, File localFile) throws DbxException, IOException
	{
		// Using approach from the DropBox GitHub repository
		long uploaded = 0L;
		DbxException thrown = null;
		String sessionId = null;
		for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i)
		{
			if (i > 0)
			{
				LOGGER.warn(String.format("Retrying chunked upload (%d / %d attempts)", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS));
			}

			try (InputStream in = new FileInputStream(localFile))
			{
				long size = localFile.length();
				// if this is a retry, make sure seek to the correct offset
				in.skip(uploaded);

				if (sessionId == null)
				{
					sessionId = dbxClient.files().uploadSessionStart()
							.uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE)
							.getSessionId();
					uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
					printProgress(uploaded, size);
				}
				Pair<Long, UploadSessionCursor> batchResult = processBatch(in, dbxClient, uploaded, sessionId, size);
				uploaded = batchResult.getLeft();

				finishUpload(in, dbxClient, dropboxPath, localFile, uploaded, size, batchResult);

				return;
			}
			catch (RetryException ex)
			{
				thrown = ex;
				// RetryExceptions are never automatically retried by the client for uploads. Must
				// catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
				sleepQuietly(ex.getBackoffMillis());
				continue;
			}
			catch (NetworkIOException ex)
			{
				thrown = ex;
				// network issue with Dropbox (maybe a timeout?) try again
				LOGGER.error("Network issue uploading to Dropbox: " + ex.getMessage());
				continue;
			}
		}
		// if we made it here, then we must have run out of attempts
		LOGGER.error("Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage());
	}

	private Pair<Long, UploadSessionCursor> processBatch(InputStream in, DbxClientV2 dbxClient, long uploaded, String sessionId, long size)
			throws IOException, DbxException
	{
		UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);
		// (2) Append
		while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE)
		{
			dbxClient.files().uploadSessionAppendV2(cursor)
					.uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE);
			uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
			printProgress(uploaded, size);
			cursor = new UploadSessionCursor(sessionId, uploaded);
		}
		return Pair.of(uploaded, cursor);
	}

	private void finishUpload(InputStream in, DbxClientV2 dbxClient, String dropboxPath, File localFile, long uploaded, long size,
			Pair <Long, UploadSessionCursor> result) throws DbxException, IOException
	{
		UploadSessionCursor cursor = result.getRight();
		// (3) Finish
		long remaining = size - uploaded;
		CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath)
				.withMode(WriteMode.ADD)
				.withClientModified(new Date(localFile.lastModified()))
				.build();

		FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
				.uploadAndFinish(in, remaining);

		LOGGER.info(String.format("File %s was uploaded to DropBox succefuly. Metadata: %s", localFile.getName(), metadata.toStringMultiline()));
	}

	private static void printProgress(long uploaded, long size)
	{
		LOGGER.debug(String.format("Uploaded %12d / %12d bytes (%5.2f%%)", uploaded, size, 100 * (uploaded / (double) size)));
	}

	private void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			LOGGER.error("Error uploading to Dropbox: interrupted during backoff.");
		}
	}
}
