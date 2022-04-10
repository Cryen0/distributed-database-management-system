package services.io;

import model.EventLog;
import model.GeneralLog;
import model.QueryLog;
import services.DbManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LogIO {

    private static DbManager dbManager = DbManager.getInstance();

    public static List<GeneralLog> readGeneralLog(boolean isRemote) {

        if (isRemote) {
            dbManager.fetchLog("general");
        }

        List<GeneralLog> generalLogList = new ArrayList<>();
        String filePath = getLogPath(isRemote) + "/" + "general.txt";
        File file = new File(filePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return generalLogList;
        }

        // Read all the records
        while (scanner.hasNext()) {
            String generalLogString = scanner.nextLine();
            generalLogList.add(new GeneralLog(generalLogString));
        }

        if (isRemote) {
            dbManager.cleanDirectory(getLogPath(true));
        }
        return generalLogList;
    }

    public static List<EventLog> readEventLog(boolean isRemote) {

        if (isRemote) {
            dbManager.fetchLog("event");
        }

        List<EventLog> eventLogList = new ArrayList<>();

        String filePath = getLogPath(isRemote) + "/" + "event.txt";
        File file = new File(filePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return eventLogList;
        }

        // Read all the records
        while (scanner.hasNext()) {
            String eventLogString = scanner.nextLine();
            eventLogList.add(new EventLog(eventLogString));
        }

        if (isRemote) {
            dbManager.cleanDirectory(getLogPath(true));
        }
        return eventLogList;
    }

    public static List<QueryLog> readQueryLog(boolean isRemote) {

        if (isRemote) {
            dbManager.fetchLog("query");
        }

        List<QueryLog> queryLogList = new ArrayList<>();

        String filePath = getLogPath(isRemote) + "/" + "query.txt";
        File file = new File(filePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return queryLogList;
        }

        // Read all the records
        while (scanner.hasNext()) {
            String queryLogString = scanner.nextLine();
            queryLogList.add(QueryLog.getQueryLog(queryLogString));
        }

        if (isRemote) {
            dbManager.cleanDirectory(getLogPath(true));
        }
        return queryLogList;
    }

    public static boolean writeToGeneralLog(GeneralLog generalLog) {

        // Insert will always be on local
        String filePath = getLogPath(false) + "/general.txt";
        File file = new File(filePath);

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write('\n'); // new line
            fileWriter.write(generalLog.getStatement());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean writeToEventLog(EventLog eventLog) {

        // Insert will always be on local
        String filePath = getLogPath(false) + "/event.txt";
        File file = new File(filePath);

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write('\n'); // new line
            fileWriter.write(eventLog.getStatement());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean writeToQueryLog(QueryLog queryLog) {

        // Insert will always be on local
        String filePath = getLogPath(false) + "/query.txt";
        File file = new File(filePath);

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write('\n'); // new line
            fileWriter.write(queryLog.getQueryLogString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getLogPath(boolean isRemote) {
        String logPath = "";
        if (isRemote) {
            logPath = dbManager.configProperties.getProperty("transLogsDir");
        } else {
            logPath = dbManager.configProperties.getProperty("logsDir");
        }
        return logPath;
    }
}