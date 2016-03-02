package zookeeper;

/**
 * package: zookeeper <br/>
 * blog:<a href="http://dr-yanglong.github.io/">dr-yanglong.github.io</a><br/>
 * functional describe:
 *
 * @author DR.YangLong [410357434@163.com]
 * @version 1.0    2016/3/2
 */
public class SubClass extends ParentClass{
    private static int a=initStatic(1);
    static{
        System.out.println("子类静态语句块");
    }
    {
        System.out.println("子类动态语句块");
    }
    private static int b=initStatic(2);
    private int c=initDy();
    public SubClass() {
        System.out.println("子类构造函数");
    }

    public static void main(String []args){
        new SubClass();
        System.out.println("112112");
    }
    public static int initStatic(int n){
        System.out.println("子类静态变量初始化："+n);
        return n;
    }

    public int initDy(){
        System.out.println("子类普通成员变量初始化");
        return 1;
    }

    public static int getA() {
        return a;
    }

    public static void setA(int a) {
        SubClass.a = a;
    }

    public static int getB() {
        return b;
    }

    public static void setB(int b) {
        SubClass.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("销毁子类");
        super.finalize();
    }
}
