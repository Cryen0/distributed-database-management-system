package export;
import java.io.*;
import java.util.ArrayList;


public class Export {


    // importing file
    static BufferedReader importfile(String file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        return br;
    }

    //return all file names
    static String[] getTableName(String location){
        File file = new File(location);
        File[] f = file.listFiles();
        String[] files = new String[f.length];
        for (int i = 0; i < f.length; i++) {
            files[i] = f[i].getName();
        }
        return files;
    }

    public void export(String db) throws IOException{
        //public static void main(String[] args) throws IOException{ String db = "temp";
        String[] files = getTableName("databases/"+db);
        String line;
        String FinalQuery = "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
                "START TRANSACTION;\n" +
                "SET time_zone = \"+00:00\";\n\n\n\n";
        for(int i=0; i< files.length; i++){
            //System.out.println(files[i]);
            ArrayList<String> primarykeys = new ArrayList<>();
            BufferedReader br = importfile("databases/"+db+"/"+files[i]);
            String tablename = files[i].split(".txt")[0];
            //System.out.println(tablename);
            line = br.readLine();
            String[] columns = line.split("³");
            String[][] formats = new String[columns.length][2];
            String createqry = "CREATE TABLE IF NOT EXIST " + tablename + "( ";
            String insertqry = "";
            for(int j =0; j< columns.length; j++){
                if(j!=0)
                    createqry = createqry + ", ";
                String[] split = columns[j].split("²");
                formats[j][0] = split[0];
                formats[j][1] = split[1];
                if(split[2].equals("true"))
                    primarykeys.add(split[0]);
                if(split[1].equals("varchar") || split[1].equals("char"))
                    formats[j][1] += "(255)";
                //System.out.println(formats[j][0] + " " + formats[j][1]);
                createqry = createqry + formats[j][0] + " " + formats[j][1];
                createqry = createqry + " NULL";
                //primary code
            }
            for(int j=0;j<primarykeys.size();j++){
                if(j==0)
                    createqry = createqry + " , PRIMARY KEY ( " + primarykeys.get(j);
                else
                    createqry = createqry + ", " + primarykeys.get(j);
            }
            if(primarykeys.size()!=0)
                createqry = createqry + ")";
            createqry = createqry +");\n";
            //System.out.print(createqry);
            line = br.readLine();
            while(line!=null){
                if(insertqry.equals(""))
                    insertqry = insertqry + "INSERT INTO " + tablename + " VALUES";
                insertqry = insertqry + "( ";
                columns = line.split("³");
                for(int j =0; j< columns.length; j++){
                    //System.out.println(formats[j][1]);
                    if(j!=0)
                        insertqry = insertqry + ", ";
                    if(formats[j][1].equals("varchar")||formats[j][1].equals("char")||formats[j][1].equals("text"))
                        insertqry = insertqry + "\"";
                    insertqry = insertqry + columns[j] + "";
                    if(formats[j][1].equals("varchar")||formats[j][1].equals("char")||formats[j][1].equals("text"))
                        insertqry = insertqry + "\" ";
                }
                line = br.readLine();
                if(line!=null)
                    insertqry = insertqry + "),\n";
                else
                    insertqry = insertqry + ");\n\n";
            }
            //System.out.print(insertqry);
            FinalQuery = FinalQuery + createqry + insertqry;
        }
        FinalQuery = FinalQuery + "\nCOMMIT;";
        PrintWriter wrfile = new PrintWriter(new File("dump/"+db+".sql"));
        wrfile.write(FinalQuery);
        wrfile.close();
    }
}
