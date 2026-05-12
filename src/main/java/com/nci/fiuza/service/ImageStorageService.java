package com.nci.fiuza.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

//makes this class aa spring service, so it can be injected into other classes
@Service
public class ImageStorageService {

    //reads the app.upload.dir, or uses "uploads" if not configured
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    //defines allows image types
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    //method received the uploaded file and subfolder where it should be saved
    public String saveImage(MultipartFile imageFile, String subFolder) throws IOException {

        //if no file selected there is nothing to save
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        //check allowed types
        if (!ALLOWED_TYPES.contains(imageFile.getContentType())) {
            throw new IllegalArgumentException("image.message.invalidType");
        }

        //get the original file name uploaded by the user
        String originalFileName = imageFile.getOriginalFilename();

        //get the extension, if it exists
        String extension = "";
        if(originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        //created file name with a random id such as "75dd53be-9eac-4fa7-a658-ebb84abcf8bb"
        String fileName = UUID.randomUUID() + extension;

        //builds the full upload path, for example "uploads/products"
        Path uploadPath = Paths.get(uploadDir, subFolder);

        //created the folder if it does not exist yet
        Files.createDirectories(uploadPath);

        //builds the final file path, for example "uploads/products/75dd53be-9eac-4fa7-a658-ebb84abcf8bb.png"
        Path filePath = uploadPath.resolve(fileName);

        //copies the uploaded image content into the final file path
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        //returns the file name to be stored on the db
        return fileName;

    }

}
