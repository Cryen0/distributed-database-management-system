package parser;

import model.Column;
import model.Record;
import model.Table;
import services.DatabaseSetting;
import services.DbManager;
import services.io.TableIO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    DbManager dbManager = DbManager.getInstance();
    Scanner sc = new Scanner(System.in);
    public void parseQuery() throws IOException {
        System.out.println("\nEnter your query: ");
        String query = sc.nextLine();
        String operation = getOperation(query);

        switch(operation.toUpperCase()){
            case "CREATE":
                parseCreate(query);
                break;
            case "INSERT":
                parseInsert(query);
                break;
            case "SELECT":
                parseSelect(query);
                break;
            case "USE":
                parseUse(query);
                break;
            case "DELETE":
                parseDelete(query);
                break;
            case "UPDATE":
                parseUpdate(query);
                break;
            default:
                System.out.println("Invalid Operation.");
                break;
        }
    }

    private String getOperation(String query){
         return query.split(" ")[0];
    }

    // ----------- PARSERS -----------
    private void parseCreate(String query) throws IOException {
        if(!dbManager.isCurrentDbSelected()){
            System.out.println("Please use USE command to select a database.");
            return;
        }
        String keyword = query.split(" ")[1].trim();
        String keywordName = query.split(" ")[2].trim();
        System.out.println(keyword + " " + keywordName);
        if(keyword.toUpperCase().equals("DATABASE")){
            dbManager.createDb(keywordName);
        } else if(keyword.toUpperCase().equals("TABLE")){

            Table table = new Table();
            table.setName(keywordName);
            table.setColumnList(parseColumns(query));
            dbManager.createTable(table);
            System.out.println("Table " + table.getName() + " created.");
        } else {
            System.out.println("Invalid keyword.");
        }
    }

    private void parseUse(String query){
        String databaseName = query.split(" ")[2].trim();
        if(dbManager.setCurrentDb(databaseName)){
            System.out.println("Database " + databaseName + " selected.");
        } else {
            System.out.println("Database " + databaseName + " does not exist.");
        }
    }

    private void parseSelect(String query){
        String regex;
        boolean where = false;
        if(query.toLowerCase().contains("where")) {
            regex = "SELECT\\s+(.*)\\s+FROM\\s+(.*)\\s+WHERE\\s+(.*)";
            where = true;
        } else {
            regex = "SELECT\\s+(.*)\\s+FROM\\s+(.*)";
        }
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(query);
        matcher.find();
        String tableName = matcher.group(2).trim();
        if(!dbManager.isCurrentDbSelected()){
            System.out.println("Please use USE command to select a database.");
            return;
        }
        if(!dbManager.tableExists(tableName)){
            System.out.println("Table " + tableName + " does not exist.");
            return;
        }
        Table localTable = TableIO.readTable(tableName, false);
        Table remoteTable = TableIO.readTable(tableName, true);
        List<Column> columns = new ArrayList<>();
        if(matcher.group(1).equals("*")){
            columns = localTable.getColumnList();
        } else {
            for ( String columnName : matcher.group(1).split(",")) {
                if(localTable.columnExists(columnName)){
                    Column column = new Column();
                    column.setName(columnName);
                    columns.add(column);
                }
            }
        }

        String whereString = where ? matcher.group(3).trim() : "";

        Table mergedTable = Table.merge(localTable, remoteTable);
        dbManager.selectFromTable(mergedTable, columns, whereString);
    }

    private void parseInsert(String query){
        if(!dbManager.isCurrentDbSelected()){
            System.out.println("Please use USE command to select a database.");
            return;
        }
        Matcher matcher = Pattern.compile("INSERT\\s+INTO\\s+(.*)\\s+VALUES\\s*\\((.*)\\);", Pattern.CASE_INSENSITIVE).matcher(query);
        if(matcher.find()){
            String tableName = matcher.group(1);
            if(!dbManager.tableExists(tableName)){
                System.out.println("Table " + tableName + " does not exist.");
            }
            List<String> values = new ArrayList<>(Arrays.asList(matcher.group(2).replaceAll("\'|\"", "").split(",\\s*")));
            Table localTable = TableIO.readTable(tableName, false);
            Table remoteTable = TableIO.readTable(tableName, true);

            Record record = new Record();
            record.setValues(values);
            Table mergedTable = Table.merge(localTable, remoteTable);

            //if(canInsert(mergedTable, record)){
            //TODO: check if record is valid
            TableIO.insert(tableName, record);
            System.out.println("Record inserted.");
            //}
        } else {
            System.out.println("Invalid INSERT statement.");
        }
    }

    private void parseUpdate(String query){
        if(!dbManager.isCurrentDbSelected()){
            System.out.println("Please use USE command to select a database.");
        }

        Matcher matcher = Pattern.compile("UPDATE\\s+(.*)\\s+SET\\s+(.*)WHERE\\s*(.*)", Pattern.CASE_INSENSITIVE).matcher(query);
        if(!matcher.find()){
            System.out.println("Invalid UPDATE statement.");
            return;
        }

        String tableName = matcher.group(1).trim();
        if(!dbManager.tableExists(tableName)){
            System.out.println("Table " + tableName + " does not exist.");
        }

        String[] toUpdateString = matcher.group(2).split(",\\s*");
        Map<String, String> toUpdateMap = getMapFromStringArray(toUpdateString);

        String whereString = matcher.group(3);

        Table localTable = TableIO.readTable(tableName, false);
        localTable = dbManager.updateTable(localTable, toUpdateMap, whereString);
        TableIO.update(localTable, false);

        Table remoteTable = TableIO.readTable(tableName, true);
        remoteTable = dbManager.updateTable(remoteTable, toUpdateMap, whereString);
        TableIO.update(remoteTable, true);
    }

    private void parseDelete(String query){
        if(!dbManager.isCurrentDbSelected()){
            System.out.println("Please use USE command to select a database.");
            return;
        }

        Matcher matcher = Pattern.compile("DELETE\\s*FROM\\s*(.*)\\s*WHERE\\s*(.*)", Pattern.CASE_INSENSITIVE).matcher(query);
        if(!matcher.find()){
            System.out.println("Invalid DELETE statement.");
            return;
        }

        String tableName = matcher.group(1).trim();
        if(!dbManager.tableExists(tableName)){
            System.out.println("Table " + tableName + " does not exist.");
            return;
        }

        String whereString = matcher.group(2);

        Table localTable = TableIO.readTable(tableName, false);
        localTable = dbManager.deleteFromTable(localTable, whereString);
        TableIO.update(localTable, false);

        Table remoteTable = TableIO.readTable(tableName, true);
        remoteTable = dbManager.deleteFromTable(remoteTable, whereString);
        TableIO.update(remoteTable, true);
    }

    // ----------- HELPER FUNCTIONS -----------
    private List<Column> parseColumns(String query) {
        Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(query);
        String columnNamesString = "";
        List<Column> parsedColumns = new ArrayList<>();
        if(matcher.find()){

            columnNamesString = matcher.group(1);
            columnNamesString = String.join(" ", columnNamesString.split("\\s+"));
            columnNamesString = String.join(",", columnNamesString.split(",\\s+"));
            for(String columnEntry : columnNamesString.split(",")){
                Column column = new Column();
                String[] columnEntrySplit = columnEntry.split(" ");
                column.setName(columnEntrySplit[0].trim());
                column.setType(columnEntrySplit[1].trim());
                if(columnEntrySplit.length > 3){
                    if(columnEntrySplit[3].trim().equals("PRIMARY")){
                        column.setPrimary(true);
                    }
                } else {
                    column.setPrimary(false);
                }
                parsedColumns.add(column);
            }
}
        return parsedColumns;
    }

    public ArrayList<String> getColumnValues(String query){
        ArrayList<String> columnValues = new ArrayList<String>();
        Matcher queryMatcher = Pattern.compile("\\((.*?)\\)").matcher(query);
        int count = 0;
        queryMatcher.find();
        if (queryMatcher.find()){
                for (String columnValue:
                        queryMatcher.group(1).split(",")) {
                    columnValues.add(columnValue.trim());
                }
        } else {
            System.out.println("Incorrect syntax");
            System.exit(0);
        }

        System.out.println(columnValues);
        return columnValues;
    }

    private Map<String, String> getMapFromStringArray(String[] mappedStrings){
        Map<String,String> mappedData = new HashMap<>();
        for(String mappedString : mappedStrings){
            String[] toUpdateSplit = mappedString.split("=");
            mappedData.put(toUpdateSplit[0].trim().replaceAll("\'|\"", ""), toUpdateSplit[1].trim().replaceAll("\'|\"", ""));
        }
        return mappedData;
    }
}
