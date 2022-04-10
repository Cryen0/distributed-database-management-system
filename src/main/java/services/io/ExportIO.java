package services.io;

import model.Table;
import services.DbManager;

import java.util.ArrayList;
import java.util.List;

public class ExportIO {

    private static DbManager dbManager = DbManager.getInstance();

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
            Table remoteTable = TableIO.readTable(tableName, true);
            tableList.add(Table.merge(localTable, remoteTable));
        }
        dbManager.rollback();

        return tableList;
    }
}