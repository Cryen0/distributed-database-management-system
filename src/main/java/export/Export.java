package export;


import model.Column;
import model.Record;
import model.Table;
import services.io.ExportIO;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Export {
    public void export(String db) throws IOException{
        String line;
        String FinalQuery = "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
                "START TRANSACTION;\n" +
                "SET time_zone = \"+00:00\";\n\n\n\n";
        List<Table> allTables = ExportIO.getTableList(db);
        for (Table table : allTables) {
            ArrayList<String> primarykeys = new ArrayList<>();
            List<Column> c = table.getColumnList();
            String[][] formats = new String[c.size()][2];
            String createqry = "CREATE TABLE IF NOT EXIST " + table.getName() + "( ";
            String insertqry = "";
            for (int j = 0; j < c.size(); j++) {
                if (j != 0)
                    createqry = createqry + ", ";
                formats[j][0] = c.get(j).getName();
                formats[j][1] = c.get(j).getType();
                if (c.get(j).isPrimary())
                    primarykeys.add(c.get(j).getName());
                if (c.get(j).getType().equals("varchar") || c.get(j).getType().equals("char"))
                    formats[j][1] += "(255)";
                createqry = createqry + formats[j][0] + " " + formats[j][1];
                createqry = createqry + " NULL";
            }
            for (int j = 0; j < primarykeys.size(); j++) {
                if (j == 0)
                    createqry = createqry + " , PRIMARY KEY ( " + primarykeys.get(j);
                else
                    createqry = createqry + ", " + primarykeys.get(j);
            }
            if (primarykeys.size() != 0)
                createqry = createqry + ")";
            createqry = createqry + ");\n";
            for (Record record : table.getRecordList()) {
                List<String> columnval = record.getValues();
                if (insertqry.equals(""))
                    insertqry = insertqry + "INSERT INTO " + table.getName() + " VALUES\n";
                insertqry = insertqry + "(";
                int j = 0;
                for (String columnrecord : columnval) {
                    if (j != 0)
                        insertqry = insertqry + ", ";
                    if (formats[j][1].equals("varchar(255)") || formats[j][1].equals("char(255)") || formats[j][1].equals("text"))
                        insertqry = insertqry + "\"";
                    insertqry = insertqry + columnrecord + "";
                    if (formats[j][1].equals("varchar(255)") || formats[j][1].equals("char(255)") || formats[j][1].equals("text"))
                        insertqry = insertqry + "\" ";
                    j++;
                }
                insertqry += "),\n";
            }
            insertqry = insertqry.substring(0, insertqry.length() - 2) + ";\n\n";
            FinalQuery += createqry + insertqry;
        }
        FinalQuery += "\nCOMMIT;";
        PrintWriter wrfile = new PrintWriter(new File("dump/"+db+"_dump.sql"));
        wrfile.write(FinalQuery);
        wrfile.close();
    }
}
