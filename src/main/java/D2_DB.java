import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

public class D2_DB {

    private static Properties configProperties;

    public static void main(String[] arg) throws JSchException, SftpException, IOException {

        // Getting the properties in config.properties
        configProperties = new Properties();
        InputStream fileInputStream = D2_DB.class.getClassLoader().getResourceAsStream("config.properties");
        configProperties.load(fileInputStream);
        fileInputStream.close();

        System.out.println("Program Starts!");
        JSch jSch = new JSch();
//        jSch.addIdentity(configProperties.getProperty("sshPrivateKey"));
        jSch.addIdentity("dmwa");
        Session session = jSch.getSession(
                configProperties.getProperty("remoteUser"),
                configProperties.getProperty("remoteHost"),
                Integer.parseInt(configProperties.getProperty("remotePort")));

        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        System.out.println("Session Connected!");

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        System.out.println("Channel Connected!");

        // Change Directory
        channel.cd(configProperties.getProperty("remoteDbHomeDir"));

        // Listing Directories
        Vector<ChannelSftp.LsEntry> entries = channel.ls(".");
        for (ChannelSftp.LsEntry entry : entries) {
            System.out.println("Entry: " + entry.getFilename());
        }

        // Downloading a file
        String downloadFileName = "test_vm2.txt";
        downloadFile(channel, downloadFileName);

        // Uploading a file
        String dirPath = configProperties.getProperty("remoteDbHomeDir") + "/";
        String uploadFileName = "test_vm1.txt";
        uploadFile(channel, dirPath, uploadFileName);

        // Delete File
//        channel.rm("file");

        // Creating Dir
//        channel.mkdir("dirName");

        // Deleting Dir -- MAKE SURE THE DIR IS EMPTY. IT WAS THROWING ERROR OTHERWISE
//        channel.rmdir("dirName");

        channel.disconnect();
        System.out.println("Channel Disconnected!");

        session.disconnect();
        System.out.println("Session Disconnected!");

        System.out.println("Program Ends!");
    }

    private static void uploadFile(ChannelSftp channel, String dirPath, String uploadFileName) throws IOException, SftpException {
        // Uploading a file

        File inputFile = new File(uploadFileName);
        InputStream inputStream = new DataInputStream(new FileInputStream(inputFile));
        channel.put(inputStream, (dirPath + uploadFileName));

        inputStream.close();
        System.out.println("File Uploaded");
    }

    private static void downloadFile(ChannelSftp channel, String downloadFileName) throws SftpException, IOException {
        // Reading file
        InputStream inputStream = channel.get(downloadFileName);
        File outputFile = new File(downloadFileName);
        OutputStream outStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outStream.close();
        System.out.println("File Downloaded!");
    }
}
