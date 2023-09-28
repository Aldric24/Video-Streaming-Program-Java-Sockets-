package com.company;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Client extends Application {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private ListView<String> videoList;
    private ObservableList<String> videoFiles;
    private double downloadSpeedKbps;
    private Logger logger;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video Client");

        videoFiles = FXCollections.observableArrayList();
        videoList = new ListView<>(videoFiles);
        videoList.setPrefWidth(400);
        videoList.setPrefHeight(300);

            Button receiveButton = new Button("Receive Videos");
        receiveButton.setOnAction(e -> receiveVideoList());

        Button playButton = new Button("Download");
        playButton.setOnAction(e -> {
            String selectedVideo = videoList.getSelectionModel().getSelectedItem();
            if (selectedVideo != null) {
                selectTransmissionProtocol(selectedVideo);
            }
        });

        HBox buttonBox = new HBox(10, receiveButton, playButton);
        buttonBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(videoList);
        root.setBottom(buttonBox);

        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();

        // Setting up logger
        logger = Logger.getLogger("VideoClientLogger");
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler("client.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up logger.", e);
        }

        // Retrieving the speed test result from the separate class
        speedtest speedTest = new speedtest();
        downloadSpeedKbps = speedTest.calculateDownloadSpeed("http://speedtest.ftp.otenet.gr/files/test1Mb.db");
        // Displaying the speed test result to the client GUI
        showSpeedTestResult(downloadSpeedKbps);
    }

    private void receiveVideoList() {
        String selectedFormat = showFormatSelectionDialog();
        if (selectedFormat == null) {
            logger.warning("Please select a video format.");
            return;
        }

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            out.writeUTF("LIST"); // Sending request to server to get video list
            out.writeUTF(selectedFormat); // Sending selected video format to the server
            out.writeDouble(downloadSpeedKbps); // Sending speed test result to the server

            int numFiles = in.readInt(); // Receiving the number of video files from the server

            videoFiles.clear();
            for (int i = 0; i < numFiles; i++) {
                String fileName = in.readUTF(); // Receiving each video file name
                videoFiles.add(fileName);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error receiving video list.", e);
        }
    }

    private String showFormatSelectionDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Video Format Selection");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter the video format (e.g., mp4, avi):");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void showSpeedTestResult(double downloadSpeedKbps) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Speed Test Result");
        alert.setHeaderText(null);
        alert.setContentText("Download Speed: " + downloadSpeedKbps + " kbps");

        alert.showAndWait();
    }

    private void selectTransmissionProtocol(String fileName) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("TCP", "TCP");
        dialog.setTitle("Transmission Protocol Selection");
        dialog.setHeaderText(null);
        dialog.setContentText("Please select the transmission protocol:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(protocol -> playVideo(fileName, protocol));
    }

    private void playVideo(String fileName, String protocol) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             InputStream in = socket.getInputStream();
             DataInputStream dataIn = new DataInputStream(in)
        ) {
            out.writeUTF("SEND");
            out.writeUTF(fileName);
            out.writeUTF(protocol);

            long fileSize = dataIn.readLong(); // Receiving the file size from the server

            String savePath = "D:\\Downloaded\\" + fileName;

            // Creating the FFmpeg command to save the video to the local folder
            
            String ffmpegCommand = String.format("ffmpeg -i - -c copy \"%s\"", savePath);

            // Executing the FFmpeg command and receive the video file
            Process ffmpegProcess = Runtime.getRuntime().exec(ffmpegCommand);
            OutputStream ffmpegInput = ffmpegProcess.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = in.read(buffer)) != -1) {
                ffmpegInput.write(buffer, 0, bytesRead); // Writing the received data to FFmpeg
                totalBytesRead += bytesRead;
            }

            ffmpegInput.close();
            ffmpegProcess.waitFor();

            System.out.println("Video saved: " + savePath);
            playVideoFile(savePath);
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error playing video.", e);
        }
    }



    private void playVideoFile(String fileName) {
        Platform.runLater(() -> {
            try {
                Desktop.getDesktop().open(new File(fileName));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error playing video file.", e);
            }
        });
    }
}
