package Communication;

import Controller.ServerProperties;
import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CopyStatus;
import com.microsoft.azure.storage.blob.ListBlobItem;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.EnumSet;
import java.util.UUID;
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
                    .parse(Connection.getStorageConnectionString(ServerProperties.serverId));
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
                    .parse(Connection.getStorageConnectionString(ServerProperties.serverId));
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
                    .parse(Connection.getStorageConnectionString(ServerProperties.serverId));

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
        //if it has the last index of a dot then it is a file else it is a folder
        if (newName.lastIndexOf(".") == -1 && oldName.lastIndexOf(".") == -1) {
            System.out.println("Blob is a directory");
            renameBlobDir(oldName, newName);
        } else {
            //blob is a file
            renameSingleBlob(oldName, newName);
        }
    }

    private static void renameSingleBlob(String oldName, String newName) {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(Connection.getStorageConnectionString(ServerProperties.serverId));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient
                    .getContainerReference(containerName);
            System.out.println("The old path is " + url + oldName
                    + " and the new path is " + url + newName);
            CloudBlob oldBlob = container.getBlockBlobReference(url + oldName);
            CloudBlob newBlob = container.getBlockBlobReference(url + newName);

            String path = System.getProperty("user.dir") + "/" + oldBlob.getName();
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }
            oldBlob.downloadToFile(path);
            newBlob.uploadFromFile(path);//.startCopyFromBlob(oldBlob);
            oldBlob.delete();
            f.delete();
        } catch (URISyntaxException | InvalidKeyException | StorageException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void renameBlobDir(String oldName, String newName) {
        System.out.println("The new name is " + newName);
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(Connection.getStorageConnectionString(ServerProperties.serverId));

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
                String path = System.getProperty("user.dir") + "/" + oldBlob.getName();
                File f = new File(path);
                if (!f.exists()) {
                    f.createNewFile();
                }
                oldBlob.downloadToFile(path);
                newBlob.uploadFromFile(path);//.startCopyFromBlob(oldBlob);
                oldBlob.delete();
                f.delete();
            }
        } catch (URISyntaxException | InvalidKeyException | StorageException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.out.println("The message of the exception is "
                    + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void copyBlob(String srcContainerName, String destContainerName, String blobName, int sourceServer, int destServer) {

        CloudStorageAccount storageAccountSource = null;
        CloudStorageAccount storageAccountDest = null;
        try {
            storageAccountSource = CloudStorageAccount.parse(Connection.getStorageConnectionString(sourceServer));
            storageAccountDest = CloudStorageAccount.parse(Connection.getStorageConnectionString(destServer));
        } catch (URISyntaxException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        CloudBlobClient cloudBlobClients = null;
        CloudBlobClient cloudBlobClientd = null;
        cloudBlobClients = storageAccountSource.createCloudBlobClient();
        cloudBlobClientd = storageAccountDest.createCloudBlobClient();

        CloudBlobContainer srcContainer = null;
        CloudBlobContainer destContainer = null;

        try {
            destContainer = cloudBlobClientd
                    .getContainerReference(destContainerName);
            srcContainer = cloudBlobClients
                    .getContainerReference(srcContainerName);

            openContainer(destContainer);
            openContainer(srcContainer);

        } catch (URISyntaxException | StorageException e1) {
            e1.printStackTrace();
        }

        CloudBlob destBlob = null;
        CloudBlob sourceBlob = null;
        // get the SAS token to use for all blobs
        try {
            sourceBlob = srcContainer.getBlockBlobReference(blobName);
            destBlob = destContainer.getBlockBlobReference(blobName);

            System.out.println(destBlob + " " + destBlob.getName());
            if (!destBlob.exists()) {
                //copy would fail so I have to create the blob first and add some random data in it
                System.out.println("Blob does not exist ... creating the blob");
                String path = System.getProperty("user.dir") + "/helpme.txt";
                File f = new File(path);
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                        destBlob.uploadFromFile(path);
                        f.delete();
                    } catch (IOException ex) {
                        Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            String leaseId = "";
            while (true) {
                try {
                    leaseId = sourceBlob.acquireLease(60, "ddddddddddddddddddddddddddddddde");
                    break;
                } catch (Exception e) {
                    System.out.println("Trying to acquire lease on blob " + blobName);
                    e.printStackTrace();
                }
            }

            //System.out.println(sourceBlob.acquireLease(40, "ok", null, null, null));
            destBlob.startCopyFromBlob(sourceBlob);
            
            String path = System.getProperty("user.dir") + "/" + sourceBlob.getName();
            File f = new File(path);
            if(!f.exists()) {
                f.createNewFile();
            }
            sourceBlob.downloadToFile(path);
            destBlob.uploadFromFile(path);//.startCopyFromBlob(oldBlob);
            //oldBlob.delete();
            f.delete();
            

            //System.out.println(destBlob.getCopyState().getStatusDescription());

            closeContainer(srcContainer);
            closeContainer(destContainer);

        } catch (StorageException | URISyntaxException e) {
            //e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
        } catch (IOException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void closeContainer(CloudBlobContainer e) {
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
        // Include public access in the permissions object.
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.OFF);
        // Set the permissions on the container.
        try {
            e.uploadPermissions(containerPermissions);
        } catch (StorageException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private static void openContainer(CloudBlobContainer e) {
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
        // Include public access in the permissions object.
        containerPermissions
                .setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        // Set the permissions on the container.
        try {
            e.uploadPermissions(containerPermissions);
        } catch (StorageException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /*private String generateLeaseId() {
     String uuid = UUID.randomUUID().toString();
     //System.out.println("uuid = " + uuid);
     return uuid;
     } */
    public static void main(String[] args) {
        copyBlob("democontainer", "democontainer", "Kalimba.mp3", 1, 3);
    }
}
