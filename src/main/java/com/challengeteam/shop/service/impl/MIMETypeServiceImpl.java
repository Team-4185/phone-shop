package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.FileUtilityException;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.persistence.repository.MIMETypeRepository;
import com.challengeteam.shop.service.MIMETypeService;
import com.challengeteam.shop.utility.FileUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class MIMETypeServiceImpl implements MIMETypeService {
    private final MIMETypeRepository mimeTypeRepository;

    @Transactional
    @Override
    public MIMEType createIfDoesntExist(MultipartFile file) {
        try {
            String fileExtension = FileUtility.getFileExtension(file);
            String contentType = FileUtility.getContentType(file);
            Supplier<MIMEType> createNewFunction = () -> createMIMEType(fileExtension, contentType);

            return mimeTypeRepository
                    .findByExtension(fileExtension)
                    .orElseGet(createNewFunction);
        } catch (FileUtilityException e) {
            log.warn("Failed to get MIMEType, because: {}", e.getMessage(), e);
            throw new InvalidAPIRequestException("Incorrect file type request", e);
        }
    }

    private MIMEType createMIMEType(String fileExtension, String contentType) {
        try {
            var newType = MIMEType
                    .builder()
                    .type(contentType)
                    .extension(fileExtension)
                    .build();

            log.debug("Create new MIMEType with file extension '{}' and content type '{}'", fileExtension, contentType);
            return mimeTypeRepository.save(newType);
        } catch (DataIntegrityViolationException e) {
            // If causes race condition when a few threads try to save the same MIMEType,
            // it throws DataIntegrityViolationException that says about attempt to save already existing typeContent
            // When it throws, just return this MIMEType
            var message = "Failed to retrieve MIME type after constraint violation for extension: " + fileExtension;
            log.warn(message, e);
            return mimeTypeRepository
                    .findByExtension(fileExtension)
                    .orElseThrow(() -> new CriticalSystemException(message, e));
        }
    }

}
