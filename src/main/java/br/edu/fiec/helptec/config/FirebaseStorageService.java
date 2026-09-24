package br.edu.fiec.helptec.config;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    public String upload(MultipartFile file, String pasta) throws IOException {
        String nomeArquivo = pasta + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        Storage storage = StorageClient.getInstance().bucket().getStorage();
        String bucketName = StorageClient.getInstance().bucket().getName();

        BlobId blobId = BlobId.of(bucketName, nomeArquivo);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, nomeArquivo);
    }

    public void deletar(String url) {
        String bucketName = StorageClient.getInstance().bucket().getName();
        String caminhoArquivo = url.substring(url.indexOf(bucketName) + bucketName.length() + 1);

        Storage storage = StorageClient.getInstance().bucket().getStorage();
        storage.delete(BlobId.of(bucketName, caminhoArquivo));
    }
}