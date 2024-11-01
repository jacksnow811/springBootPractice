package com.example.pretest.application;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.example.pretest.application.exceptions.EmptyFileException;
import com.example.pretest.application.exceptions.FileSizeExceededException;
import com.example.pretest.application.exceptions.FileStorageException;
import com.example.pretest.application.exceptions.UnsupportedFileTypeException;
import com.example.pretest.application.interfaces.IFileStoreTool;

public class FileManagementUsecase {
    private final long maxFileSizeKB;
    private final List<String> allowedFileTypes;
    private final IFileStoreTool fileStorageTool;

    public FileManagementUsecase(long maxFileSizeKB, List<String> allowedFileTypes, IFileStoreTool fileStorageTool) {
        this.maxFileSizeKB = maxFileSizeKB;
        this.allowedFileTypes = allowedFileTypes;
        this.fileStorageTool = fileStorageTool;
    }

    public String uploadFile(MultipartFile file) {
        validateFile(file);
        return storeFile(file);
    }

    public File downloadFile(String filepath) throws IOException {
        return this.fileStorageTool.readFile(filepath);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("File is empty. Please select a file to upload.");
        }
        if (file.getSize() > this.maxFileSizeKB * 1024) {
            throw new FileSizeExceededException(
                    String.format("File size exceeds the maximum limit of %dKB.", this.maxFileSizeKB));
        }
        if (!this.allowedFileTypes.contains(file.getContentType())) {
            throw new UnsupportedFileTypeException(
                    String.format("Only the following file types are allowed: %s.", this.allowedFileTypes));
        }
    }

    private String storeFile(MultipartFile file) {
        try {
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String filePath = "/" + currentDate + "/" + file.getOriginalFilename();
            this.fileStorageTool.storeFile(file, filePath);
            return filePath;
        } catch (IOException e) {
            throw new FileStorageException("An error occurred while saving the file.", e);
        }
    }
}