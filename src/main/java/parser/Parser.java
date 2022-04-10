package parser;

import model.*;
import services.DbManager;
import services.ScpHelper;
import services.io.LogIO;
import services.io.MetadataIO;
import services.io.TableIO;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private Properties config;
    String loggedInUser;
    DbManager dbManager = DbManager.getInstance();
    Scanner sc = new Scanner(System.in);

    public Parser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
        config = new Properties();
        try {
            InputStream fileInputStream = ScpHelper.class.getClassLoader().getResourceAsStream("config.properties");
            config.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseQuery() throws IOException {
        System.out.println("\nEnter your query: ");
        String query = removeSemiColon(sc.nextLine());
        String operation = getOperation(query);

        switch (operation.toUpperCase()) {
            case "CREATE":
                parseCreate(query);
                break;
            case "INSERT":
                parseInsert(query);
                if (dbManager.isAutoCommit()) {
                    dbManager.commit();
                }
                break;
            case "SELECT":
                parseSelect(query);
                if (dbManager.isAutoCommit()) {
                    dbManager.rollback();
                }
                break;
            case "USE":
                parseUse(query);
                break;
            case "DELETE":
                parseDelete(query);
                if (dbManager.isAutoCommit()) {
                    dbManager.commit();
                }
                break;
            case "UPDATE":
                parseUpdate(query);
                if (dbManager.isAutoCommit()) {
                    dbManager.commit();
                }
                break;
            case "START": // Start Transaction
                dbManager.setAutoCommit(false);
                parseStart(query);
                break;
            case "COMMIT":
                parseCommit(query);
                dbManager.setAutoCommit(true);
                break;
            case "ROLLBACK":
                parseRollback(query);
                dbManager.setAutoCommit(true);
                break;
            default:
                System.out.println("Invalid Operation.");
                break;
        }
    }

    private String getOperation(String query) {
        return query.split(" ")[0];
    }

    // ----------- PARSERS -----------
    private void parseCreate(String query) {
        try {
            String keyword = query.split(" ")[1].trim();
            String keywordName = query.split(" ")[2].trim();


            long startTime = new Timestamp(System.currentTimeMillis()).getTime();
            if (keyword.toUpperCase().equals("DATABASE")) {
                dbManager.createDb(keywordName);
                System.out.println("Database created successfully.");
                EventLog eventLog = new EventLog("Database created successfully.");
                LogIO.writeToEventLog(eventLog);
            } else if (keyword.toUpperCase().equals("TABLE")) {
                if (!dbManager.isCurrentDbSelected()) {
                    throw new Exception("Please select a database using USE command.");
                }
                Table table = new Table();
                table.setName(keywordName);
                table.setColumnList(parseColumns(query));
                dbManager.createTable(table);
                System.out.println("Table " + table.getName() + " created.");
            } else {
                throw new Exception("Invalid keyword.");
            }
            long endTime = new Timestamp(System.currentTimeMillis()).getTime();
            long execTime = endTime - startTime;

            QueryLog queryLog = new QueryLog(config.getProperty("vm"), loggedInUser, dbManager.getCurrentDb(), String.valueOf(execTime), query, keywordName);
            LogIO.writeToQueryLog(queryLog);

            GeneralLog generalLog = new GeneralLog(String.valueOf(execTime), config.getProperty("vm"), dbManager.databaseCount(), dbManager.tableCount());
            LogIO.writeToGeneralLog(generalLog);
            MetadataIO.updateMetaData();
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");

        }
    }

    private void parseUse(String query) {
        try {
            if (dbManager.isTransactionInProgress()) {
                throw new Exception("Cannot change databases in between transaction.");
            }

            String databaseName = query.split(" ")[2].trim();
            if (dbManager.setCurrentDb(databaseName)) {
                System.out.println("Database " + databaseName + " selected.");
            } else {
                throw new Exception("Database " + databaseName + " does not exist.");
            }
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    private void parseSelect(String query) {
        try {
            if (!dbManager.isCurrentDbSelected()) {
                throw new Exception("Please use USE command to select a database.");
            }
            String regex;
            boolean where = false;
            if (query.toLowerCase().contains("where")) {
                regex = "SELECT\\s+(.*)\\s+FROM\\s+(.*)\\s+WHERE\\s+(.*)";
                where = true;
            } else {
                regex = "SELECT\\s+(.*)\\s+FROM\\s+(.*)";
            }
            Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(query);
            matcher.find();
            String tableName = matcher.group(2).trim();

            if (!dbManager.tableExists(tableName)) {
                throw new Exception("Table " + tableName + " does not exist.");
            }
            Table localTable = TableIO.readTable(tableName, false);
            Table remoteTable = TableIO.readTable(tableName, true);
            List<Column> columns = new ArrayList<>();
            if (matcher.group(1).equals("*")) {
                columns = localTable.getColumnList();
            } else {
                for (String columnName : matcher.group(1).split(",")) {
                    if (localTable.columnExists(columnName)) {
                        Column column = new Column();
                        column.setName(columnName);
                        columns.add(column);
                    }
                }
            }

            String whereString = where ? matcher.group(3).trim() : "";

            Table mergedTable = Table.merge(localTable, remoteTable);

            long startTime = new Timestamp(System.currentTimeMillis()).getTime();
            dbManager.selectFromTable(mergedTable, columns, whereString);
            long endTime = new Timestamp(System.currentTimeMillis()).getTime();
            long execTime = endTime - startTime;
            QueryLog queryLog = new QueryLog(config.getProperty("vm"), loggedInUser, dbManager.getCurrentDb(), String.valueOf(execTime), query, tableName);
            LogIO.writeToQueryLog(queryLog);
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    private void parseInsert(String query) {
        try {
            if (!dbManager.isCurrentDbSelected()) {
                throw new Exception("Please use USE command to select a database.");
            }
            Matcher matcher = Pattern.compile("INSERT\\s+INTO\\s+(.*)\\s+VALUES\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE).matcher(query);
            if (matcher.find()) {
                String tableName = matcher.group(1);
                if (!dbManager.tableExists(tableName)) {
                    System.out.println("Table " + tableName + " does not exist.");
                }
                List<String> values = new ArrayList<>(Arrays.asList(matcher.group(2).replaceAll("\'|\"", "").split(",\\s*")));

                long startTime = new Timestamp(System.currentTimeMillis()).getTime();
                Table localTable = TableIO.readTable(tableName, false);
                Table remoteTable = TableIO.readTable(tableName, true);

                Record record = new Record();
                record.setValues(values);
                Table mergedTable = Table.merge(localTable, remoteTable);


                if (!mergedTable.canInsertRecord(record)) {
                    throw new Exception("Cannot insert duplicate records.");
                }
                TableIO.insert(tableName, record);
                long endTime = new Timestamp(System.currentTimeMillis()).getTime();
                long execTime = endTime - startTime;
                QueryLog queryLog = new QueryLog(config.getProperty("vm"), loggedInUser, dbManager.getCurrentDb(), String.valueOf(execTime), query, tableName);
                LogIO.writeToQueryLog(queryLog);
                System.out.println("Record inserted.");

            } else {
                throw new Exception("Invalid INSERT statement.");
            }
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");
            LogIO.writeToEventLog(eventLog);

        }
    }

    private void parseUpdate(String query) {
        try {
            if (!dbManager.isCurrentDbSelected()) {
                throw new Exception("Please use USE command to select a database.");
            }

            Matcher matcher = Pattern.compile("UPDATE\\s+(.*)\\s+SET\\s+(.*)WHERE\\s*(.*)", Pattern.CASE_INSENSITIVE).matcher(query);
            if (!matcher.find()) {
                throw new Exception("Invalid UPDATE statement.");
            }

            String tableName = matcher.group(1).trim();
            if (!dbManager.tableExists(tableName)) {
                System.out.println("Table " + tableName + " does not exist.");
            }


            String updateString = matcher.group(2);
            String whereString = matcher.group(3);

            long startTime = new Timestamp(System.currentTimeMillis()).getTime();
            Table localTable = TableIO.readTable(tableName, false);
            localTable = dbManager.updateTable(localTable, updateString, whereString);
            TableIO.update(localTable, false);

            Table remoteTable = TableIO.readTable(tableName, true);
            remoteTable = dbManager.updateTable(remoteTable, updateString, whereString);
            TableIO.update(remoteTable, true);
            long endTime = new Timestamp(System.currentTimeMillis()).getTime();
            long execTime = endTime - startTime;
            QueryLog queryLog = new QueryLog(config.getProperty("vm"), loggedInUser, dbManager.getCurrentDb(), String.valueOf(execTime), query, tableName);
            LogIO.writeToQueryLog(queryLog);
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    private void parseDelete(String query) {
        try {
            if (!dbManager.isCurrentDbSelected()) {
                throw new Exception("Please use USE command to select a database.");
            }

            Matcher matcher = Pattern.compile("DELETE\\s*FROM\\s*(.*)\\s*WHERE\\s*(.*)", Pattern.CASE_INSENSITIVE).matcher(query);
            if (!matcher.find()) {
                throw new Exception("Invalid DELETE statement.");
            }

            String tableName = matcher.group(1).trim();
            if (!dbManager.tableExists(tableName)) {
                System.out.println("Table " + tableName + " does not exist.");
                return;
            }

            String whereString = matcher.group(2);

            long startTime = new Timestamp(System.currentTimeMillis()).getTime();
            Table localTable = TableIO.readTable(tableName, false);
            localTable = dbManager.deleteFromTable(localTable, whereString);
            TableIO.update(localTable, false);

            Table remoteTable = TableIO.readTable(tableName, true);
            remoteTable = dbManager.deleteFromTable(remoteTable, whereString);
            TableIO.update(remoteTable, true);
            long endTime = new Timestamp(System.currentTimeMillis()).getTime();
            long execTime = endTime - startTime;
            QueryLog queryLog = new QueryLog(config.getProperty("vm"), loggedInUser, dbManager.getCurrentDb(), String.valueOf(execTime), query, tableName);
            LogIO.writeToQueryLog(queryLog);
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    private void parseStart(String query) {
        try {
            if (!dbManager.isCurrentDbSelected()) {
                throw new Exception("Please use USE statement to select a database");
            }
            String[] querySplit = query.split("\\s+");
            if (!(querySplit[1].trim().equalsIgnoreCase("TRANSACTION") || querySplit[1].trim().equalsIgnoreCase("TRANS"))) {
                throw new Exception("Unknown Statement detected.");
            }
            if (!dbManager.isTransactionInProgress()) {
                dbManager.startTransaction();
                String transactionSuccess = "Transaction has started";
                System.out.println("Transaction has started");
                EventLog eventLog = new EventLog("Transaction has started");
                LogIO.writeToEventLog(eventLog);
            } else {
                throw new Exception("Transaction already in progress!");
            }

        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    private void parseCommit(String query) {
        try {
            if (!dbManager.isTransactionInProgress()) {
                throw new Exception("No transaction in process.");
            }
//            dbManager.setAutoCommit(true);
            dbManager.commit();
            String commitSuccess = "The changes have been committed";
            System.out.println(commitSuccess);
            EventLog eventLog = new EventLog(commitSuccess);
            LogIO.writeToEventLog(eventLog);
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application Crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    private void parseRollback(String query) {
        try {
            if (!dbManager.isTransactionInProgress()) {
                throw new Exception("No transaction in process.");
            }
            String rollbackSuccess = "The rollback was successful";
            System.out.println(rollbackSuccess);
            EventLog eventLog = new EventLog(rollbackSuccess);
            dbManager.rollback();
        } catch (Exception e) {
            System.out.println(e);
            EventLog eventLog = new EventLog("Application Crashed");
            LogIO.writeToEventLog(eventLog);
        }
    }

    // ----------- HELPER FUNCTIONS -----------
    private List<Column> parseColumns(String query) {
        Matcher matcher = Pattern.compile("\\(\\s*(.*?)\\s*\\)").matcher(query);
        String columnNamesString = "";
        List<Column> parsedColumns = new ArrayList<>();
        if (matcher.find()) {

            columnNamesString = matcher.group(1);
            columnNamesString = String.join(" ", columnNamesString.split("\\s+"));
            columnNamesString = String.join(",", columnNamesString.split(",\\s+"));
            for (String columnEntry : columnNamesString.split(",")) {
                Column column = new Column();
                String[] columnEntrySplit = columnEntry.split(" ");
                column.setName(columnEntrySplit[0].trim());
                column.setType(columnEntrySplit[1].trim());
                if (columnEntrySplit.length > 3) {
                    if (columnEntrySplit[2].trim().equalsIgnoreCase("PRIMARY")) {
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

    public ArrayList<String> getColumnValues(String query) {
        ArrayList<String> columnValues = new ArrayList<String>();
        Matcher queryMatcher = Pattern.compile("\\((.*?)\\)").matcher(query);
        int count = 0;
        queryMatcher.find();
        if (queryMatcher.find()) {
            for (String columnValue : queryMatcher.group(1).split(",")) {
                columnValues.add(columnValue.trim());
            }
        } else {
            System.out.println("Incorrect syntax");
            System.exit(0);
        }

        return columnValues;
    }

    private Map<String, String> getMapFromStringArray(String[] mappedStrings) {
        Map<String, String> mappedData = new HashMap<>();
        for (String mappedString : mappedStrings) {
            String[] toUpdateSplit = mappedString.split("=");
            mappedData.put(toUpdateSplit[0].trim().replaceAll("\'|\"", ""), toUpdateSplit[1].trim().replaceAll("\'|\"", ""));
        }
        return mappedData;
    }

    private String removeSemiColon(String query) {
        if (query.charAt(query.length() - 1) == ';') {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }
}
