package com.hzgc.ftpserver.kafka.ftp;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;

public class KafkaFileSystemFactory implements FileSystemFactory, Serializable {
    private final Logger LOG = Logger.getLogger(KafkaFileSystemFactory.class);

    private boolean createHome;

    private boolean caseInsensitive;

    public boolean isCreateHome() {
        return createHome;
    }

    public void setCreateHome(boolean createHome) {
        this.createHome = createHome;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public FileSystemView createFileSystemView(User user) throws FtpException {
        synchronized (user) {
            // create home if does not exist
            if (createHome) {
                String homeDirStr = user.getHomeDirectory();
                File homeDir = new File(homeDirStr);
                if (homeDir.isFile()) {
                    LOG.warn("Not a directory :: " + homeDirStr);
                    throw new FtpException("Not a directory :: " + homeDirStr);
                }
                if ((!homeDir.exists()) && (!homeDir.mkdirs())) {
                    LOG.warn("Cannot create user home :: " + homeDirStr);
                    throw new FtpException("Cannot create user home :: "
                            + homeDirStr);
                }
            }

            FileSystemView fsView = new KafkaFileSystemView(user,
                    caseInsensitive);
            return fsView;
        }
    }
}
