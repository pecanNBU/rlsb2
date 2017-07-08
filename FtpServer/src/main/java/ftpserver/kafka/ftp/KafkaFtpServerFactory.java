package ftpserver.kafka.ftp;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftpletcontainer.impl.DefaultFtpletContainer;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.message.MessageResource;

import java.util.Map;

public class KafkaFtpServerFactory extends FtpServerFactory{
    private KafkaFtpServerContext kafkaFtpServerContext;

    public KafkaFtpServerFactory() {
        kafkaFtpServerContext = new KafkaFtpServerContext();
    }


    public FtpServer createServer() {
        return new DefaultFtpServer(kafkaFtpServerContext);
    }


    public Map<String, Listener> getListeners() {
        return kafkaFtpServerContext.getListeners();
    }


    public Listener getListener(final String name) {
        return kafkaFtpServerContext.getListener(name);
    }


    public void addListener(final String name, final Listener listener) {
        kafkaFtpServerContext.addListener(name, listener);
    }


    public void setListeners(final Map<String, Listener> listeners) {
        kafkaFtpServerContext.setListeners(listeners);
    }


    public Map<String, Ftplet> getFtplets() {
        return kafkaFtpServerContext.getFtpletContainer().getFtplets();
    }

    public void setFtplets(final Map<String, Ftplet> ftplets) {
        kafkaFtpServerContext.setFtpletContainer(new DefaultFtpletContainer(ftplets));
    }

    public UserManager getUserManager() {
        return kafkaFtpServerContext.getUserManager();
    }

    public void setUserManager(final UserManager userManager) {
        kafkaFtpServerContext.setUserManager(userManager);
    }

    public FileSystemFactory getFileSystem() {
        return kafkaFtpServerContext.getFileSystemManager();
    }

    public void setFileSystem(final FileSystemFactory fileSystem) {
        kafkaFtpServerContext.setFileSystemManager(fileSystem);
    }

    public CommandFactory getCommandFactory() {
        return kafkaFtpServerContext.getCommandFactory();
    }


    public void setCommandFactory(final CommandFactory commandFactory) {
        kafkaFtpServerContext.setCommandFactory(commandFactory);
    }

    public MessageResource getMessageResource() {
        return kafkaFtpServerContext.getMessageResource();
    }

    public void setMessageResource(final MessageResource messageResource) {
        kafkaFtpServerContext.setMessageResource(messageResource);
    }

    public ConnectionConfig getConnectionConfig() {
        return kafkaFtpServerContext.getConnectionConfig();
    }

    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        kafkaFtpServerContext.setConnectionConfig(connectionConfig);
    }
}
