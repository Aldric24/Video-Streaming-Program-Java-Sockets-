# Video-Streaming-Program-Java-Sockets-
This Java client-server system features a GUI client for video management. It begins with a speed test, displaying the outcome and allowing format selection. The server filters files by format and resolution according to the client's speed test results. Clients can request, download, and play videos, enhancing file management. 

Video Manager with Speed Test 🎥 🚀

This Java-based client-server application simplifies video management. The client opens with a speed test 📡, showing the user their download speed. Users can then select video formats (MP4, AVI, MKV) from a dropdown 📥.

On the server side, files are filtered based on format and resolution. The server determines which files to send based on the client's download speed. Files are stored in a directory with names like "movie_resolution.format" 📂. The server reads the file names, extracts resolution details 🎬, and filters accordingly:

🐢 If speed ≤ 700 kbps, sends 240p and lower resolutions
🚗 If speed ≤ 1000 kbps, adds 360p videos
🚄 If speed ≤ 2000 kbps, includes 480p
🚀 If speed ≤ 4000 kbps, delivers 720p
🌟 If speed ≤ 6000 kbps, shares up to 1080p
Clients can request a video for playback, and the server sends it upon request. Users can save and play the received video locally 📽️📦.

Several classes are used, including GUI components and network socket communication. Error handling and debugging are ongoing to enhance functionality.

Technologies Used: Java, GUI, Networking.

Usage: Improve video management, select, download, and play videos based on download speed.

Instructions: Launch the server and client. Perform a speed test, choose a format, and browse videos.

Contributions: Welcome, especially for error handling and optimization.

📝 Note: Requires additional testing and debugging for optimal performance.
