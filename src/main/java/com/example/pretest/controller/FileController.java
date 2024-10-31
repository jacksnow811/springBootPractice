package com.example.pretest.controller;

// java import
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.net.URLEncoder;
// spring import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
// swagger import
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api")
public class FileController {
    private static final List<String> ALLOWED_FILE_TYPES = List.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAX_FILE_SIZE_KB = 1000;
    @Value("${server.storage.directory}")
    private String STORAGE_DIRECTORY;
    // private static final String STORAGE_DIRECTORY = "C:/Users/jack2/Desktop/Line
    // Interview/storage/";
    private static final String FILE_DIRECTORY = "file/";

    @Operation(summary = "Upload file API", description = "This API allows users to upload a file to the server. Only specific file types (Image and PDF) are allowed. The file size must not exceed 1000KB. A successful response includes the file's storage path, which can be used for future downloads.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        // Check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File is empty. Please select a file to upload.");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE_KB * 1024) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File size exceeds the maximum limit of 1000KB.");
        }

        // Validate file type
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only images (JPEG, PNG) and PDF files are allowed.");
        }

        try {
            // Create the storage directory if it does not exist
            Path storagePath = Paths.get(STORAGE_DIRECTORY);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // Save the file to the specified directory
            String filePath = STORAGE_DIRECTORY + FILE_DIRECTORY + file.getOriginalFilename();
            Path destination = Paths.get(filePath);
            file.transferTo(destination);

            return ResponseEntity
                    .ok("File uploaded successfully, and file_url: " + "/" + FILE_DIRECTORY
                            + file.getOriginalFilename());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while saving the file. Please try again later.");
        }
    }

    @Operation(summary = "Download file API", description = "This API allows users to download a file from the server by providing the filename returned by the upload API. The file is served in its original format, with the correct MIME type set for download. This API supports UTF-8 encoded filenames, allowing files with non-ASCII characters (e.g., Chinese characters) to be downloaded correctly.")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filepath) {
        try {
            Path filePath = Paths.get(STORAGE_DIRECTORY + filepath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // Check if the file exists and is readable
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Encode the filename in UTF-8 to handle Chinese
            String contentType = Files.probeContentType(filePath); // Detects the file's MIME type
            String encodedFileName = URLEncoder.encode(resource.getFilename(), "UTF-8").replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // @Operation(summary = "打招呼 API", description = "通過傳入的名字，返回個性化的問候語。如果未提供名字，則默認為
    // 'World'。", responses = {
    // @ApiResponse(responseCode = "200", description = "成功返回問候語"),
    // @ApiResponse(responseCode = "400", description = "請求錯誤")
    // })
    // @GetMapping("/hello")
    // public String sayHello(@RequestParam(value = "name", defaultValue = "World")
    // String name) {
    // return "Hello, " + name + "!";
    // }
}