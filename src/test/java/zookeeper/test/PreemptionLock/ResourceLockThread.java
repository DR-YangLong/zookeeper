package zookeeper.test.PreemptionLock;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zookeeper.common.PathChildrenHandler;
import zookeeper.one.ZkDao;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static zookeeper.test.PreemptionLock.ResourcePreemptionLock.num;

/**
 * package: zookeeper.test <br/>
 * functional describe:模拟资源竞争服务的线程
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/5/10 21:50
 */
public class ResourceLockThread extends Thread implements PathChildrenHandler {
    private static final Logger log = LoggerFactory.getLogger(ResourceLockThread.class);
    //客户端
    private ZkDao zkDao;
    //锁节点名称
    private static final String LOCK_NAME = "/lock";
    //监听的节点
    private static final String WATCHER_PATH = "/preemption";
    //全路径名称
    private static final String LOCK_NODE = WATCHER_PATH + LOCK_NAME;

    private static CountDownLatch notifySign = new CountDownLatch(1);
    private String serverName;

    @Override
    public void run() {
        log.debug("========================" + serverName + "开始执行======================");
        //添加watcher标志位
        boolean firstWatcher = true;
        //创建节点，抢锁
        boolean myLock = zkDao.createNodeOnly(LOCK_NODE, getGodNum(), CreateMode.EPHEMERAL, ZooDefs.Ids.OPEN_ACL_UNSAFE);
        while (!myLock) {//如果没有抢到锁
            log.debug("========================" + serverName + "没有抢到锁！======================");
            //添加watcher
            if (firstWatcher) {
                firstWatcher = false;
                try {
                    //zkDao.addChildWatcher(WATCHER_PATH, PathChildrenCache.StartMode.BUILD_INITIAL_CACHE, true, this, new ThreadPoolExecutor(1, 4, 2000l, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
                    zkDao.addChildWatcher(WATCHER_PATH, PathChildrenCache.StartMode.BUILD_INITIAL_CACHE, true, this, null);
                } catch (Exception e) {
                    log.error(serverName + "添加watcher失败！中断执行！", e);
                    throw new RuntimeException(serverName + "添加watcher失败！");
                }
            }
            //阻塞线程等待获得其他服务（线程）的释放锁通知
            try {
                notifySign.await();
            } catch (InterruptedException e) {
                log.error("+++++++++++++++" + serverName + "阻塞失败++++++++++++++++++++++", e);
            }
            //得到通知其他服务（线程）已经释放锁，抢锁
            myLock = zkDao.createNodeOnly(LOCK_NODE, getGodNum(), CreateMode.EPHEMERAL, ZooDefs.Ids.OPEN_ACL_UNSAFE);
            if (!myLock) {
                //依然没有抢到锁，重新创建CountDownLatch
                notifySign = new CountDownLatch(1);
            }
        }
        //抢到锁，进行业务处理
        log.debug("========================" + serverName + "抢到锁，开始执行业务处理======================");
        if (num > 0) {
            //将修改后的num写到节点数据
            num = num - 1 <= 0 ? 0 : num - 1;
            zkDao.updateDate(LOCK_NODE, getGodNum(), -1);
            log.debug("========================" + serverName + "结束执行业务处理，货物数减一，准备释放锁======================");
            //等待一下= =
            try {
                Thread.sleep(3000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            zkDao.updateDate(LOCK_NODE, getGodNum(), -1);
            log.debug("========================" + serverName + "检查资源已耗尽，不再执行业务处理======================");
        }
        //删除节点
        zkDao.deleteNode(LOCK_NODE, -1);
        //结束执行
        log.debug("========================" + serverName + "执行结束======================");
    }


    //watcher得到通知的事件并处理
    @Override
    public void childrenChanged(PathChildrenCache pathChildrenCache, PathChildrenCacheEvent event) {
        //监听事情，处理子节点删除后发通知抢锁
        PathChildrenCacheEvent.Type notifyType = event.getType();
        //只处理锁释放通知
        if (notifyType.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
            String path = event.getData().getPath();
            System.out.println("收到监听" + path);
            if (path.contains(LOCK_NAME)) {
                log.debug("+++++++++++++++++资源独占锁，" + serverName + "收到锁释放通知++++++++++++++++++");
                String numStr = new String(event.getData().getData());
                num = Integer.parseInt(numStr);
                //发出通知
                notifySign.countDown();
            } else {
                //不是资源独占锁释放通知
            }
        }
    }

    /**
     * 获取库存
     *
     * @return byte[]
     */
    private static byte[] getGodNum() {
        Integer numVal = (num == null || num < 0) ? 0 : num;
        return String.valueOf(numVal).getBytes();
    }

    public ZkDao getZkDao() {
        return zkDao;
    }

    public void setZkDao(ZkDao zkDao) {
        this.zkDao = zkDao;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
