package services;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

public class ScpHelper {

    public Properties configProperties;

    public ScpHelper() {
        init(); // get properties
    }

    private void init() {
        try {
            configProperties = new Properties();
            InputStream fileInputStream = ScpHelper.class.getClassLoader().getResourceAsStream("config.properties");
            configProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public Session getSession() {
        String vmName = configProperties.getProperty("vm");
        JSch jSch = new JSch();
        try {
            jSch.addIdentity(configProperties.getProperty("sshPrivateKey"));
            Session session = jSch.getSession(
                    configProperties.getProperty("remoteUser"),
                    configProperties.getProperty(vmName + "remoteHost"),
                    Integer.parseInt(configProperties.getProperty("remotePort")));

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            return session;
        } catch (JSchException jSchException) {
            jSchException.printStackTrace();
            return null;
        }
    }

    public ChannelSftp getChannel(Session session, String directory) {
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            // change directory
            String remoteDir = configProperties.getProperty("remoteDir");
            String path = remoteDir + configProperties.getProperty(directory);
            channel.cd(path);
            return channel;
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean downloadFile(ChannelSftp channel, String downloadFileName, String path) {

        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length() - 1) != '/') {
            path += '/';
        }

        File outputFile = new File(path + downloadFileName);
        OutputStream outStream = null;
        try {
            InputStream inputStream = channel.get(downloadFileName);
            outStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outStream.close();
        } catch (IOException | SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean uploadFile(ChannelSftp channel, File uploadFile, String path) {
        // Checking if destination path contains '/'
        if (path != null
                && !path.isEmpty()
                && path.charAt(path.length() - 1) != '/') {
            path += '/';
        }

        try {
            InputStream inputStream = new DataInputStream(new FileInputStream(uploadFile));
            channel.put(inputStream, (path + uploadFile.getName()));
            inputStream.close();
        } catch (IOException | SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteFile(ChannelSftp channel, String name) throws SftpException {

        //TODO: Check if file exists
        channel.rm(name);

        return true;
    }

    public boolean makeDirectory(ChannelSftp channel, String directoryName) {
        try {
            channel.mkdir(directoryName);
        } catch (SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteDirectory(ChannelSftp channel, String directoryName) {
        try {
            channel.rmdir(directoryName);
        } catch (SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}