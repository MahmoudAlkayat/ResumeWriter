package ninjas.cs490Project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final String UPLOAD_DIR = "uploads/";

    public String storeFile(MultipartFile file) throws IOException {
        logger.info("Starting to store file: {}", file.getOriginalFilename());
        try {
            // Create the uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                logger.info("Upload directory does not exist. Creating directory at: {}", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String fileExtension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            logger.info("Saving file as: {}", filePath.toAbsolutePath());
            // Save the file
            file.transferTo(filePath.toFile());
            logger.info("File stored successfully with unique filename: {}", uniqueFilename);

            return uniqueFilename;
        } catch (IOException e) {
            logger.error("Error storing file: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }
}
