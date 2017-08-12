package com.hzgc.ftpserver.local;


import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.*;
import org.apache.ftpserver.util.BaseProperties;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class LocalPropertiesUserManager extends AbstractUserManager implements Serializable {

    private final Logger LOG = LoggerFactory
            .getLogger(LocalPropertiesUserManager.class);

    private final static String PREFIX = "com.hzgc.ftpserver.user.";

    private BaseProperties userDataProp;

    private File userDataFile;

    private URL userUrl;

    public LocalPropertiesUserManager(PasswordEncryptor passwordEncryptor,
                                      File userDataFile, String adminName) {
        super(adminName, passwordEncryptor);

        loadFromFile(userDataFile);
    }

    public LocalPropertiesUserManager(PasswordEncryptor passwordEncryptor,
                                      URL userDataPath, String adminName) {
        super(adminName, passwordEncryptor);

        loadFromUrl(userDataPath);
    }

    private void loadFromFile(File userDataFile) {
        try {
            userDataProp = new BaseProperties();

            if (userDataFile != null) {
                LOG.info("File configured, will try loading");

                if (userDataFile.exists()) {
                    this.userDataFile = userDataFile;

                    LOG.info("File found on file system");
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(userDataFile);
                        userDataProp.load(fis);
                    } finally {
                        IoUtils.close(fis);
                    }
                } else {
                    // try loading it from the classpath
                    LOG
                            .info("File not found on file system, try loading from classpath");

                    InputStream is = getClass().getClassLoader()
                            .getResourceAsStream(userDataFile.getPath());

                    if (is != null) {
                        try {
                            userDataProp.load(is);
                        } finally {
                            IoUtils.close(is);
                        }
                    } else {
                        throw new FtpServerConfigurationException(
                                "User data file specified but could not be located, "
                                        + "neither on the file system or in the classpath: "
                                        + userDataFile.getPath());
                    }
                }
            }
        } catch (IOException e) {
            throw new FtpServerConfigurationException(
                    "Error loading user data file : " + userDataFile, e);
        }
    }

    private void loadFromUrl(URL userDataPath) {
        try {
            userDataProp = new BaseProperties();

            if (userDataPath != null) {
                LOG.info("URL configured, will try loading");

                userUrl = userDataPath;
                InputStream is = null;

                is = userDataPath.openStream();

                try {
                    userDataProp.load(is);
                } finally {
                    IoUtils.close(is);
                }
            }
        } catch (IOException e) {
            throw new FtpServerConfigurationException(
                    "Error loading user data resource : " + userDataPath, e);
        }
    }

    public void refresh() {
        synchronized (userDataProp) {
            if (userDataFile != null) {
                LOG.info("Refreshing user manager using file: "
                        + userDataFile.getAbsolutePath());
                loadFromFile(userDataFile);

            } else {
                //file is null, must have been created using URL
                LOG.info("Refreshing user manager using URL: "
                        + userUrl.toString());
                loadFromUrl(userUrl);
            }
        }
    }

    public File getFile() {
        return userDataFile;
    }

    public synchronized void save(User usr) throws FtpException {
        // null value check
        if (usr.getName() == null) {
            throw new NullPointerException("User name is null.");
        }
        String thisPrefix = PREFIX + usr.getName() + '.';

        // set other properties
        userDataProp.setProperty(thisPrefix + ATTR_PASSWORD, getPassword(usr));

        String home = usr.getHomeDirectory();
        if (home == null) {
            home = "/";
        }
        userDataProp.setProperty(thisPrefix + ATTR_HOME, home);
        userDataProp.setProperty(thisPrefix + ATTR_ENABLE, usr.getEnabled());
        userDataProp.setProperty(thisPrefix + ATTR_WRITE_PERM, usr
                .authorize(new WriteRequest()) != null);
        userDataProp.setProperty(thisPrefix + ATTR_MAX_IDLE_TIME, usr
                .getMaxIdleTime());

        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest) usr
                .authorize(transferRateRequest);

        if (transferRateRequest != null) {
            userDataProp.setProperty(thisPrefix + ATTR_MAX_UPLOAD_RATE,
                    transferRateRequest.getMaxUploadRate());
            userDataProp.setProperty(thisPrefix + ATTR_MAX_DOWNLOAD_RATE,
                    transferRateRequest.getMaxDownloadRate());
        } else {
            userDataProp.remove(thisPrefix + ATTR_MAX_UPLOAD_RATE);
            userDataProp.remove(thisPrefix + ATTR_MAX_DOWNLOAD_RATE);
        }

        // request that always will succeed
        ConcurrentLoginRequest concurrentLoginRequest = new ConcurrentLoginRequest(
                0, 0);
        concurrentLoginRequest = (ConcurrentLoginRequest) usr
                .authorize(concurrentLoginRequest);

        if (concurrentLoginRequest != null) {
            userDataProp.setProperty(thisPrefix + ATTR_MAX_LOGIN_NUMBER,
                    concurrentLoginRequest.getMaxConcurrentLogins());
            userDataProp.setProperty(thisPrefix + ATTR_MAX_LOGIN_PER_IP,
                    concurrentLoginRequest.getMaxConcurrentLoginsPerIP());
        } else {
            userDataProp.remove(thisPrefix + ATTR_MAX_LOGIN_NUMBER);
            userDataProp.remove(thisPrefix + ATTR_MAX_LOGIN_PER_IP);
        }

        saveUserData();
    }

    private void saveUserData() throws FtpException {
        if (userDataFile == null) {
            return;
        }

        File dir = userDataFile.getAbsoluteFile().getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            String dirName = dir.getAbsolutePath();
            throw new FtpServerConfigurationException(
                    "Cannot create directory for user data file : " + dirName);
        }

        // save user data
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(userDataFile);
            userDataProp.store(fos, "Generated file - don't edit (please)");
        } catch (IOException ex) {
            LOG.error("Failed saving user data", ex);
            throw new FtpException("Failed saving user data", ex);
        } finally {
            IoUtils.close(fos);
        }
    }

    public void delete(String usrName) throws FtpException {
        // remove entries from properties
        String thisPrefix = PREFIX + usrName + '.';
        Enumeration<?> propNames = userDataProp.propertyNames();
        ArrayList<String> remKeys = new ArrayList<String>();
        while (propNames.hasMoreElements()) {
            String thisKey = propNames.nextElement().toString();
            if (thisKey.startsWith(thisPrefix)) {
                remKeys.add(thisKey);
            }
        }
        Iterator<String> remKeysIt = remKeys.iterator();
        while (remKeysIt.hasNext()) {
            userDataProp.remove(remKeysIt.next());
        }

        saveUserData();
    }

    private String getPassword(User usr) {
        String name = usr.getName();
        String password = usr.getPassword();

        if (password != null) {
            password = getPasswordEncryptor().encrypt(password);
        } else {
            String blankPassword = getPasswordEncryptor().encrypt("");

            if (doesExist(name)) {
                String key = PREFIX + name + '.' + ATTR_PASSWORD;
                password = userDataProp.getProperty(key, blankPassword);
            } else {
                password = blankPassword;
            }
        }
        return password;
    }

    public String[] getAllUserNames() {
        // get all user names
        String suffix = '.' + ATTR_HOME;
        ArrayList<String> ulst = new ArrayList<String>();
        Enumeration<?> allKeys = userDataProp.propertyNames();
        int prefixlen = PREFIX.length();
        int suffixlen = suffix.length();
        while (allKeys.hasMoreElements()) {
            String key = (String) allKeys.nextElement();
            if (key.endsWith(suffix)) {
                String name = key.substring(prefixlen);
                int endIndex = name.length() - suffixlen;
                name = name.substring(0, endIndex);
                ulst.add(name);
            }
        }

        Collections.sort(ulst);
        return ulst.toArray(new String[0]);
    }

    public User getUserByName(String userName) {
        if (!doesExist(userName)) {
            return null;
        }

        String baseKey = PREFIX + userName + '.';
        BaseUser user = new BaseUser();
        user.setName(userName);
        user.setEnabled(userDataProp.getBoolean(baseKey + ATTR_ENABLE, true));
        user.setHomeDirectory(userDataProp
                .getProperty(baseKey + ATTR_HOME, "/"));

        List<Authority> authorities = new ArrayList<Authority>();

        if (userDataProp.getBoolean(baseKey + ATTR_WRITE_PERM, false)) {
            authorities.add(new WritePermission());
        }

        int maxLogin = userDataProp.getInteger(baseKey + ATTR_MAX_LOGIN_NUMBER,
                0);
        int maxLoginPerIP = userDataProp.getInteger(baseKey
                + ATTR_MAX_LOGIN_PER_IP, 0);

        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

        int uploadRate = userDataProp.getInteger(
                baseKey + ATTR_MAX_UPLOAD_RATE, 0);
        int downloadRate = userDataProp.getInteger(baseKey
                + ATTR_MAX_DOWNLOAD_RATE, 0);

        authorities.add(new TransferRatePermission(downloadRate, uploadRate));

        user.setAuthorities(authorities);

        user.setMaxIdleTime(userDataProp.getInteger(baseKey
                + ATTR_MAX_IDLE_TIME, 0));

        return user;
    }

    public boolean doesExist(String name) {
        String key = PREFIX + name + '.' + ATTR_HOME;
        return userDataProp.containsKey(key);
    }

    public User authenticate(Authentication authentication)
            throws AuthenticationFailedException {
        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;

            String user = upauth.getUsername();
            String password = upauth.getPassword();

            if (user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            if (password == null) {
                password = "";
            }

            String storedPassword = userDataProp.getProperty(PREFIX + user
                    + '.' + ATTR_PASSWORD);

            if (storedPassword == null) {
                // user does not exist
                throw new AuthenticationFailedException("Authentication failed");
            }

            if (getPasswordEncryptor().matches(password, storedPassword)) {
                return getUserByName(user);
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }

        } else if (authentication instanceof AnonymousAuthentication) {
            if (doesExist("anonymous")) {
                return getUserByName("anonymous");
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }
        } else {
            throw new IllegalArgumentException(
                    "Authentication not supported by this user manager");
        }
    }

    public synchronized void dispose() {
        if (userDataProp != null) {
            userDataProp.clear();
            userDataProp = null;
        }
    }

}
