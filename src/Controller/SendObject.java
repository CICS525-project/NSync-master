package Controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Date;

public class SendObject implements Serializable {

    private String fileName;
    private String newFileName;  //used just in case of a Rename event
    private String filePath;    //relative filePath
    private EventType event;
    private boolean enteredIntoDB;
    private Date timeStamp;
    private boolean isAFolder;
    private String hash;
    private String ID;      //this is the file ID
    private String userID;
    private String sharedWith;

    private static final long serialVersionUID = 1L;

    public enum EventType {

        Create, Delete, Rename, Modify, Share
    }

    public SendObject() {
        enteredIntoDB = false;
        sharedWith = null;
        //this.setUserID(UserProperties.getUsername());
    }

    public SendObject(String fileName, String filePath, EventType event, Date timeStamp,
            boolean isAFolder, String newFileName, String hash, String userID) {
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.filePath = filePath;
        this.event = event;
        this.timeStamp = timeStamp;
        this.isAFolder = isAFolder;
        this.enteredIntoDB = false;
        this.setHash(hash);
        this.setUserID(userID);
        
        sharedWith = null;
        
        
    }

    public SendObject(String fileID, String fileName, String filePath, EventType event, Date timeStamp,
            boolean isAFolder, String newFileName, String hash, String userID) {
        this.ID = fileID;
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.filePath = filePath;
        this.event = event;
        this.timeStamp = timeStamp;
        this.isAFolder = isAFolder;
        this.enteredIntoDB = false;
        this.setHash(hash);
        this.setUserID(userID);
        
        sharedWith = null;
    }

    /**
     * @return the newFileName
     */
    public String getNewFileName() {
        return newFileName;
    }

    /**
     * @param newFileName the newFileName to set
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    /**
     * @return the isAFolder
     */
    public boolean isIsAFolder() {
        return isAFolder;
    }

    /**
     * @param isAFolder the isAFolder to set
     */
    public void setIsAFolder(boolean isAFolder) {
        this.isAFolder = isAFolder;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the event
     */
    public EventType getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(EventType event) {
        this.event = event;
    }

    /**
     * @return the enteredIntoDB
     */
    public boolean isEnteredIntoDB() {
        return enteredIntoDB;
    }

    /**
     * @param enteredIntoDB the enteredIntoDB to set
     */
    public void setEnteredIntoDB(boolean enteredIntoDB) {
        this.enteredIntoDB = enteredIntoDB;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }


    public void setHash(String hash) {
        this.hash = hash;
    }

   
    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return the sharedWith
     */
    public String getSharedWith() {
        return sharedWith;
    }

    /**
     * @param sharedWith the sharedWith to set
     */
    public void setSharedWith(String sharedWith) {
        this.sharedWith = sharedWith;
    }
}
