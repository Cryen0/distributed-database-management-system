package analysis;

import model.QueryLog;
import services.io.LogIO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Analytics {


    private List<QueryLog> lq;
    private String finalOutcome="";

    public void generateAnalytics()
    {
        countQueries();
        countOperationTableVise();

        //writing to a file
        try {
            FileWriter myWriter = new FileWriter("logs/Analytics.txt");
            myWriter.write(finalOutcome);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //    // the return will be like "Total 9 Update operations are performed on Employee table of DB1"
    public void countOperationTableVise()
    {
        lq = LogIO.readQueryLog(false);
        lq.addAll(LogIO.readQueryLog(true));

        List<String> op = new ArrayList<String>();
        op.add("insert");
        op.add("select");
        op.add("update");
        op.add("delete");

        for(String opr: op)
        {
            for(String db: getDBSet())
            {
                for(String table: getTables(db))
                {
                    int cnt = getOperationCount(opr,db,table);
                    if(cnt>0)
                    {
                        String s = "Total "+cnt+" "+" "+opr+" operations are performed on "+table+" table of "+db;
                        finalOutcome+=s+"\n";
                        System.out.println(s);
                    }
                }
            }
        }
    }



//    // the return will be like "user SDEY submitted 10 queries for DB1 running on Virtual Machine 1"
    public void countQueries()
    {
        lq = LogIO.readQueryLog(false);
        lq.addAll(LogIO.readQueryLog(true));

        for(String user: getUserIDSet())
        {
            for(String db: getDBSet())
            {
                for(String vm: getVms())
                {
                    int cnt = getQueryCount(vm,db,user);
                    if(cnt>0)
                    {
                        String s = "user " + user + " submitted " + cnt + " queries for " + db + " running on " + vm;
                        finalOutcome+=s+"\n";
                        System.out.println(s);
                    }
                }
            }
        }
    }

    //getting all tables for database
    private HashSet<String> getTables(String DB)
    {
        HashSet<String> hs = new HashSet<String>();

        for(QueryLog q: lq) {

            if(q.getDb().trim().toLowerCase(Locale.ROOT).equals(DB.trim().toLowerCase(Locale.ROOT)))
            {
                String table = q.getTableName().trim();
                hs.add(table);
            }
        }

        return hs;
    }


    //getting all users
    private HashSet<String> getUserIDSet()
    {
        HashSet<String> hs = new HashSet<String>();

            for(QueryLog q: lq) {
                String user = q.getUserId().trim();
                hs.add(user);
            }

        return hs;
    }
//
    //getting all DBs
    private HashSet<String> getDBSet()
    {
        HashSet<String> hs = new HashSet<String>();

        for(QueryLog q: lq) {
            String user = q.getDb().trim();
            hs.add(user);
        }
        return hs;
    }

//    //getting all VMs
    private HashSet<String> getVms()
    {
        HashSet<String> hs = new HashSet<String>();

        for(QueryLog q: lq) {
            String user = q.getVirtualMachine().trim();
            hs.add(user);
        }

        return hs;
    }

//    //getting count using vm, db and userId
    private int getQueryCount(String vm,String db,String userId)
    {
        int cnt=0;


            for(QueryLog q:lq) {

                boolean a = q.getVirtualMachine().trim().equals(vm);
                boolean b = q.getUserId().trim().equals(userId);
                boolean c = q.getDb().trim().equals(db);
                if(a&&b&&c)
                {
                    cnt++;
                }
            }

        return cnt;
    }

    //    //getting count using vm, db and userId
    private int getOperationCount(String opr,String db,String table)
    {
        int cnt=0;


        for(QueryLog q:lq) {

            boolean a = q.getQuery().trim().split("\\s+")[0].toLowerCase(Locale.ROOT).equals(opr);
            boolean b = q.getTableName().trim().equals(table);
            boolean c = q.getDb().trim().equals(db);
            if(a&&b&&c)
            {
                cnt++;
            }
        }

        return cnt;
    }



}
