package zookeeper.test.sharelock;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zookeeper.common.PathChildrenHandler;
import zookeeper.one.ZkDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * package: zookeeper.test.sharelock <br/>
 * functional describe:
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/5/12 10:15
 */
public class ShareLockThread extends Thread implements PathChildrenHandler {
    private static final Logger logger = LoggerFactory.getLogger(ShareLockThread.class);
    //服务名称
    private String serverName;
    //阻塞锁，可重用
    private CyclicBarrier lock = new CyclicBarrier(2);
    private ZkDao zkDao;
    //锁类型
    private String lockType = LockType.READ_LOCK.getType();
    //锁节点父节点
    private String lockPath = "/sharelock";
    //是否是第一次添加watcher
    private boolean isFirst = true;
    //身份标识
    private String identity;
    //nodeName创建的node名称
    private String localName;
    //资源存储目录
    private String sourcePath = "/sourcenum";

    @Override
    public void run() {
        logger.debug("==============" + serverName + "开始添加监听准备上锁================");
        //添加watcher
        if (isFirst) {
            try {
                zkDao.addChildWatcher(lockPath, PathChildrenCache.StartMode.BUILD_INITIAL_CACHE, true, this, null);
            } catch (Exception e) {
                logger.error(serverName + "添加监听器失败！");
            }
        }
        //创建顺序节点
        //生成节点前缀，规则：lockpath+"/"+lockType+serverName+"-"
        identity = lockPath + "/" + lockType + serverName;
        String ePath = identity + "-";
        zkDao.createNodeOnly(ePath, "locktest".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL, ZooDefs.Ids.OPEN_ACL_UNSAFE);
        //获取当前队列
        List<String> children = zkDao.getChildren(lockPath);
        while (true) {
            //判断自己是否是锁持有者，不是就阻塞
            if (!isUnLock(children)) {
                try {
                    lock.await();//---转到watcher执行
                } catch (InterruptedException | BrokenBarrierException e) {
                    logger.error("=====================" + serverName + "阻塞失败==================", e);
                }
            } else {
                break;
            }
            //收到解除阻塞通知
            logger.error("=====================" + serverName + "解除阻塞======================");
            //获取锁队列
            children = zkDao.getChildren(lockPath);
            if (!isUnLock(children)) {//如果仍然不能解锁
                lock.reset();//重置计数器
            } else {//可以解锁
                break;
            }
        }
        logger.debug("==============" + serverName + "执行业务处理================");
        try {//模拟业务处理，暂停3秒
            Thread.sleep(3000l);
        } catch (InterruptedException e) {
            logger.error("= =模拟业务处理延迟失败");
        }
        //执行业务处理
        Integer sourceNum = getSourceNum(zkDao.readData(sourcePath));
        if (LockType.READ_LOCK.getType().equals(lockType)) {
            //如果是S锁，读取打印信息
            logger.error("===========S锁读取资源数：" + sourceNum + "================");
        } else {
            //如果是X锁，资源数减一
            sourceNum = sourceNum - 1 >= 0 ? sourceNum - 1 : 0;
            //更新资源
            zkDao.updateDate(sourcePath, sourceNum.toString().getBytes(), -1);
            logger.error("===========X锁操作资源数：" + sourceNum + "================");
        }
        logger.debug("==============" + serverName + "业务处理结束================");
        //释放锁
        zkDao.deleteNode(localName, -1);
    }

    @Override
    public void childrenChanged(PathChildrenCache pathChildrenCache, PathChildrenCacheEvent event) {
        PathChildrenCacheEvent.Type notifyType = event.getType();
        if (notifyType.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
            logger.debug("++++++" + serverName + "监听到子节点移除通知++++++");
            try {
                lock.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                logger.error("++++++" + serverName + "watcher中阻塞失败++++++++", e);
            }
        } else if (notifyType.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
            logger.debug("++++++" + serverName + "监听到子节点添加通知++++++");
        } else if (notifyType.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
            logger.debug("++++++" + serverName + "监听到子节点更新通知++++++");
        } else if (notifyType.equals(PathChildrenCacheEvent.Type.INITIALIZED)) {
            logger.debug("++++++" + serverName + "监听到监听器初始化通知++++++");
        }


    }

    /**
     * 是否解锁，判断自己锁类型，以及自己之前是否有X锁
     *
     * @return true/false 能不能解锁
     */
    private boolean isUnLock(List<String> nodes) {
        boolean unLock = false;
        if (nodes == null || nodes.isEmpty()) {
            return unLock;
        }
        //记录锁类型和对应下标
        List<String> lockSeq = new ArrayList<>();
        //确定自己位置和锁类型
        String prefix = lockType + serverName;
        for (int i = 0; i < nodes.size(); i++) {
            String nodeName = nodes.get(i);
            if (prefix.equals(nodeName.split("-")[0])) {//如果是本节点
                localName=nodeName;
                break;
            } else {//，记录锁类型
                lockSeq.add(nodeName.indexOf(LockType.READ_LOCK.getType()) > -1 ? LockType.READ_LOCK.getType() : LockType.WRITE_LOCK.getType());
            }
        }
        //如果是第一个，无论S和X都解锁
        if (lockSeq.isEmpty()) {
            // X锁，只有自己是第一个才能解锁，S锁第一个也解锁
            unLock = true;
        } else {// 查看自己之前的锁的类型判断是否可以解锁
            //只处理S锁
            if (LockType.READ_LOCK.getType().equals(lockType)) {
                //查看自己之前是否有X锁
                unLock = lockSeq.contains(LockType.WRITE_LOCK.getType()) ? false : true;
            }
        }
        return unLock;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }


    public ZkDao getZkDao() {
        return zkDao;
    }

    public void setZkDao(ZkDao zkDao) {
        this.zkDao = zkDao;
    }

    public String getLockType() {
        return lockType;
    }

    public void setLockType(String lockType) {
        logger.debug("=====================锁类型：" + lockType + "========================");
        this.lockType = lockType;
    }

    public String getLockPath() {
        return lockPath;
    }

    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * 获取资源数
     *
     * @param bytes
     * @return
     */
    private static Integer getSourceNum(byte[] bytes) {
        String numVal = new String(bytes);
        return Integer.parseInt(numVal);
    }
}
