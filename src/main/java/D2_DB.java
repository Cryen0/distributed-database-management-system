import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

public class D2_DB {

    private static Properties configProperties;

    public static void main(String[] arg) throws JSchException, SftpException, IOException {

        ScpHelper scpHelper = new ScpHelper();
        Session session = scpHelper.getSession();
        ChannelSftp channel = scpHelper.getChannel(session);

        // Listing Directories
        Vector<ChannelSftp.LsEntry> entries = channel.ls(".");
        for (ChannelSftp.LsEntry entry : entries) {
            System.out.println("Entry: " + entry.getFilename());
        }

        // Downloading a file
        String downloadFileName = "test_vm2.txt";
        scpHelper.downloadFile(channel, "", downloadFileName);

        // Uploading a file
        String dirPath = "/home/science183";
        String uploadFileName = "test_vm1.txt";
        scpHelper.uploadFile(channel, dirPath, uploadFileName);

        channel.disconnect();
        System.out.println("Channel Disconnected!");

        session.disconnect();
        System.out.println("Session Disconnected!");

        System.out.println("Program Ends!");
    }
}
