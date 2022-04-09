package services.io;

import model.EventLog;
import model.GeneralLog;
import model.QueryLog;
import model.Record;
import services.ScpHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class LogIO {

    public static List<GeneralLog> readGeneralLog(boolean isRemote) {

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
        return generalLogList;
    }

    public static List<EventLog> readEventLog(boolean isRemote) {

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
        return eventLogList;
    }

    public static List<QueryLog> readQueryLog(boolean isRemote) {

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
        return queryLogList;
    }

    public static boolean insert(String logType, Record record) {

        // Insert will always be on local
        String filePath = getLogPath(false) + "/" + logType + ".txt";
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

    public static String getLogPath(boolean isRemote) {
        String logPath = "";
        try {
            Properties configProperties = new Properties();
            InputStream fileInputStream = ScpHelper.class.getClassLoader().getResourceAsStream("config.properties");
            configProperties.load(fileInputStream);
            fileInputStream.close();
            if (isRemote) {
                logPath = configProperties.getProperty("transLogseDir");
            } else {
                logPath = configProperties.getProperty("logsDir");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return logPath;
    }
}