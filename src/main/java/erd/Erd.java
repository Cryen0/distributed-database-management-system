package erd;

import model.Column;
import model.Table;
import services.ScpHelper;
import services.io.ExportIO;
import services.io.TableIO;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class Erd {
    public Properties configProperties;

    public Erd() {
        try {
            configProperties = new Properties();
            InputStream fileInputStream = ScpHelper.class.getClassLoader().getResourceAsStream("config.properties");
            configProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void generateErd(String dbName) throws IOException {

        List<Table> tables = ExportIO.getTableList(dbName);
        HashMap<Integer, HashMap<String, Boolean>> columnDetailForDb = new HashMap<Integer, HashMap<String, Boolean>>();

        //final output
        ArrayList<String> output = new ArrayList<String>();

        StringBuilder finalString = new StringBuilder("================ERD====================");
        finalString.append(System.getProperty("line.separator"));


        //tables
        for (int i = 0; i < tables.size(); i++) {
            Table t = TableIO.readTable(tables.get(i).getName().split(".txt")[0], false);

            //get column list
            List<Column> columnList = t.getColumnList();
            StringBuilder temp = new StringBuilder("tableName: " + tables.get(i).getName());
            temp.append(System.getProperty("line.separator"));
            temp.append("Format : Column_name, type, isPrimaryKey");
            temp.append(System.getProperty("line.separator"));
            temp.append("Columns :");
            temp.append(System.getProperty("line.separator"));

            HashMap<String, Boolean> columnDetailForTable = new HashMap<String, Boolean>();

            //get name,isPk for the single table
            for (int j = 0; j < columnList.size(); j++) {
                columnDetailForTable.put(columnList.get(j).getName(), columnList.get(j).isPrimary());
                String primaryKey = columnList.get(j).isPrimary() ? "Primary Key" : "";
                temp.append(j + 1 + "  " + columnList.get(j).getName() + "   " + columnList.get(j).getType() + "  " + primaryKey);
                temp.append(System.getProperty("line.separator"));
            }

            finalString.append(temp);
            finalString.append(System.getProperty("line.separator"));

            //add into the main hashmap
            columnDetailForDb.put(i + 1, columnDetailForTable);
        }


        for (int k = 1; k <= columnDetailForDb.size(); k++) {
            //single hashmap from list
            HashMap<String, Boolean> current = columnDetailForDb.get(k);

            //another tables
            for (int l = k + 1; l <= columnDetailForDb.size(); l++) {
                HashMap<String, Boolean> nextTable = columnDetailForDb.get(l);
                //comparison between current(name,isPrimary) ==  next(name,isPrimary)

                for (String key : current.keySet()) {
                    //key and value a pair of current
                    String colNameForCurrent = key;
                    Boolean isPrimaryKeyForCurrent = current.get(key);

                    // get next(name,isPrimary)
                    for (String keyForNext : nextTable.keySet()) {
                        String colNameForNext = keyForNext;
                        Boolean isPrimaryKeyForNext = nextTable.get(keyForNext);
                        if (colNameForCurrent.equals(colNameForNext) && isPrimaryKeyForCurrent && isPrimaryKeyForNext) {
                            String op = tables.get(k - 1).getName() + " ==> " + tables.get(l - 1).getName() + "    relation  " + "1:1";
                            //1:1
                            output.add(op);
                        } else if (colNameForCurrent.equals(colNameForNext) && isPrimaryKeyForCurrent && !isPrimaryKeyForNext) {
                            //1:N
                            String op = tables.get(k - 1).getName() + " ==> " + tables.get(l - 1).getName() + "    relation  " + "1:N";
                            output.add(op);
                        } else if (colNameForCurrent.equals(colNameForNext) && !isPrimaryKeyForCurrent && !isPrimaryKeyForNext) {
                            //N:N
                            String op = tables.get(k - 1).getName() + " ==> " + tables.get(l - 1).getName() + "    relation  " + "N:N";
                            output.add(op);
                        }
                    }
                }

            }

        }

        finalString.append("-------------Relation Diagram-------------");
        finalString.append(System.getProperty("line.separator"));

        //print the erd
        for (int p = 0; p < output.size(); p++) {
            finalString.append(output.get(p));
            finalString.append(System.getProperty("line.separator"));
        }

        FileWriter myWriter = new FileWriter("dump/" + dbName + "_erd.txt");
        myWriter.write(String.valueOf(finalString));
        myWriter.close();
    }

}
