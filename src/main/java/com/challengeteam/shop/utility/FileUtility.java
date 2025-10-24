package com.challengeteam.shop.utility;

import com.challengeteam.shop.exceptionHandling.exception.FileUtilityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class FileUtility {
    public static final String FILENAME_DELIMITER = "\\.";
    public static final String FILENAME_SEPARATOR = "_";

    public static String getFileExtension(MultipartFile file) throws FileUtilityException {
        Objects.requireNonNull(file, "file");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("Original filename is nul in supplied file");
            throw new FileUtilityException("File supplied without original filename");
        }

        String[] filenameParts = originalFilename
                .toLowerCase()
                .split(FILENAME_DELIMITER);
        return filenameParts[filenameParts.length - 1];
    }

    public static String getFilename(MultipartFile file) throws FileUtilityException {
        Objects.requireNonNull(file, "file");

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new FileUtilityException("File supplied without original filename");
        }

        return filename;
    }

    public static String getContentType(MultipartFile file) throws FileUtilityException {
        Objects.requireNonNull(file, "file");

        String contentType = file.getContentType();
        if (contentType == null) {
           throw new FileUtilityException("File supplied without content type");
        }

        return contentType;
    }

    public static String transformFilenameToUnique(String originalFilename) {
        Objects.requireNonNull(originalFilename, "originalFilename");

        String uniqueString = UUID.randomUUID().toString();
        return uniqueString + FILENAME_SEPARATOR + originalFilename;
    }

}
