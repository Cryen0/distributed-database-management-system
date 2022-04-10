import model.Column;
import model.Record;
import model.Table;
import services.DbManager;
import services.io.ExportIO;

import java.io.IOException;
import java.util.List;

public class TempSimulator {
    public static void main(String[] args) throws IOException {

        DbManager dbManager = DbManager.getInstance();

        List<Table> tableList = ExportIO.getTableList("School");

        for (Table table : tableList) {
            System.out.println("Table Name: " + table.getName());
            System.out.println("Column List: " + Column.getColumnListString(table.getColumnList()));
            for (Record record : table.getRecordList()) {
                System.out.println(Record.getRecordString(record.getValues()));
            }
        }

        dbManager.disconnectSession();
    }
}
