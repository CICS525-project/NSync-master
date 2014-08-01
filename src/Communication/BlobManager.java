package Communication;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;
import java.io.File;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlobManager {
	// add the username to this string instead of default

	private static String containerName = "democontainer"; // User.getUsername();
	private static String url = "https://portalvhdsh8ghz0s9b7mx9.blob.core.windows.net/"
			+ containerName + "/";

	// remember to set container name back to user if you have to change it for
	// any reason
	public static void setContainerName(String newContainerName) {
		containerName = newContainerName;
	}

	public synchronized static void createContainter(String containerName) {
		containerName = containerName.toLowerCase();
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(Connection.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			container.createIfNotExists();
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public synchronized static void deleteBlob(String fullPath) {
		String blobName = fullPath;
		if (blobName.contains("\\")) {
			blobName = blobName.replace("\\", "/");
		}
		System.out.println("Blob is " + blobName);
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(Connection.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			EnumSet<BlobListingDetails> details = EnumSet
					.of(BlobListingDetails.METADATA);
			System.out.println("Blob name is " + blobName);
			for (ListBlobItem blobItem : container.listBlobs(blobName, true,
					details, null, null)) {

				CloudBlob blob = (CloudBlob) blobItem;
				System.out.println("Blob name found is " + blob.getName());
				blob.delete();
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public synchronized static void deleteBlobContainer() {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(Connection.getStorageConnectionString());

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			container.delete();
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public synchronized static void renameBlob(String newName, String oldName) {
		File file = new File(oldName);
		if (file.isDirectory()) {
			System.out.println("Blob is a directory");
			renameBlobDir(oldName, newName);
		}

		if (file.isFile()) {
			System.out.println("Blob is a file");
			renameSingleBlob(oldName, newName);
		}

	}

	private static void renameSingleBlob(String oldName, String newName) {		
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(Connection.getStorageConnectionString());
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			System.out.println("The old path is " + url + oldName
					+ " and the new path is " + url + newName);
			CloudBlob oldBlob = container.getBlockBlobReference(url + oldName);
			CloudBlob newBlob = container.getBlockBlobReference(url + newName);
			newBlob.startCopyFromBlob(oldBlob);
			oldBlob.delete();
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	private static void renameBlobDir(String oldName, String newName) {		
		System.out.println("The new name is " + newName);
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(Connection.getStorageConnectionString());

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient
					.getContainerReference(containerName);
			EnumSet<BlobListingDetails> details = EnumSet
					.of(BlobListingDetails.METADATA);
			for (ListBlobItem blobItem : container.listBlobs(oldName, true,
					details, null, null)) {
				CloudBlob blob = (CloudBlob) blobItem;
				String oName = blob.getName();
				String nName = newName + oName.substring(oldName.length());
				System.out.println("New name is " + nName);
				CloudBlob newBlob = container.getBlockBlobReference(nName);
				CloudBlob oldBlob = container.getBlockBlobReference(oName);
				System.out.println("The blob names are " + blob.getName());
				newBlob.startCopyFromBlob(oldBlob);
				oldBlob.delete();
			}
		} catch (URISyntaxException | InvalidKeyException | StorageException ex) {
			Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
					null, ex);
			System.out.println("The message of the exception is "
					+ ex.getMessage());
		}
	}
}
