package zookeeper.test.PreemptionLock;

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
    //资源数
    public static Integer num = 2;

    public static void main(String[] args) {
        String parentPath = "sourcelock";
        ZkDao zkDao = new ZkDaoImpl(parentPath);
        //初始化
        ResourceLockThread lockThread1 = new ResourceLockThread();
        lockThread1.setZkDao(zkDao);
        lockThread1.setServerName("服务1");
        ResourceLockThread lockThread2 = new ResourceLockThread();
        lockThread2.setZkDao(zkDao);
        lockThread2.setServerName("服务2");
        ResourceLockThread lockThread3 = new ResourceLockThread();
        lockThread3.setZkDao(zkDao);
        lockThread3.setServerName("服务3");
        //启动
        lockThread1.start();
        lockThread2.start();
        lockThread3.run();
    }
}
