package zookeeper.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zookeeper.one.ZkDao;
import zookeeper.one.ZkDaoImpl;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        ZkDao dao = new ZkDaoImpl();
        String path = "/test/test1";
        //dao.createNodeWithParent(path,"ceshi".getBytes(),null,null);
        //dao.deleteNode(path,0);
        Stat stat = new Stat();
        byte[] data = dao.readData(path, stat);
        log.debug(new String(data));
        log.debug(stat.toString());
        List<String> list = dao.getChildren("/test", stat);
        if (list != null) {
            for (String child : list) {
                log.debug(child + "\n");
            }
        }
        log.debug(stat.toString());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
