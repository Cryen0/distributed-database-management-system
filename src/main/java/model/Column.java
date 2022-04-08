package model;

import java.util.ArrayList;
import java.util.List;

public class Column {

    private String name;
    private String type;
    private boolean isPrimary;

    public Column() {
    }

    public Column(String name, String type, boolean isPrimary) {
        this.name = name;
        this.type = type;
        this.isPrimary = isPrimary;
    }

    public Column(String[] columnValues) {
        if (columnValues != null && columnValues.length == 3) {
            this.name = columnValues[0];
            this.type = columnValues[1];
            this.isPrimary = Boolean.parseBoolean(columnValues[2]);
        }
    }

    /*************************************************************************
     * GETTERS AND SETTERS
     *************************************************************************/

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    /*************************************************************************
     * STATIC UTILS
     *************************************************************************/

    public static String getColumnString(Column column) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(column.getName())
                .append("²")
                .append(column.getType())
                .append("²")
                .append(column.isPrimary);
        return builder.toString();
    }

    public static List<Column> getColumnList(String columnListString) {
        List<Column> columns = new ArrayList<>();
        if (columnListString != null || !columnListString.isEmpty()) {
            List<String> columnStringList = List.of(columnListString.split("³"));
            for (String columnStr : columnStringList) {
                String[] columnValues = columnStr.split("²");
                Column column = new Column(columnValues);
                columns.add(column);
            }
        }
        return columns;
    }

    public static String getColumnListString(List<Column> columns) {
        StringBuilder builder = new StringBuilder();
        String splitterPrefix = "";
        for (Column column : columns) {
            builder.append(splitterPrefix).append(Column.getColumnString(column));
            splitterPrefix = "³";
        }

        return builder.toString();
    }
}