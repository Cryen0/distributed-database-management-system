package services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import model.Column;
import model.Record;
import model.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class DbManager {

    private static DbManager dbManager;
    public Properties configProperties;

    private ScpHelper scpHelper;
    private Session session;
    private String currentDb;
    private boolean transactionInProgress;
    private boolean autoCommit;

    private DbManager() {
        this.scpHelper = new ScpHelper();
        this.session = scpHelper.getSession();
        this.autoCommit = true;
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

    public List<String> getAllTableNames() {
        List<String> tableNames = new ArrayList<>();
        String dbPath = configProperties.getProperty("dbDir") + "/" + this.currentDb;
        File dbDir = new File(dbPath);
        File[] fileList = dbDir.listFiles();
        if (fileList == null) {
            fileList = new File[0];
        }
        for (File file : fileList) {
            String tableFileName = file.getName();
            String tableName = tableFileName.replace(".txt", "");
            tableNames.add(tableName);
        }

        return tableNames;
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
        }
    }

    public Table findWhere(Table table, String whereString) {
        if (!whereString.equals("")) {
            Table resultTable = new Table();
            resultTable.setName(table.getName().trim());
            resultTable.setColumnList(table.getColumnList());

            String[] whereSplit = whereString.split("=");
            String whereColumn = whereSplit[0].trim();
            String whereValue = whereSplit[1].trim();


            List<Record> records = new ArrayList<>();
            if (table.getRecordList().isEmpty()) {
                return table;
            }

            table.getMappedRecordList().forEach(row -> {
                if (row.get(whereColumn).equals(whereValue)) {
                    Record record = new Record();
                    record.setValues(new ArrayList<>(row.values()));
                    records.add(record);
                }
            });

            resultTable.setRecordList(records);
            return resultTable;
        } else {
            return table;
        }
    }

    public void selectFromTable(Table table, List<Column> columns, String whereString) {
        Table fetchedData = new Table();
        fetchedData.setName(table.getName());
        fetchedData.setColumnList(columns);
        table = findWhere(table, whereString);
        table.getMappedRecordList().forEach(record -> {
            List<String> row = new ArrayList<>();
            for (Column column : columns) {
                row.add(record.get(column.getName()));
            }
            System.out.format("%s\n", String.join("|", row));
        });
    }

    public Table deleteFromTable(Table table, String whereString) {
        String whereColumn = whereString.split("=")[0].trim();
        String whereValue = whereString.split("=")[1].trim();

        int columnIndex = 0;
        for (Column column : table.getColumnList()) {
            if (column.getName().equals(whereColumn)) {
                break;
            }
            columnIndex++;
        }

        for (int i = 0; i < table.getRecordList().size(); i++) {
            Record record = table.getRecordList().get(i);
            if (record.getValues().get(columnIndex).equals(whereValue)) {
                table.getRecordList().remove(record);
                i--;
            }
        }

        return table;
    }

    public Table updateTable(Table table, String updateString, String whereString) {
        String whereColumn = whereString.split("=")[0].trim();
        String whereValue = whereString.split("=")[1].trim();

        String updateColumn = updateString.split("=")[0].trim();
        String updateValue = updateString.split("=")[1].trim();

        int columnIndex = 0;
        int updateIndex = 0;
        for (int i = 0; i < table.getColumnList().size(); i++) {
            Column column = table.getColumnList().get(i);
            if (column.getName().equals(whereColumn)) {
                columnIndex = i;
            }
            if (column.getName().equals(updateColumn)) {
                updateIndex = i;
            }
        }

        for (int i = 0; i < table.getRecordList().size(); i++) {
            Record record = table.getRecordList().get(i);
            if (record.getValues().get(columnIndex).equals(whereValue)) {
                List<String> newValues = new ArrayList<>(record.getValues());
                newValues.set(updateIndex, updateValue);
                record.setValues(newValues);
            }
        }

        return table;
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

    /**********************2***************************************************
     * TRANSACTION UTILS
     *************************************************************************/

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean isTransactionInProgress() {
        return transactionInProgress;
    }

    public void startTransaction() {
        this.transactionInProgress = true;
    }

    public boolean commit() {
        copyTablesToDb(); // Copy to Local
        pushTables(); // Copy to Remote
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
        String transRemoteDirPath = this.configProperties.getProperty("transRemoteDir");
        File transRemoteDir = new File(transRemoteDirPath);
        File[] fileList = transRemoteDir.listFiles();
        if (fileList == null) {
            fileList = new File[0];
        }

        // Push Tables to Remote
        ChannelSftp channelSftp = scpHelper.getChannel(this.session, "dbDir");
        try {
            channelSftp.cd(this.currentDb);
        } catch (SftpException e) {
            e.printStackTrace();
            return false;
        }

        for (File file : fileList) {
            String remoteFilePath = this.configProperties.getProperty("remoteDir") + this.configProperties.getProperty("dbDir") + "/" + this.currentDb;
            scpHelper.uploadFile(channelSftp, file, remoteFilePath);
        }
        cleanDirectory(this.configProperties.getProperty("transRemoteDir"));
        return true;
    }

    public boolean copyTablesToDb() {
        String transLocalDirPath = this.configProperties.getProperty("transLocalDir");
        File transLocalDir = new File(transLocalDirPath);
        File[] fileList = transLocalDir.listFiles();
        if (fileList == null) {
            fileList = new File[0];
        }

        String dbDirPath = this.configProperties.getProperty("dbDir") + "/" + this.currentDb;

        for (File file : fileList) {
            String destFilePath = dbDirPath + "/" + file.getName();
            File destFile = new File(destFilePath);
            if (destFile.exists()) {
                destFile.delete();
            }
            try {
                Files.copy(file.toPath(), destFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        cleanDirectory(this.configProperties.getProperty("transLocalDir"));
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

    public boolean pushAuthFile() {
        File authFile = new File(this.configProperties.getProperty("authDir") + "/UserProfile.txt");

        ChannelSftp channelSftp = scpHelper.getChannel(this.session, "authDir");
        String remoteFilePath = this.configProperties.getProperty("remoteDir") + this.configProperties.getProperty("authDir");
        return scpHelper.uploadFile(channelSftp, authFile, remoteFilePath);
    }

    public boolean pushGlobalMetaData() {
        File authFile = new File(this.configProperties.getProperty("metaDataDir") + "/global.txt");

        ChannelSftp channelSftp = scpHelper.getChannel(this.session, "metaDataDir");
        String authFilePath = this.configProperties.getProperty("remoteDir") + this.configProperties.getProperty("metaDataDir");
        return scpHelper.uploadFile(channelSftp, authFile, authFilePath);
    }
}