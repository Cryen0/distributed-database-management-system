package services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import model.Column;
import model.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbManager {

    private static DbManager dbManager;
    private Properties configProperties;

    private ScpHelper scpHelper;
    private Session session;
    private String currentDb;

    private DbManager() {
        this.scpHelper = new ScpHelper();
        this.session = scpHelper.getSession();
        init();
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

    public static DbManager getInstance() {
        if (dbManager == null) {
            dbManager = new DbManager();
        }
        return dbManager;
    }

    public boolean disconnectSession() {
        this.session.disconnect();
        System.out.println("Session Disconnected!");
        return true;
    }

    /*************************************************************************
     * DB UTILS
     *************************************************************************/

    public String getCurrentDb() {
        return currentDb;
    }

    public boolean setCurrentDb(String currentDb) {
        if (dbExists(currentDb)) {
            this.currentDb = currentDb;
            return true;
        } else {
            this.currentDb = null;
            return false;
        }
    }

    public boolean isCurrentDbSelected() {
        return this.currentDb != null;
    }

    public boolean dbExists(String dbName) {
        String dbPath = configProperties.getProperty("dbDir") + "/" + dbName;
        File dbDir = new File(dbPath);
        return dbDir.exists();
    }

    public boolean createDb(String dbName) {
        String dbPath = configProperties.getProperty("dbDir") + "/" + dbName;
        File dbDir = new File(dbPath);

        // Make same db in remote
        if (dbDir.mkdir()) {
            ChannelSftp channelSftp = scpHelper.getChannel(this.session, "dbDir");
            return scpHelper.makeDirectory(channelSftp, dbName);
        }
        return false;
    }

    public boolean deleteDb(String dbName) {
        String dbPath = configProperties.getProperty("dbDir") + "/" + dbName;
        File dbDir = new File(dbPath);

        // Make same db in remote
        if (dbDir.delete()) {
            ChannelSftp channelSftp = scpHelper.getChannel(this.session, "dbDir");
            return scpHelper.deleteDirectory(channelSftp, dbName);
        }
        return false;
    }

    /*************************************************************************
     * TABLE UTILS
     *************************************************************************/

    public boolean tableExists(String tableName) {
        if (isCurrentDbSelected()) {
            String dbPath = configProperties.getProperty("dbDir") + "/" + this.currentDb;
            String tablePath = dbPath + "/" + tableName + ".txt";
            File tableFile = new File(tablePath);
            return tableFile.exists();
        }
        return false;
    }

    public boolean createTable(Table table) {

        if (isCurrentDbSelected()) {
            String dbPath = configProperties.getProperty("dbDir") + "/" + this.currentDb;
            String tablePath = dbPath + "/" + table.getName() + ".txt";
            File tableFile = new File(tablePath);
            try {
                if (tableFile.createNewFile()) {
                    FileWriter fileWriter = new FileWriter(tableFile, false);
                    String columnString = Column.getColumnListString(table.getColumnList());
                    fileWriter.write(columnString);
                    fileWriter.close();

                    // Copy Table to Remote
                    ChannelSftp channelSftp = scpHelper.getChannel(this.session, "dbDir");
                    channelSftp.cd(this.currentDb);
                    String remoteFilePath = this.configProperties.getProperty("remoteDir")
                            + this.configProperties.getProperty("dbDir")
                            + "/" + this.currentDb;
                    return scpHelper.uploadFile(channelSftp, tableFile, remoteFilePath);
                }
            } catch (IOException | SftpException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}