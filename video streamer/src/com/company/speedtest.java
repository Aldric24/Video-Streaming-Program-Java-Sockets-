package com.company;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class speedtest {
    private static final int BUFFER_SIZE = 4096; // Buffer size for reading data

    public static double calculateDownloadSpeed(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            long startTime = System.currentTimeMillis();
            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream("tempfile")) {

                byte[] dataBuffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            double downloadSpeed = calculateSpeedInKbps("tempfile", elapsedTime);
            return downloadSpeed;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 in case of an error
    }

    private static double calculateSpeedInKbps(String filePath, long elapsedTimeInMillis) {
        long fileSizeInBytes = getFileSize(filePath);
        double fileSizeInBits = fileSizeInBytes * 8;
        double elapsedTimeInSeconds = elapsedTimeInMillis / 1000.0;
        return (fileSizeInBits / (elapsedTimeInSeconds * 1000));
    }

    private static long getFileSize(String filePath) {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(filePath);
            return java.nio.file.Files.size(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        //String fileUrl = "http://speedtest.ftp.otenet.gr/files/test1Mb.db";
        //double downloadSpeed = calculateDownloadSpeed(fileUrl);
        //System.out.println("Download speed: " + downloadSpeed + " kbps");
    }
}
