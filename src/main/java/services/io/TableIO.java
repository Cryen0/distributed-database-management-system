package services.io;

import model.Column;
import model.Record;
import model.Table;
import services.DbManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TableIO {

    private static DbManager dbManager = DbManager.getInstance();

    public static Table readTable(String tableName, boolean isRemote) {
        if (isRemote) {
            dbManager.fetchTable(tableName);
        } else {
            dbManager.copyTableToTransactions(tableName);
        }

        Table table = new Table();
        table.setName(tableName);

        String filePath = getTablePath(isRemote) + "/" + tableName + ".txt";
        File file = new File(filePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return table;
        }

        // Get Column Names
        String columnListString = scanner.nextLine();
        table.setColumnList(Column.getColumnList(columnListString));

        // Read all the records
        while (scanner.hasNext()) {
            String recordString = scanner.nextLine();
            table.getRecordList().add(Record.getRecord(recordString));
        }
        return table;
    }

    public static boolean update(Table table, boolean isRemote) {

        String filePath = getTablePath(isRemote) + "/" + table.getName() + ".txt";
        File file = new File(filePath);

        try {
            FileWriter fileWriter = new FileWriter(file, false);
            String columnString = Column.getColumnListString(table.getColumnList());
            fileWriter.write(columnString);
            fileWriter.close();

            fileWriter = new FileWriter(file, true);
            for (Record record : table.getRecordList()) {
                String recordString = Record.getRecordString(record.getValues());
                fileWriter.write('\n'); // new line
                fileWriter.write(recordString);
            }

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean insert(String tableName, Record record) {

        // Insert will always be on local
        String filePath = getTablePath(false) + "/" + tableName + ".txt";
        File file = new File(filePath);

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            String recordString = Record.getRecordString(record.getValues());
            fileWriter.write('\n'); // new line
            fileWriter.write(recordString);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getTablePath(boolean isRemote) {
        String tablePath = "";
        if (isRemote) {
            tablePath = dbManager.configProperties.getProperty("transRemoteDir");
        } else {
            tablePath = dbManager.configProperties.getProperty("transLocalDir");
        }
        return tablePath;
    }
}