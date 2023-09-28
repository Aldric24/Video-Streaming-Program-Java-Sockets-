package com.company;

import java.io.File;
import java.io.IOException;

public class converter {
    private static final String FFMPEG_PATH = "C:\\PATH_Programs\\ffmpeg.exe";

    public static void main(String[] args) {
        String directoryPath = "D:\\Movies";

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isMovieFile(file)) {
                    String fileName = file.getName();
                    String resolution = getResolution(fileName);
                    String format = getFormat(fileName);

                    System.out.println("Processing file: " + fileName);
                    System.out.println("Resolution: " + resolution);
                    System.out.println("Format: " + format);

                    createMissingFiles(directoryPath, fileName, resolution, format);
                }
            }
        }
    }

    private static boolean isMovieFile(File file) {
        String fileName = file.getName();
        return fileName.matches("^[^_]+_[0-9]+p\\.(mp4|avi|mkv)$");
    }

    private static String getResolution(String fileName) {
        return fileName.split("_")[1].split("\\.")[0];
    }

    private static String getFormat(String fileName) {
        return fileName.split("\\.")[1];
    }

    private static void createMissingFiles(String directoryPath, String fileName, String resolution, String format) {
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String baseFormat = format;

        // Creating files with different resolutions and formats
        String[] resolutions = {"1080p", "720p", "480p", "360p", "240p"};
        String[] formats = {"mp4", "avi", "mkv"};

        int existingResolution = Integer.parseInt(resolution.split("p")[0]);

        for (String res : resolutions) {
            int currentResolution = Integer.parseInt(res.split("p")[0]);

            if (existingResolution < currentResolution) {
                continue; // to Skip resolutions higher than the existing file
            }

            boolean fileCreated = false; // Flag to track if a missing file is created

            for (String fmt : formats) {
                if (!fmt.equals(format)) {
                    String newFileName = baseName.replace(resolution, res) + "." + fmt;
                    String command = FFMPEG_PATH + " -i " + directoryPath + File.separator + fileName +
                            " -vf \"scale=trunc(iw/2)*2:-1\" " +
                            directoryPath + File.separator + newFileName;


                    System.out.println("Executing command: " + command);

                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
                        processBuilder.inheritIO(); // Inheriting the input/output streams
                        Process process = processBuilder.start();
                        int exitCode = process.waitFor();

                        if (exitCode == 0) {
                            System.out.println("Created file: " + newFileName);
                            fileCreated = true;
                        } else {
                            System.err.println("Failed to create file: " + newFileName);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (currentResolution < existingResolution) {
                        String newFileName = baseName.replace(resolution, res) + "." + format;
                        String command = FFMPEG_PATH + " -i " + directoryPath + File.separator + fileName +
                                " -vf scale=-1:" + res.split("p")[0] +
                                " " + directoryPath + File.separator + newFileName;

                        System.out.println("Executing command: " + command);

                        try {
                            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
                            processBuilder.inheritIO(); // Inheriting the input/output streams
                            Process process = processBuilder.start();
                            int exitCode = process.waitFor();

                            if (exitCode == 0) {
                                System.out.println("Created file: " + newFileName);
                                fileCreated = true; // Setting the flag to true
                            } else {
                                System.err.println("Failed to create file: " + newFileName);
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (fileCreated) {
                existingResolution = currentResolution; // Updating the existingResolution
            }
        }
    }





}
