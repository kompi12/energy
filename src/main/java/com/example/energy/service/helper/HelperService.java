package com.example.energy.service.helper;

import com.example.energy.service.export.ExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class HelperService {
    private static final Logger logger = LoggerFactory.getLogger(HelperService.class);

    public void storeCsvLocally(String fileName, byte[] data) {
        try {
            File outputDir = new File("exports");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(data);
            }

            logger.info("Saved CSV locally: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to store CSV locally", e);
        }
    }
}
