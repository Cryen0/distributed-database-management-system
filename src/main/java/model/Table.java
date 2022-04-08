package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}