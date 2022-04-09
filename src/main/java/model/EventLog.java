package model;
public class EventLog {

    private String statement;

    public EventLog() {
    }

    public EventLog(String statement) {
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