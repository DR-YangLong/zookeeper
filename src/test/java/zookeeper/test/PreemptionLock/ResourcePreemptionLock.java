package zookeeper.test.preemptionlock;

import zookeeper.one.ZkDao;
import zookeeper.one.ZkDaoImpl;

/**
 * package: zookeeper.test <br/>
 * functional describe:资源抢占锁，场景：秒杀，服务部署在多台服务器上，只考虑应用层面的设计。只有定额的资源，此时抢锁进行资源判断然后进行资源操作<br/>
 * 原理：所有服务在zk固定目录下创建相同节点，创建成功的抢到锁，可以进行业务，没有创建成功的等待通知，抢到锁的服务业务执行完成后释放锁（删除节点），其余的服务再抢锁。<br/>
 * 优化：资源数为0后余下的服务不再进行锁的争抢，直接结束业务<br/>
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/5/10 22:52
 */
public class ResourcePreemptionLock {
    public static void main(String[] args) {
        String parentPath = "sourcelock";
        String sourcePath="/sourcenum";
        ZkDao zkDao = new ZkDaoImpl(parentPath);
        //初始化
        ResourceLockThread lockThread1 = new ResourceLockThread();
        lockThread1.setZkDao(zkDao);
        lockThread1.setServerName("服务"+System.currentTimeMillis());
        lockThread1.setSourcePath(sourcePath);
        //启动
        lockThread1.start();
    }
}
