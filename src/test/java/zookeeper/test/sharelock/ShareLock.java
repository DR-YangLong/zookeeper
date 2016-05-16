package zookeeper.test.sharelock;

import zookeeper.one.ZkDao;
import zookeeper.one.ZkDaoImpl;

import java.util.Random;

/**
 * package: zookeeper.test.sharelock <br/>
 * functional describe:
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/5/12 12:08
 */
public class ShareLock {
    public static void main(String[] args) {
        String parent = "/sharelock";
        String scheme = "sourcelock";
        Random random=new Random();
        int n=random.nextInt(3);
        String lockType = n>1?LockType.WRITE_LOCK.getType():LockType.READ_LOCK.getType();
        ZkDao zkDao = new ZkDaoImpl(scheme);
        ShareLockThread shareLock = new ShareLockThread();
        shareLock.setServerName(""+System.currentTimeMillis());
        shareLock.setLockType(lockType);
        shareLock.setLockPath(parent);
        shareLock.setZkDao(zkDao);
        shareLock.start();
    }
}
