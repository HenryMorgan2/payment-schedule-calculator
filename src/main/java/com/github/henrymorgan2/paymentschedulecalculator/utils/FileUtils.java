package com.github.henrymorgan2.paymentschedulecalculator.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static String getFileAsString(String pathName) throws IOException {
        File file = new ClassPathResource(pathName).getFile();
        return org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8.toString());
    }
}
