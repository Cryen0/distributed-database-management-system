package parser;

import services.DatabaseIO;
import services.DatabaseSetting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    DatabaseIO databaseIO = new DatabaseIO();
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
                    //TODO: INSERT OPERATION
                break;
            case "SELECT":
                //TODO: SELECT OPERATION
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
        if(DatabaseSetting.CURRENT_DATABASE == null){
            System.out.println("Please use USE command to select a database.");
            return;
        }
        String keyword = query.split(" ")[1];
        String keywordName = query.split(" ")[2];
        if(keyword.toUpperCase().equals("DATABASE")){

        } else if(keyword.toUpperCase().equals("TABLE")){
            ArrayList<String> columnNames = getColumnNames(query);
            databaseIO.createTable(keywordName, columnNames);
        } else {
            System.out.println("Invalid keyword.");
        }
    }

    private void parseUse(String query){
        String databaseName = query.split(" ")[2];
        File file = new File(System.getProperty("user.dir") + "/assets/database/" + databaseName);
        if(!file.exists()){
            System.out.println("Database does not exist.");
            return;
        }
        DatabaseSetting.CURRENT_DATABASE = databaseName;
        System.out.println("Using database: " + DatabaseSetting.CURRENT_DATABASE);
    }

    private void parseInsert(String query){

    }


    // ----------- HELPER FUNCTIONS -----------
    private ArrayList<String> getColumnNames(String query) {
        ArrayList<String> columnNames = new ArrayList<String>();
        Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(query);
        if(matcher.find()){
            for (String columnName:
                 matcher.group(1).split(",")) {
                String[] splitColumnName = columnName.split(" ");
                if(!DatabaseSetting.DATA_TYPES.contains(splitColumnName[1].toLowerCase())){
                    System.out.println("Invalid data type.\n");
                    return null;
                }
                columnName = String.join("²", columnName.trim().split(" "));
                columnNames.add(columnName.trim());
            }
        };
        System.out.println(columnNames);
        return columnNames;
    }

    private ArrayList<String> getColumnValues(String query){
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
