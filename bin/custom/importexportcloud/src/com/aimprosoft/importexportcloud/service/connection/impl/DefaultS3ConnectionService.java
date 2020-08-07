package com.aimprosoft.importexportcloud.service.connection.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.providers.S3ConnectionProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.core.Protocol;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.time.Instant;


public class DefaultS3ConnectionService extends AbstractConnectionService
{
	private static final Logger LOGGER = Logger.getLogger(DefaultS3ConnectionService.class);

	private S3ConnectionProvider s3ConnectionProvider;

	@Override
	public void connect(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		try (final S3Client s3Client = getS3Client(storageConfigData))
		{
			checkConnection(s3Client);

			getStorageConfigService().setConnectionStatus(storageConfigData.getCode(), Boolean.TRUE);
		}
	}

	/*
	 * 'revokeToken' method is used for disconnecting AWS S3 account.
	 *	 This part of functionality implemented in 'revokeToken' for unification 'Disconnect' button functionality.
	 */
	@Override
	public void revokeToken(final StorageConfigData storageConfigData)
	{
		storageConfigData.setIsConnected(Boolean.FALSE);
		getStorageConfigService().setConnectionStatus(storageConfigData.getCode(), Boolean.FALSE);
	}

	@Override
	public void checkAccessToken(final StorageConfigData storageConfigData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthURL(final StorageConfigData storageConfigData, final HttpSession httpSession)
	{
		throw new UnsupportedOperationException("");
	}

	@Override
	public String obtainPublicURL(final StorageConfigData storageConfig, final String location)
			throws CloudStorageException
	{
		final boolean useSignedUrl = storageConfig.isUseSignedUrls();

		if (BooleanUtils.isFalse(useSignedUrl))
		{
			final S3Utilities utilities = getS3Client(storageConfig).utilities();

			final GetUrlRequest getUrlRequest = GetUrlRequest.builder()
					.bucket(storageConfig.getBucketName())
					.region(Region.of(storageConfig.getRegion()))
					.key(location)
					.build();

			return utilities.getUrl(getUrlRequest).toString();
		}

		final Aws4PresignerParams presignedParams = buildPresignedRequstParams(storageConfig);

		final SdkHttpFullRequest presignRequest = buildPresignRequest(storageConfig, location);

		final SdkHttpFullRequest presignRequestResult = AwsS3V4Signer.create()
				.presign(presignRequest, presignedParams);

		try
		{
			return presignRequestResult.getUri()
					.toURL()
					.toString();
		}
		catch (MalformedURLException e)
		{
			throw new CloudStorageException("Malformed presign URL: ", e);
		}
	}

	private static SdkHttpFullRequest buildPresignRequest(final StorageConfigData storageConfig, final String location)
	{
		return SdkHttpFullRequest.builder()
				.encodedPath("/" + location)
				.host(storageConfig.getBucketName() + ".s3." + storageConfig.getRegion() + ".amazonaws.com")
				.method(SdkHttpMethod.GET)
				.protocol(Protocol.HTTPS.toString())
				.build();
	}

	private Aws4PresignerParams buildPresignedRequstParams(final StorageConfigData storageConfig)
	{
		final Region region = Region.of(storageConfig.getRegion());

		return Aws4PresignerParams.builder()
				.expirationTime(Instant.now().plus(getSignedUrlDuration()))
				.awsCredentials(getConfiguredCredentialProvider(storageConfig).resolveCredentials())
				.signingName("s3")
				.signingRegion(region)
				.build();
	}

	private static AwsCredentialsProvider getConfiguredCredentialProvider(final StorageConfigData data)
	{
		final AwsBasicCredentials basicCredentials = AwsBasicCredentials
				.create(data.getAppKey(), data.getEncodedAppSecret());

		return StaticCredentialsProvider.create(basicCredentials);
	}

	protected S3Client getS3Client(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		return s3ConnectionProvider.getS3Client(storageConfigData);
	}

	private void checkConnection(final S3Client s3Client) throws CloudStorageException
	{
		final ListBucketsResponse listBucketsResponse;

		try
		{
			listBucketsResponse = s3Client.listBuckets();
		}
		catch (final SdkException e)
		{
			throw new CloudStorageException("Connection failed.", e);
		}

		if (CollectionUtils.isEmpty(listBucketsResponse.buckets()))
		{
			throw new CloudStorageException("Buckets have not found.");
		}

		LOGGER.info("Connection to Amazon has been checked.");
	}

	@Required
	public void setS3ConnectionProvider(final S3ConnectionProvider s3ConnectionProvider)
	{
		this.s3ConnectionProvider = s3ConnectionProvider;
	}
}
