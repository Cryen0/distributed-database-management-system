package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class Table {

    private String name;
    private List<Column> columnList;
    private List<Record> recordList;

    public Table() {
        this.columnList = new ArrayList<>();
        this.recordList = new ArrayList<>();
    }

    /*************************************************************************
     * GETTERS AND SETTERS
     *************************************************************************/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    public boolean columnExists(String columnName) {
        return columnList.stream().anyMatch(column -> column.getName().equals(columnName));
    }

    /*************************************************************************
     * UTILS
     *************************************************************************/

    public Map<String, String> getMappedRecord(Record record) {
        Map<String, String> recordMap = new HashMap<>();

        for (int i = 0; i < columnList.size(); i++) {
            recordMap.put(columnList.get(i).getName(), record.getValues().get(i));
        }
        return recordMap;
    }

    public List<Map<String, String>> getMappedRecordList() {

        List<Map<String, String>> mappedRecordList = new ArrayList<>();
        for (Record record : recordList) {
            mappedRecordList.add(getMappedRecord(record));
        }

        return mappedRecordList;
    }

    public boolean canInsertRecord(Record record) {
        int primaryKeyIndex = 0;
        for (int i = 0; i < this.columnList.size(); i++) {
            if (columnList.get(i).isPrimary()) {
                primaryKeyIndex = i;
                break;
            }
        }
        // Checking if primary key exists
        for (Record eachRecord : this.recordList) {
            if (eachRecord.getValues().get(primaryKeyIndex).equals(record.getValues().get(primaryKeyIndex))) {
                return false;
            }
        }
        return true;
    }

    public int getColumnIndex(String columnName) {
        int columnIndex = 0;
        for (int i = 0; i < this.columnList.size(); i++) {
            if (columnList.get(i).isPrimary()) {
                columnIndex = i;
                break;
            }
        }
        return columnIndex;
    }

    public static Table merge(Table table1, Table table2) {
        Table mergedTable = new Table();
        if(!table1.getName().equals(table2.getName()) || !(table1.getColumnList().size() == table2.getColumnList().size())) {
            throw new IllegalArgumentException("Tables must be of the same type and have the same columns");
        }

        List<Record> mergedRecordList = new ArrayList<>();
        mergedRecordList.addAll(table1.getRecordList());
        mergedRecordList.addAll(table2.getRecordList());

        mergedTable.setName(table1.getName());
        mergedTable.setColumnList(table1.getColumnList());
        mergedTable.setRecordList(mergedRecordList);

        return mergedTable;
    }
}