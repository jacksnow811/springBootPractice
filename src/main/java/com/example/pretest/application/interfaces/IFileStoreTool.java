package com.example.pretest.application.interfaces;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStoreTool {
    void storeFile(MultipartFile file, String filepath) throws IOException;

    File readFile(String filepath) throws IOException;
}
