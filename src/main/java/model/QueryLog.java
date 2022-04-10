package model;

public class QueryLog {

    private String virtualMachine;
    private String userId;
    private String db;
    private String time;
    private String query;
    private String tableName;

    public QueryLog() {
    }

    public QueryLog(String virtualMachine, String userId, String db, String time, String query, String tableName) {
        this.virtualMachine = virtualMachine;
        this.userId = userId;
        this.db = db;
        this.time = time;
        this.query = query;
        this.tableName = tableName;
    }

    /*************************************************************************
     * GETTERS AND SETTERS
     *************************************************************************/

    public String getVirtualMachine() {
        return virtualMachine;
    }

    public void setVirtualMachine(String virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTableName() {
        return tableName == null ? "" : this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /*************************************************************************
     * STATIC UTILS
     *************************************************************************/

    public static QueryLog getQueryLog(String queryLogString) {
        QueryLog queryLog = new QueryLog();
        String[] logValues = queryLogString.split("³");
        queryLog.setVirtualMachine(logValues[0]);
        queryLog.setUserId(logValues[1]);
        queryLog.setDb(logValues[2]);
        queryLog.setTime(logValues[3]);
        queryLog.setQuery(logValues[4]);
        queryLog.setTableName(logValues[5]);
        return queryLog;
    }

    public String getQueryLogString() {
        StringBuilder builder = new StringBuilder();
        builder.append(virtualMachine).append("³");
        builder.append(userId).append("³");
        builder.append(db).append("³");
        builder.append(time).append("³");
        builder.append(query).append("³");
        builder.append(tableName);

        return builder.toString();
    }


}
