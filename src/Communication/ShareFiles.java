package Communication;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class ShareFiles {
	
	  private static final String SharedAccessPermissions = null;

	public static void main(String[] args) throws InvalidKeyException, 
	     URISyntaxException, StorageException 
	  {
		  final String storageConnectionString = "DefaultEndpointsProtocol=http;"
					+ "AccountName=portalvhdsh8ghz0s9b7mx9;"
					+ "AccountKey=ThVIUXcwpsYqcx08mtIhRf6+XxvEuimK35/M65X+XlkdVCQNl4ViUiB/+tz/nq+eeZAEZCNrmFVQwwN3QiykvA==";
	//  final String storageConnectionString = creds.getstorageconnectionstring();
	  CloudStorageAccount storageAccount = 
	     CloudStorageAccount.parse(storageConnectionString);
	  CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
	  CloudBlobContainer container = blobClient.getContainerReference("yanki");
	// Generate shared access signature on blob
	// Define the start and end time to granting permissions.
	Calendar cal = Calendar.getInstance();
	cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	 
	// Define the start and end time to grant permissions.
	// To handle clock skew set start time 5 min early and
	// expiry time 5 min later. So actual duration to be specified
	// for SAS is between 1hr 5min and 1hr 35 min from now.
	//cal.add(Calendar.DST_OFFSET, 7);
	
	//cal.setTimeZone(TimeZone.getTimeZone("UTC"));
	cal.add(Calendar.HOUR, -7);
	Date sharedAccessStartTime = cal.getTime();
	
	//cal.add(Calendar.MINUTE, 0);
	
	cal.add(Calendar.HOUR, 1);
	Date sharedAccessExpiryTime = cal.getTime();
	
	System.out.println(sharedAccessStartTime +  sharedAccessExpiryTime.toString());
	                                               
	// Define shared access policy
	SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
	EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.READ);
	policy.setPermissions(perEnumSet);
	policy.setSharedAccessExpiryTime(sharedAccessExpiryTime);
	policy.setSharedAccessStartTime(sharedAccessStartTime);
	CloudBlob blob = container.getBlockBlobReference("DSC01266.JPG");       
	//System.out.println(blob.generateSharedAccessSignature(policy, "myPolicy"));
	//Generating Shared Access Signature
        
	String sharedUri = blob.generateSharedAccessSignature(policy, "myPolicy");
	System.out.println(blob.getStorageUri() + "?" + sharedUri);
	
	  }
}
