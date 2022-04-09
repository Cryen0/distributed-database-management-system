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
                //TODO: DELETE OPERATION
                break;
            case "UPDATE":
                //TODO: UPDATE OPERATION
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
        String keyword = query.split(" ")[1];
        String keywordName = query.split(" ")[2];
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
        String databaseName = query.split(" ")[2];
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
        String tableName = matcher.group(2);
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

        Map<String, String> whereMap = new HashMap<>();
        if(where){
            String whereClause = matcher.group(3);
            String[] whereClauseSplit = whereClause.split("=");
            String whereColumnName = whereClauseSplit[0].trim();
            String whereValue = whereClauseSplit[1].trim();
            whereMap.put(whereColumnName, whereValue);
        }

        Table mergedTable = Table.merge(localTable, remoteTable);
        dbManager.selectFromTable(mergedTable, columns, whereMap);
    }

    private void parseInsert(String query){
        if(!dbManager.isCurrentDbSelected()){
            System.out.println("Please use USE command to select a database.");
            return;
        }
        Matcher matcher = Pattern.compile("INSERT\\sINTO\\s(.*)\\sVALUES\\s\\((.*)\\);", Pattern.CASE_INSENSITIVE).matcher(query);
        if(matcher.find()){
            String tableName = matcher.group(1);
            List<String> values = new ArrayList<>(Arrays.asList(matcher.group(2).split(",\\s*")));
            Table localTable = TableIO.readTable(tableName, false);
            Table remoteTable = TableIO.readTable(tableName, true);

            Record record = new Record();
            record.setValues(values);
            Table mergedTable = Table.merge(localTable, remoteTable);

            //if(canInsert(mergedTable, record)){
            TableIO.insert(tableName, record);
            System.out.println("Record inserted.");
            //}
        } else {
            System.out.println("Invalid INSERT statement.");
        }
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



    private boolean validateInsertQuery(String query){
        String[] querySplit = query.split(" ");
        if(querySplit[0].equalsIgnoreCase("insert") && querySplit[1].equalsIgnoreCase("into") && querySplit[4].equalsIgnoreCase("values")){
            return true;
        } else {
            return false;
        }
    }
}
