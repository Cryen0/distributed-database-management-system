package services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

public class DbManager {

    ScpHelper scpHelper;
    Session session;

    DbManager() {
        this.scpHelper = new ScpHelper();
        this.session = scpHelper.getSession();
    }

    public boolean dbExists() {
        ChannelSftp channel = scpHelper.getChannel(session);
        return true;
    }
}
