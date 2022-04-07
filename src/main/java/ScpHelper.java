import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

public class ScpHelper {

    public Properties configProperties;

    ScpHelper() {
        init(); // get properties
    }

    private void init() {
        try {
            configProperties = new Properties();
            InputStream fileInputStream = D2_DB.class.getClassLoader().getResourceAsStream("config.properties");
            configProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public Session getSession() throws JSchException {
        JSch jSch = new JSch();
        jSch.addIdentity(configProperties.getProperty("sshPrivateKey"));
//        jSch.addIdentity("dmwa");
        Session session = jSch.getSession(
                configProperties.getProperty("remoteUser"),
                configProperties.getProperty("remoteHost"),
                Integer.parseInt(configProperties.getProperty("remotePort")));

        session.setConfig("StrictHostKeyChecking", "no");
        System.out.println("Attempting Connection!");
        session.connect();
        System.out.println("Session Connected!");
        return session;
    }

    public ChannelSftp getChannel(Session session) throws JSchException, SftpException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        System.out.println("Channel Connected!");

        // change directory: home directory
        channel.cd(configProperties.getProperty("remoteDbHomeDir"));

        return channel;
    }

    public boolean downloadFile(ChannelSftp channel, String path, String downloadFileName) throws SftpException, IOException {
        InputStream inputStream = channel.get(downloadFileName);

        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length()-1) != '/') {
            path += '/';
        }

        File outputFile = new File(path + downloadFileName);
        OutputStream outStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outStream.close();
        System.out.println("File Downloaded!");
        return true;
    }

    public boolean uploadFile(ChannelSftp channel, String path, String uploadFileName) throws IOException, SftpException {
        File inputFile = new File(uploadFileName);
        InputStream inputStream = new DataInputStream(new FileInputStream(inputFile));

        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length()-1) != '/') {
            path += '/';
        }

        channel.put(inputStream, (path + uploadFileName));

        inputStream.close();
        System.out.println("File Uploaded");

        return true;
    }

    public boolean deleteFile(ChannelSftp channel, String path, String fileName) throws SftpException {

        //TODO: Check if file exists

        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length()-1) != '/') {
            path += '/';
        }
        channel.rm(path + fileName);

        return true;
    }

    public boolean makeDirectory(ChannelSftp channel, String path, String directoryName) throws SftpException {

        //TODO: Check if directory exists

        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length()-1) != '/') {
            path += '/';
        }
        channel.mkdir(path + directoryName);

        return true;
    }

    public boolean deleteDirectory(ChannelSftp channel, String path, String directoryName) throws SftpException {

        //TODO: Check if directory exists

        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length()-1) != '/') {
            path += '/';
        }
        channel.rmdir(path + directoryName);

        return true;
    }
}