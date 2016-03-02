package zookeeper.one;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayList;

/**
 * package: zookeeper.one <br/>
 * blog:<a href="http://dr-yanglong.github.io/">dr-yanglong.github.io</a><br/>
 * functional describe:zk客户端封装
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/3/2
 */
public interface ZkDao {
    /**
     * 关闭客户端
     */
    void close();

    /**
     * 创建节点，如果含有未创建的父节点，将会一起创建
     *
     * @param path       节点路径
     * @param data       数据
     * @param createMode 节点类型，临时，持久，序列，默认临时
     * @param acl        权限,默认world:anyone:crwda
     * @return 成功或失败
     */
    boolean createNodeWithParent(String path, byte[] data, CreateMode createMode, ArrayList<ACL> acl);

    /**
     * 创建节点，如果含有未创建的父节点，将会失败
     *
     * @param path       节点路径
     * @param data       数据
     * @param createMode 节点类型，临时，持久，序列，默认临时
     * @param acl        权限,默认world:anyone:crwda
     * @return 成功或失败
     */
    boolean createNodeOnly(String path, byte[] data, CreateMode createMode, ArrayList<ACL> acl);
}
