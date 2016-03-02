package zookeeper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import zookeeper.one.ZkDao;
import zookeeper.one.ZkDaoImpl;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        ZkDao dao=new ZkDaoImpl();
        dao.createNodeWithParent("/test/test1","ceshi".getBytes(),null,null);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }
}
