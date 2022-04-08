package model;

import java.util.ArrayList;
import java.util.List;

public class Column {

    private String name;
    private String type;

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

    /*************************************************************************
     * STATIC UTILS
     *************************************************************************/

    public static Column getColumn(String[] columnValues) {
        Column column = new Column();
        if (columnValues != null && columnValues.length == 2) {
            column.setName(columnValues[0]);
            column.setType(columnValues[1]);
        }
        return column;
    }

    public static String getColumnString(Column column) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(column.getName())
                .append("²")
                .append(column.getType());
        return builder.toString();
    }

    public static List<Column> getColumnList(String columnListString) {
        List<Column> columns = new ArrayList<>();
        if (columnListString != null || !columnListString.isEmpty()) {
            List<String> columnStringList = List.of(columnListString.split("³"));
            for (String columnStr : columnStringList) {
                String[] columnValues = columnStr.split("²");
                Column column = Column.getColumn(columnValues);
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
