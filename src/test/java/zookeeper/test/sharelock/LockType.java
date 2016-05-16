package zookeeper.test.sharelock;

/**
 * package: zookeeper.test.sharelock <br/>
 * functional describe:共享锁类型
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/5/16 10:00
 */
public enum LockType {
    READ_LOCK("S","S锁，可并发读"),
    WRITE_LOCK("X","X锁，不可并发");
    private String type;
    private String describe;

    LockType(String type, String describe) {
        this.type = type;
        this.describe = describe;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
