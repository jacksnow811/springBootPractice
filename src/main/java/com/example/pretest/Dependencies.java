package com.example.pretest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.pretest.adapter.outbound.LocalFileStoreTool;
import com.example.pretest.application.FileManagementUsecase;

import java.util.Arrays;
import java.util.List;

@Configuration
public class Dependencies {

    @Value("${server.storage.directory}")
    private String storageDirectory;

    @Value("${server.file.max-size-kb}")
    private long maxFileSizeKB;

    @Value("${server.file.allowed-types}")
    private String allowedFileTypes;

    @Bean
    public FileManagementUsecase fileManagementUsecase() {
        LocalFileStoreTool fileStorageTool = new LocalFileStoreTool(storageDirectory);
        List<String> allowedTypesList = Arrays.asList(allowedFileTypes.split(","));
        return new FileManagementUsecase(maxFileSizeKB, allowedTypesList, fileStorageTool);
    }
}
