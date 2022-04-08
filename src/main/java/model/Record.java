package model;

import java.util.ArrayList;
import java.util.List;

public class Record {

    private List<String> values;

    /*************************************************************************
     * GETTERS AND SETTERS
     *************************************************************************/

    public List<String> getValues() {
        return values == null ? new ArrayList<>() : values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    /*************************************************************************
     * STATIC UTILS
     *************************************************************************/

    public static Record getRecord(String recordString) {

        Record record = new Record();
        if (recordString != null || !recordString.isEmpty()) {
            record.setValues(List.of(recordString.split("³")));
        }
        return record;
    }

    public static String getRecordString(List<String> record) {
        StringBuilder builder = new StringBuilder();

        String splitterPrefix = "";
        for (String value : record) {
            builder.append(splitterPrefix).append(value);
            splitterPrefix = "³";
        }

        return builder.toString();
    }
}
