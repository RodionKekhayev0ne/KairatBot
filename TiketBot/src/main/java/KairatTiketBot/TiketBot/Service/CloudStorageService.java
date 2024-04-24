package KairatTiketBot.TiketBot.Service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public class CloudStorageService {


    private  Storage storage;

    public CloudStorageService(){}
    public CloudStorageService(Storage storage) {
        this.storage = storage;
    }

    public void uploadFile(String bucketName, String fileName, byte[] data) {
        storage.create(BlobInfo.newBuilder(bucketName, fileName).build(), data);
    }
    public void deleteFile(String bucketName, String fileName) {
        storage.delete(bucketName, fileName);
    }

    public Storage getStorage(){
        return storage;
    }
}