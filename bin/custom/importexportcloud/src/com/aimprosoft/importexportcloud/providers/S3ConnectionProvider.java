package com.aimprosoft.importexportcloud.providers;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import software.amazon.awssdk.services.s3.S3Client;


public interface S3ConnectionProvider
{
	/**
	 * Creates S3 client.
	 *
	 * @param storageConfigData storage configuration DTO
	 * @return S3 client
	 */
	S3Client getS3Client(StorageConfigData storageConfigData);
}
