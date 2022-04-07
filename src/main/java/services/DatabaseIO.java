package services;

import java.io.IOException;
import java.util.ArrayList;

public class DatabaseIO {
    public void createTable(String tableName, ArrayList<String> columnNames) throws IOException {

//            File localCreate = new File(System.getProperty("user.dir") + "/assets/database/" + tableName);
//            if(localCreate.createNewFile()){
//                System.out.println("Table created");
//                FileWriter writer = new FileWriter(localCreate + DatabaseSetting.CURRENT_DATABASE + tableName, true);
//                String columnNamesString = "";
//                for(String columnName : columnNames){
//                    columnNamesString += columnName + "³";
//                }
//                writer.write(columnNamesString.substring(0,columnNamesString.length() - 1) + "\n");;
//                writer.close();
//            } else {
//                System.out.println("Error creating table");
//            }
        }

}
