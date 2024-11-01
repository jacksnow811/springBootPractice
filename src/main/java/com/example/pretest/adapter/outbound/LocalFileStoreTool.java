package com.example.pretest.adapter.outbound;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

import com.example.pretest.application.exceptions.FileNotFoundException;
import com.example.pretest.application.interfaces.IFileStoreTool;

public class LocalFileStoreTool implements IFileStoreTool {
    private final String storageDirectory;

    public LocalFileStoreTool(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    @Override
    public void storeFile(MultipartFile file, String filepath) throws IOException {
        Path storagePath = Paths.get(this.storageDirectory, filepath).getParent();
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        String destinationPath = this.storageDirectory + filepath;
        Path destination = Paths.get(destinationPath);
        file.transferTo(destination);
    }

    @Override
    public File readFile(String filepath) throws IOException {
        Path fullPath = Paths.get(storageDirectory, filepath).normalize();
        File file = fullPath.toFile();
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException("File not found or is not readable: " + filepath);
        }

        return file;
    }
}