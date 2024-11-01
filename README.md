# springBootPractice - File Upload and Download API

## Start-up Instructions:
1. Please modify the `server.storage.directory` property in the configuration file (`src/main/resources/application.properties`) to point to a directory path on your computer. Other parameter descriptions are as follows:
   - `server.file.max-size-kb`: The size limit allowed for a single file upload (can be adjusted as needed).
   - `server.file.allowed-types`: Allowed MIME types for file uploads (can be adjusted as needed).
2. Run the Spring Boot project, then open Swagger UI at `http://localhost:8080/swagger-ui/index.html#/` to perform testing.