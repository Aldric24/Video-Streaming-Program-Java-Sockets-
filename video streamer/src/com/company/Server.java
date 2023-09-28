package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Server {
    private static final int SERVER_PORT = 8080;
    private static final String VIDEO_DIRECTORY = "D:\\Movies";

    public static void main(String[] args) {
        //converter.main(new String[]{});

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                handleClientRequest(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            String requestType = in.readUTF();
            if (requestType.equals("LIST")) {
                String selectedFormat = in.readUTF();
                double downloadSpeedKbps = in.readDouble();

                System.out.println("Client download speed: " + downloadSpeedKbps + " kbps");

                List<String> videoFiles = getVideoFiles(selectedFormat, downloadSpeedKbps);
                out.writeInt(videoFiles.size()); // Sending the number of video files to the client

                for (String fileName : videoFiles) {
                    out.writeUTF(fileName); // Sending each video file name to the client
                }
            } else if (requestType.equals("SEND")) {
                String selectedFile = in.readUTF();
                boolean fileExists = checkFileExists(selectedFile);

                out.writeBoolean(fileExists); // Sending the existence of the file to the client

                if (fileExists) {
                    String transmissionProtocol = in.readUTF();
                    sendFileToClient(socket, selectedFile, transmissionProtocol); // Sending the file to the client
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getVideoFiles(String format, double downloadSpeedKbps) {
        List<String> videoFiles = new ArrayList<>();
        File directory = new File(VIDEO_DIRECTORY);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith("." + format)) {
                        String fileName = file.getName();
                        String[] parts = fileName.split("_");
                        if (parts.length >= 2) {
                            String resolution = parts[parts.length - 1];
                            String[] parts_2 = resolution.split("\\.");
                            String resolutionPart = parts_2[0];
                            if (isValidResolution(resolutionPart, downloadSpeedKbps)) {
                                videoFiles.add(fileName);
                            }
                        }
                    }
                }
            }
        }

        return videoFiles;
    }

    private static boolean isValidResolution(String resolutionPart, double downloadSpeedKbps) {
        // Extracting the resolution value from the filename (e.g., 240p, 360p, 480p, etc.)
        String resolutionString = resolutionPart.substring(0, resolutionPart.length() - 1);
        int resolution = Integer.parseInt(resolutionString);

        // Applying the resolution filtering based on the connection speed
        if (downloadSpeedKbps <= 700) {
            return resolution <= 240;
        } else if (downloadSpeedKbps <= 1000) {
            return resolution <= 360;
        } else if (downloadSpeedKbps <= 2000 ) {
            return resolution <= 480;
        } else if (downloadSpeedKbps <= 4000 ) {
            return resolution <= 720;
        } else if (downloadSpeedKbps <= 6000) {
            return resolution <= 1080;
        } else {
            return true; // Returning true for all resolutions if the speed is above 6000 kbps
        }
    }

    private static boolean checkFileExists(String fileName) {
        File file = new File(VIDEO_DIRECTORY, fileName);
        return file.exists() && file.isFile();
    }

    private static void sendFileToClient(Socket socket, String fileName, String transmissionProtocol) throws IOException {
        switch (transmissionProtocol) {
            case "TCP":
                sendFileTCP(socket, fileName);
                break;
            default:
                System.out.println("Invalid transmission protocol: " + transmissionProtocol);
                break;
        }
    }

    private static void sendFileTCP(Socket socket, String fileName) throws IOException {
        try {
            String ffmpegCommand = String.format("ffmpeg -i %s -c:v copy -f mpegts -", new File(VIDEO_DIRECTORY, fileName).getAbsolutePath());

            Process ffmpegProcess = Runtime.getRuntime().exec(ffmpegCommand);
            InputStream ffmpegOutput = ffmpegProcess.getInputStream();
            OutputStream os = socket.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = ffmpegOutput.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            ffmpegProcess.waitFor();
            ffmpegOutput.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
