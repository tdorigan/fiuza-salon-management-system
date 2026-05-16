package com.nci.fiuza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

//makes this class aa spring service, so it can be injected into other classes
//generic service responsible for image upload, URL creation, existence check and deletion in Cloudflare R2
@Service
public class ImageStorageService {

    //reusable S3 client injected by Spring
    private final S3Client s3Client;

    //constructor injection for the reusable S3 client
    public ImageStorageService(S3Client s3Client) {

        //stores the injected S3 client in this service
        this.s3Client = s3Client;
    }

    //reading the values from application.properties:

    //reads the Cloudflare R2 bucket name from environment variables/application.properties
    @Value("${cloudflare.r2.bucket-name:}")
    private String r2BucketName;

    //reads the Cloudflare R2 public URL used by the browser to display images
    @Value("${cloudflare.r2.public-url:}")
    private String r2PublicUrl;

    //defines if the configured public URL already points directly to the bucket
    @Value("${cloudflare.r2.public-url-includes-bucket:false}")
    private boolean r2PublicUrlIncludesBucket;

    //image max size 5MB
    @Value("${app.upload.max-image-size-bytes:5242880}")
    private long maxImageSizeBytes;

    //defines allows image types
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    //saves an uploaded image into Cloudflare R2 and returns the image key to be stored in the database
    public String saveImageToR2(MultipartFile imageFile, String subFolder) throws IOException {

        //if no file was selected, there is nothing to save
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        //validates image size before uploading
        if (imageFile.getSize() > maxImageSizeBytes) {
            throw new IllegalArgumentException("image.message.fileTooLarge");
        }

        //validates image type before uploading
        if (!ALLOWED_TYPES.contains(imageFile.getContentType())) {
            throw new IllegalArgumentException("image.message.invalidType");
        }

        //gets the original filename uploaded by the user
        String originalFileName = imageFile.getOriginalFilename();

        //starts with empty extension in case the original filename has no extension
        String extension = "";

        //extracts the original file extension when it exists
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        //creates a unique filename to avoid conflicts between uploaded images
        String fileName = UUID.randomUUID() + extension;

        //creates the R2 object key, for example products/abc.jpg or services/abc.jpg
        String imageReference = subFolder + "/" + fileName;

        //creates the upload request with bucket, image reference and content type
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2BucketName)
                .key(imageReference)
                .contentType(imageFile.getContentType())
                .build();

        //uploads the file content to Cloudflare R2
        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize())
        );

        //returns the image key to be stored in the database
        return imageReference;

    }

    //builds the public URL used by the browser to display an R2 image
    public String buildR2ImageUrl(String imageReference) {

        //if the database has no image reference, there is no URL to build
        if (imageReference == null || imageReference.isBlank()) {
            return null;
        }

        //removes a final slash from the configured public URL to avoid double slashes
        String basePublicUrl = removeTrailingSlash(r2PublicUrl);

        //if the public URL already points directly to the bucket, append only the object key
        if (r2PublicUrlIncludesBucket) {
            return basePublicUrl + "/" + imageReference;
        }

        //otherwise append the bucket name and then the object key
        return basePublicUrl + "/" + r2BucketName + "/" + imageReference;

    }

    //checks if an image exists in Cloudflare R2
    public boolean r2ImageExists(String imageReference) {

        //if the database has no image reference, the image does not exist
        if (imageReference == null || imageReference.isBlank()) {
            return false;
        }

        //creates a request to check only the object metadata, without downloading the image
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(r2BucketName)
                .key(imageReference)
                .build();

        try{
            //asks R2 if the object exists
            s3Client.headObject(headObjectRequest);

            //if no exception happens, the image exists
            return true;

        } catch (NoSuchKeyException e) {

            //if R2 says the key does not exist, return false
            return false;

        } catch (Exception e) {

            //for safety, if R2 cannot be checked, return false so the placeholder is shown
            return false;

        }

    }

    //deletes an image from Cloudflare R2
    public void deleteImageFromR2(String imageReference) {

        //if the database has no image reference, there is nothing to delete
        if (imageReference == null || imageReference.isBlank()) {
            return;
        }

        //creates the delete request with bucket and image reference
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(r2BucketName)
                .key(imageReference)
                .build();

        //sends the delete request to Cloudflare R2
        s3Client.deleteObject(deleteObjectRequest);

    }

    //removes a final slash from a URL to avoid double slashes when building image URLs
    private String removeTrailingSlash(String value) {

        //if the value ends with slash, remove the final slash
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        //otherwise return the value unchanged
        return value;

    }


}
