package model;

import java.util.List;

public class Table {

    private String name;
    private List<Column> columnList;
    private List<Record> recordList;

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
}