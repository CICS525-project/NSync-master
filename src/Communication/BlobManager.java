package Communication;

import Controller.ServerProperties;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.LeaseState;
import com.microsoft.azure.storage.blob.LeaseStatus;
import com.microsoft.azure.storage.blob.ListBlobItem;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlobManager {

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

        String blobName = fullPath.substring(fullPath.indexOf("/") + 1);
        String containerName = fullPath.substring(0, fullPath.indexOf("/"));

        if (blobName.contains("\\")) {
            blobName = blobName.replace("\\", "/");
        }
        System.out.println("The blobname is " + blobName + " and the containerName is " + containerName);
        CloudBlob blob = null;
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

                blob = (CloudBlob) blobItem;
                blob.downloadAttributes();
                System.out.println("Blob name found is " + blob.getName());
                if (blob.getProperties().getLeaseState().equals(LeaseState.LEASED)) {
                    blob.breakLease(0);
                }
                blob.delete();
            }
        } catch (URISyntaxException | InvalidKeyException | StorageException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
                    null, ex);
            try {
                if (blob.getProperties().getLeaseState().equals(LeaseState.LEASED)) {
                    blob.breakLease(0);
                }
            } catch (StorageException ex1) {
                Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex1);
            }

        }
    }

    public synchronized static void deleteBlobContainer(String containerName) {
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
        String containerName = oldName.substring(0, oldName.indexOf("/"));
        oldName = oldName.substring(oldName.indexOf("/") + 1, oldName.length());
        newName = newName.substring(newName.indexOf("/") + 1, newName.length());
        System.out.println("The container name is " + containerName);
        CloudBlob oldBlob = null;
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(Connection.getStorageConnectionString(ServerProperties.serverId));
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient
                    .getContainerReference(containerName);
            System.out.println("The old path is " + oldName
                    + " and the new path is " + newName);
            oldBlob = container.getBlockBlobReference(oldName);
            CloudBlob newBlob = container.getBlockBlobReference(newName);

            String path = System.getProperty("user.home").replace("\\", "/") + "/" + oldName;
            //String path = System.getProperty("user.home").replace("\\", "/") + "/" + oldBlob.getName().substring(oldBlob.getName().lastIndexOf("/"));
            System.out.println("The path is " + path);
            File f = new File(path);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            oldBlob.downloadToFile(path);
            newBlob.uploadFromFile(path);//.startCopyFromBlob(oldBlob);
            if (oldBlob.getProperties().getLeaseState().equals(LeaseState.LEASED)) {
                    oldBlob.breakLease(0);
                }
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
        String containerName = oldName.substring(0, oldName.indexOf("/"));
        oldName = oldName.substring(oldName.indexOf("/") + 1, oldName.length());
        newName = newName.substring(newName.indexOf("/") + 1, newName.length());
        System.out.println("The container name is " + containerName);
        System.out.println("The new name is " + newName);
        CloudBlob oldBlob = null;
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
                oldBlob = container.getBlockBlobReference(oName);
                System.out.println("The blob names are " + blob.getName());
                String path = System.getProperty("user.home").replace("\\", "/") + "/" + oldName;
                File f = new File(path);
                if (!f.exists()) {
                     f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                oldBlob.downloadToFile(path);
                newBlob.uploadFromFile(path);//.startCopyFromBlob(oldBlob);
                if (oldBlob.getProperties().getLeaseState().equals(LeaseState.LEASED)) {
                    oldBlob.breakLease(0);
                }
                oldBlob.delete();
                f.delete();
            }
        } catch (URISyntaxException | InvalidKeyException | StorageException ex) {
            Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.out.println("The message of the exception is "
                    + ex.getMessage());
            try {
                if (oldBlob.getProperties().getLeaseState().equals(LeaseState.LEASED)) {
                    oldBlob.breakLease(0);
                }
            } catch (StorageException ex1) {
                Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
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
                         f.getParentFile().mkdirs();
                        f.createNewFile();
                        destBlob.uploadFromFile(path);
                        f.delete();
                    } catch (IOException ex) {
                        Logger.getLogger(BlobManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            String leaseId = "";          

            if (sourceBlob.exists()) {
                //System.out.println(sourceBlob.acquireLease(40, "ok", null, null, null));
                //destBlob.startCopyFromBlob(sourceBlob);

                String path = System.getProperty("user.home").replace("\\", "/") + "/" + sourceBlob.getName();
                System.out.println("The path is " + path);
                File f = new File(path);
                if (!f.exists()) {
                     f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                sourceBlob.downloadToFile(path);
                destBlob.uploadFromFile(path);//.startCopyFromBlob(oldBlob);
                //oldBlob.delete();
                f.delete();
            }
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

    public static String acquireLease(String blobName, String containerName,
            int serverId) {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount
                    .parse(Connection
                            .getStorageConnectionString(serverId));

            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient
                    .getContainerReference(containerName);
            CloudBlob b = container.getBlockBlobReference(blobName);

            if (b.exists()) {
                b.downloadAttributes();
                if (b.getProperties().getLeaseStatus().equals(LeaseStatus.LOCKED)) {
                    b.breakLease(5);
                }
                System.out.println("Acquring lease on " + b.getName() + " on server " + serverId);
                String leaseID = b.acquireLease(5, generateLeaseId());
                System.out.println("Acquring lease on " + b.getName() + " on server " + serverId);
                ServerProperties.leasedBlobs.put(b, new Date());
                return leaseID;
            } else {
                return "BlobDoesNotExist";
            }
        } catch (URISyntaxException | InvalidKeyException | StorageException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String generateLeaseId() {
        String uuid = UUID.randomUUID().toString();
        //System.out.println("uuid = " + uuid);
        return uuid;
    }

    public static void main(String[] args) {
        copyBlob("democontainer", "democontainer", "Kalimba.mp3", 1, 3);
    }
}
