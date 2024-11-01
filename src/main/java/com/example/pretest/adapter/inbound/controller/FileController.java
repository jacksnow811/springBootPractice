package com.example.pretest.adapter.inbound.controller;

// java import
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.net.URLEncoder;
// spring import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.pretest.application.FileManagementUsecase;
import com.example.pretest.application.exceptions.EmptyFileException;
import com.example.pretest.application.exceptions.FileNotFoundException;
import com.example.pretest.application.exceptions.FileSizeExceededException;
import com.example.pretest.application.exceptions.FileStorageException;
import com.example.pretest.application.exceptions.UnsupportedFileTypeException;

// swagger import
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api")
public class FileController {
    @Value("${server.storage.directory}")
    private String STORAGE_DIRECTORY;
    private final FileManagementUsecase fileManagementUsecase;

    public FileController(FileManagementUsecase fileManagementUsecase) {
        this.fileManagementUsecase = fileManagementUsecase;
    }

    @Operation(summary = "Upload file API", description = "This API allows users to upload a file to the server. Only specific file types (Image and PDF) are allowed. The file size must not exceed 1000KB. A successful response includes the file's storage path, which can be used for future downloads.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            // 呼叫 FileManagementUsecase 上傳檔案
            String fileUrl = fileManagementUsecase.uploadFile(multipartFile);

            return ResponseEntity.ok("File uploaded successfully, and file_url: " + fileUrl);

        } catch (EmptyFileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (FileSizeExceededException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
        } catch (UnsupportedFileTypeException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(e.getMessage());
        } catch (FileStorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Download file API", description = "This API allows users to download a file from the server by providing the filename returned by the upload API. The file is served in its original format, with the correct MIME type set for download. This API supports UTF-8 encoded filenames, allowing files with non-ASCII characters (e.g., Chinese characters) to be downloaded correctly.")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filepath) {
        try {
            File file = fileManagementUsecase.downloadFile(filepath);
            Resource resource = new FileSystemResource(file);

            // Encode the filename in UTF-8 to handle Chinese
            String contentType = Files.probeContentType(file.toPath()); // Detects the file's MIME type
            String encodedFileName = URLEncoder.encode(resource.getFilename(), "UTF-8").replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}