package services;

import com.jcraft.jsch.Session;
import model.Table;

public class DbManager {

    private static DbManager dbManager;

    private ScpHelper scpHelper;
    private Session session;
    private String currentDb;

    private DbManager() {
        this.scpHelper = new ScpHelper();
        this.session = scpHelper.getSession();
    }

    public static DbManager getInstance() {
        if (dbManager == null) {
            dbManager = new DbManager();
        }
        return dbManager;
    }

    /*************************************************************************
     * DB UTILS
     *************************************************************************/

    public String getCurrentDb() {
        return currentDb;
    }

    public void setCurrentDb(String currentDb) {
        this.currentDb = currentDb;
    }

    public boolean isCurrentDbSelected() {
        return this.currentDb != null;
    }

    public boolean dbExists(String dbName) {
        return true;
    }

    public boolean createDb(String dbName) {
        return true;
    }

    /*************************************************************************
     * TABLE UTILS
     *************************************************************************/

    public boolean tableExists() {
        return true;
    }

    public boolean createTable(Table table) {
        return true;
    }


}