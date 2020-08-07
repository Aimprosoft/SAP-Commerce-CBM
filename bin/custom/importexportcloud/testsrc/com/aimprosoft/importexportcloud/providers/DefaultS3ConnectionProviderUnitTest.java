package com.aimprosoft.importexportcloud.providers;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.providers.impl.DefaultS3ConnectionProvider;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.Assert.assertNotNull;


@UnitTest
public class DefaultS3ConnectionProviderUnitTest
{
	private DefaultS3ConnectionProvider defaultS3ConnectionProvider;

	private StorageConfigData storageConfigData;
	private StorageTypeData storageTypeData;

	@Before
	public void setUp()
	{
		defaultS3ConnectionProvider = new DefaultS3ConnectionProvider();
		storageTypeData = new StorageTypeData();
		storageTypeData.setCode("testTypeCode");

		storageConfigData = new StorageConfigData();
		storageConfigData.setCode("testConfigDataCode");
		storageConfigData.setAccessToken("testAccessToken");
		storageConfigData.setAppKey("testAppKey");
		storageConfigData.setEncodedAppSecret("testEncodedAppSecret");
		storageConfigData.setAuthCode("testAuthCode");
		storageConfigData.setBucketName("test");
		storageConfigData.setRegion("testRegion");
		storageConfigData.setStorageTypeData(storageTypeData);
	}

	@Test
	public void testGetS3Client()
	{
		final S3Client s3Client = defaultS3ConnectionProvider.getS3Client(storageConfigData);
		assertNotNull(s3Client);
	}
}
