//package com.nex.common;
//
//public class CommonFtpService {
//    private static final String SERVER = "ftp.example.com";
//    private static final int PORT = 21;
//    private static final String USERNAME = "your-ftp-username";
//    private static final String PASSWORD = "your-ftp-password";
//
//    public static void uploadFile(String localFilePath, String remoteDirectory, String remoteFileName) throws IOException {
//        FTPClient ftpClient = new FTPClient();
//
//        try {
//            ftpClient.connect(SERVER, PORT);
//            ftpClient.login(USERNAME, PASSWORD);
//
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//            File localFile = new File(localFilePath);
//            FileInputStream inputStream = new FileInputStream(localFile);
//
//            String remoteFilePath = remoteDirectory + "/" + remoteFileName;
//            boolean done = ftpClient.storeFile(remoteFilePath, inputStream);
//
//            if (done) {
//                System.out.println("File uploaded successfully.");
//            } else {
//                System.out.println("Failed to upload file.");
//            }
//        } finally {
//            if (ftpClient.isConnected()) {
//                ftpClient.logout();
//                ftpClient.disconnect();
//            }
//        }
//    }
//}
