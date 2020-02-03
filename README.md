
## Install guide for using Cloud Commerce CBM v.1.0

Your project must use Hybris Commerce 18.08

1. Download source code of the CBM project from GitHub and copy two extensions from /bin/custom directory to your hybris project into /bin/custom folder

2. Add to your localextensions.xml importexportcloud and importexportbackoffice extensions. 

3. Optionally redefine in your local.properties:

- The custom DropBox client identifier (default AimprosoftHybrisPlugin/0.1.0)
dbx.request.config.client.identifier

- Custom name of Stage catalog (default Staged)
staged.catalog.version.name

- Custom name of Online catalog (default Online)
online.catalog.version.name

- Your project redirect URL with your domain name (i.e. https://your-project.com/backoffice/parseDropboxAuthCode)
cloud.storage.dropBoxStorageType.redirectUrl		

4. Run ant clean all.

5. Update system from hac.

6. Module is ready.
