package model;

public class GeneralLog {
    private String statement;

    public GeneralLog() {
    }

    public GeneralLog(String time, String virtualMachine, int dbCount, int tableCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(time)
                .append(": ")
                .append(virtualMachine)
                .append("contains  ")
                .append(dbCount)
                .append("database and ")
                .append("contains  ")
                .append(tableCount)
                .append("table");
        this.statement = builder.toString();
    }

    public GeneralLog(String statement) {
        this.statement = statement;
    }

    /*************************************************************************
     * GETTERS AND SETTERS
     *************************************************************************/

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}