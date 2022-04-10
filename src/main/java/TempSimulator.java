import services.DbManager;
import services.io.MetadataIO;

import java.io.IOException;

public class TempSimulator {
    public static void main(String[] args) throws IOException {

        DbManager dbManager = DbManager.getInstance();

        MetadataIO.updateMetaData();

        dbManager.disconnectSession();
    }
}
