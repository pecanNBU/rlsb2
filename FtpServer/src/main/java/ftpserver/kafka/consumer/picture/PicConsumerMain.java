package ftpserver.kafka.consumer.picture;

import ftpserver.util.Utils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PicConsumerMain {
    private static Logger log = Logger.getLogger(PicConsumerMain.class);
    private static File resourceFile;
    private static Properties propers = new Properties();
    public static void main(String args[]) {
        try {
            resourceFile = Utils.loadResourceFile("consumer-picture.properties");
            if (resourceFile != null) {
                propers.load(new FileInputStream(resourceFile));
            }
            PicConsumerGroup consumerGroup = new PicConsumerGroup(propers);
            consumerGroup.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
