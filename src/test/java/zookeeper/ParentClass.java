package zookeeper;

/**
 * package: zookeeper <br/>
 * blog:<a href="http://dr-yanglong.github.io/">dr-yanglong.github.io</a><br/>
 * functional describe:
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/3/2
 */
public class ParentClass {
    private static int i=initStatic(1);
    static{
        System.out.println("父类静态语句块");
    }
    {
        System.out.println("父类动态语句块");
    }
    private static int n=initStatic(2);
    private int k=initDy();
    public ParentClass() {
        System.out.println("父类构造函数");
    }

    public static int initStatic(int n){
        System.out.println("父类静态变量初始化："+n);
        return n;
    }

    public int initDy(){
        System.out.println("父类普通成员变量初始化");
        return 1;
    }

    public static int getI() {
        return i;
    }

    public static void setI(int i) {
        ParentClass.i = i;
    }

    public static int getN() {
        return n;
    }

    public static void setN(int n) {
        ParentClass.n = n;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("销毁子类");
        super.finalize();
    }
}
