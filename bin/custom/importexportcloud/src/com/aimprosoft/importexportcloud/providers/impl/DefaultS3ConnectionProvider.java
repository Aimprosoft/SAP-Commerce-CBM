package com.aimprosoft.importexportcloud.providers.impl;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.providers.S3ConnectionProvider;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


public class DefaultS3ConnectionProvider implements S3ConnectionProvider
{
	@Override
	public S3Client getS3Client(final StorageConfigData storageConfigData)
	{
		final AwsBasicCredentials basicCredentials = AwsBasicCredentials
				.create(storageConfigData.getAppKey(), storageConfigData.getEncodedAppSecret());
		final Region region = Region.of(storageConfigData.getRegion());
		final StaticCredentialsProvider provider = StaticCredentialsProvider.create(basicCredentials);

		return S3Client.builder().credentialsProvider(provider).region(region).build();
	}
}
