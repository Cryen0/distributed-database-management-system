package services.io;

import model.Column;
import model.Table;
import services.DbManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetadataIO {

    private static DbManager dbManager = DbManager.getInstance();

    public static List<String> getDatabaseNames() {
        List<String> dbNames = new ArrayList<>();

        String dbPath = dbManager.configProperties.getProperty("dbDir");
        File dbDir = new File(dbPath);
        File[] fileList = dbDir.listFiles();
        if (fileList == null) {
            fileList = new File[0];
        }
        for (File file : fileList) {
            String dbName = file.getName();
            dbNames.add(dbName);
        }

        return dbNames;
    }

    public static List<Table> getTableList(String dbName) {
        List<Table> tableList = new ArrayList<>();
        if (!dbManager.dbExists(dbName)) {
            System.out.println("Database does not exists!");
            return tableList;
        }

        dbManager.setCurrentDb(dbName);
        List<String> tableNames = dbManager.getAllTableNames();

        for (String tableName : tableNames) {
            Table localTable = TableIO.readTable(tableName, false);
            tableList.add(localTable);
        }

        dbManager.rollback();
        return tableList;
    }

    public static boolean updateMetaData() {

        // Local Metadata
        String localMetadataFilePath = dbManager.configProperties.getProperty("metaDataDir") + "/local.txt";
        File localMetadataFile = new File(localMetadataFilePath);
        if (localMetadataFile.exists()) {
            localMetadataFile.delete();
        }

        // Global Metadata
        String globalMetadataFilePath = dbManager.configProperties.getProperty("metaDataDir") + "/global.txt";
        File globalMetadataFile = new File(globalMetadataFilePath);
        if (globalMetadataFile.exists()) {
            globalMetadataFile.delete();
        }

        List<String> dbNames = getDatabaseNames();

        for (String dbName : dbNames) {
            List<Table> tableList = getTableList(dbName);
            writeToFile(localMetadataFile, dbName, tableList);
            writeToFile(globalMetadataFile, dbName, tableList);
        }

        dbManager.pushGlobalMetaData();

        return true;
    }

    public static boolean writeToFile(File file, String dbName, List<Table> tableList) {
        try {
            FileWriter fileWriter = new FileWriter(file, true);

            fileWriter.write("Database Name: " + dbName);
            for (Table table : tableList) {
                String columnString = Column.getColumnListString(table.getColumnList());
                fileWriter.write('\n'); // new line
                fileWriter.write("Table Name: " + table.getName());
                fileWriter.write('\n'); // new line
                fileWriter.write(columnString);
            }
            fileWriter.write('\n'); // new line
            fileWriter.write('\n'); // new line
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}