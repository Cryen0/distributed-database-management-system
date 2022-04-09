package services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import model.Column;
import model.Record;
import model.Table;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DbManager {

    private static DbManager dbManager;
    public Properties configProperties;

    private ScpHelper scpHelper;
    private Session session;
    private String currentDb;
    private boolean transactionInProgress;

    private DbManager() {
        this.scpHelper = new ScpHelper();
        this.session = scpHelper.getSession();
        init();
    }

    public static DbManager getInstance() {
        if (dbManager == null) {
            dbManager = new DbManager();
        }
        return dbManager;
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

    public int databaseCount() {
        try {
            String dbPath = configProperties.getProperty("dbDir");
            File dbDir = new File(dbPath);
            File[] allFiles = dbDir.listFiles();
            return allFiles.length;
        } catch (Exception e) {
            return 0;
        }
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

    public boolean deleteTable(String tableName) {
        String tablePath = configProperties.getProperty("dbDir") + "/" + this.currentDb + "/" + tableName + ".txt";
        File tableFile = new File(tablePath);

        // Delete the same table in remote
        if (tableFile.delete()) {
            ChannelSftp channelSftp = scpHelper.getChannel(this.session, "dbDir");
            try {
                channelSftp.cd(getCurrentDb());
                return scpHelper.deleteFile(channelSftp, tableName + ".txt");
            } catch (SftpException e) {
                e.printStackTrace();
            }
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
                    String remoteFilePath = this.configProperties.getProperty("remoteDir") + this.configProperties.getProperty("dbDir") + "/" + this.currentDb;
                    return scpHelper.uploadFile(channelSftp, tableFile, remoteFilePath);
                }
            } catch (IOException | SftpException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void selectFromTable(Table table, List<Column> columns, Map<String, String> whereMap) {
        Table fetchedData = new Table();
        fetchedData.setName(table.getName());
        fetchedData.setColumnList(columns);
        if (!whereMap.isEmpty()) {
            AtomicReference<String> whereColumn = new AtomicReference<>("");
            AtomicReference<String> whereValue = new AtomicReference<>("");
            whereMap.forEach((key, value) -> {
                whereColumn.set(key);
                whereValue.set(value);
            });
            List<Record> records = new ArrayList<>();
            table.getMappedRecordList().forEach(row -> {
                if (row.get(whereColumn.get()).equals(whereValue.get())) {
                    Record record = new Record();
                    record.setValues(new ArrayList<>(row.values()));
                    records.add(record);
                }
            });
            table.setRecordList(records);
        }

        table.getMappedRecordList().forEach(record -> {
            List<String> row = new ArrayList<>();
            for (Column column : columns) {
                row.add(record.get(column.getName()));
            }
            System.out.format("%s\n", String.join("|", row));
        });
    }

    public int tableCount() {
        try {
            String dbPathForDb = configProperties.getProperty("dbDir");
            File dbDir = new File(dbPathForDb);
            File[] allDbs = dbDir.listFiles();
            int totalTable = 0;
            for (int i = 0; i < allDbs.length; i++) {
                File tableDir = new File(String.valueOf(allDbs[i]));
                File[] allTables = tableDir.listFiles();
                totalTable += allTables.length;
            }
            return totalTable;
        } catch (Exception e) {
            return 0;
        }
    }

    public void insertIntoTable(Table table, List<String> values) {

    }

    /*************************************************************************
     * TRANSACTION UTILS
     *************************************************************************/

    public boolean isTransactionInProgress() {
        return transactionInProgress;
    }

    public void startTransaction() {
        this.transactionInProgress = true;
    }

    public boolean commit() {
        pushTables();
        cleanDirectory(this.configProperties.getProperty("transLocalDir"));
        cleanDirectory(this.configProperties.getProperty("transRemoteDir"));
        this.transactionInProgress = false;
        return true;
    }

    public boolean rollback() {
        cleanDirectory(this.configProperties.getProperty("transLocalDir"));
        cleanDirectory(this.configProperties.getProperty("transRemoteDir"));
        this.transactionInProgress = false;
        return true;
    }

    public boolean fetchTable(String tableName) {
        String downloadFilePath = this.configProperties.getProperty("transRemoteDir");

        File file = new File(downloadFilePath + "/" + tableName + ".txt");
        if (file.exists()) {
            return true;
        }
        ChannelSftp channelSftp = scpHelper.getChannel(this.session, "dbDir");
        try {
            channelSftp.cd(getCurrentDb());
            return scpHelper.downloadFile(channelSftp, tableName + ".txt", downloadFilePath);
        } catch (SftpException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean copyTableToTransactions(String tableName) {
        String copyFilePath = this.configProperties.getProperty("transLocalDir");

        File file = new File(copyFilePath + "/" + tableName + ".txt");
        if (file.exists()) {
            return true;
        }

        String dbFilePath = this.configProperties.getProperty("dbDir")
                + "/" + this.currentDb
                + "/" + tableName + ".txt";
        File dbFile = new File(dbFilePath);
        try {
            Files.copy(dbFile.toPath(), file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean pushTables() {
        return true;
    }

    public boolean cleanDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();
        if (fileList == null) {
            fileList = new File[0];
        }
        for (File file : fileList) {
            file.delete();
        }
        return true;
    }

    /*************************************************************************
     * METADATA UTILS
     *************************************************************************/

    public boolean fetchLog(String logName) {
        String downloadFilePath = this.configProperties.getProperty("transLogsDir");
        ChannelSftp channelSftp = scpHelper.getChannel(this.session, "logsDir");
        return scpHelper.downloadFile(channelSftp, logName + ".txt", downloadFilePath);
    }
}