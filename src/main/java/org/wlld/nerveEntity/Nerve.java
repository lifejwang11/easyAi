package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.i.ActiveFunction;
import org.wlld.test.Ma;
import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * @date 9:36 上午 2019/12/21
 */
public abstract class Nerve {
    private List<Nerve> son = new ArrayList<>();//轴突下一层的连接神经元
    private List<Nerve> fathor = new ArrayList<>();//树突上一层的连接神经元
    protected Map<Integer, Double> dendrites = new HashMap<>();//上一层权重(需要取出)
    protected Map<Integer, Double> wg = new HashMap<>();//上一层权重与梯度的积
    private int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    protected int upNub;//上一层神经元数量
    protected int downNub;//下一层神经元的数量
    protected Map<Long, List<Double>> features = new HashMap<>();
    //static final Logger logger = LogManager.getLogger(Nerve.class);
    protected double threshold;//此神经元的阈值需要取出
    protected String name;//该神经元所属类型
    protected double outNub;//输出数值（ps:只有训练模式的时候才可保存输出过的数值）
    protected double E;//模板期望值
    protected double gradient;//当前梯度
    protected double studyPoint;
    protected double sigmaW;//对上一层权重与上一层梯度的积进行求和
    private int backNub = 0;//当前节点被反向传播的次数
    protected ActiveFunction activeFunction;

    public Map<Integer, Double> getDendrites() {
        return dendrites;
    }

    public void setDendrites(Map<Integer, Double> dendrites) {
        this.dendrites = dendrites;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    protected Nerve(int id, int upNub, String name, int downNub,
                    double studyPoint, boolean init, ActiveFunction activeFunction
            , boolean isMatrix) {//该神经元在同层神经元中的编号
        this.id = id;
        this.upNub = upNub;
        this.name = name;
        this.downNub = downNub;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        if (init) {
            if (isMatrix) {
                initKernel();
            } else {
                initPower();//生成随机权重
            }
        }
    }

    public void sendMessage(long enevtId, double parameter, boolean isStudy, Map<Integer, Double> E) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.input(enevtId, parameter, isStudy, E);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    //正向传播矩阵
    public void sendMatrixMessage(long enevtId, Matrix parameter, boolean isStudy,
                                  double E) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.inputMatrix(enevtId, parameter, isStudy, E);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    private void backSendMessage(long eventId) throws Exception {//反向传播
        if (fathor.size() > 0) {
            for (int i = 0; i < fathor.size(); i++) {
                fathor.get(i).backGetMessage(wg.get(i + 1), eventId);
            }
        }
    }

    protected void input(long eventId, double parameter, boolean isStudy
            , Map<Integer, Double> E) throws Exception {//输入

    }

    //接收矩阵参数
    protected void inputMatrix(long eventId, Matrix parameter, boolean isStudy
            , double E) throws Exception {//输入

    }

    private void backGetMessage(double parameter, long eventId) throws Exception {//反向传播
        backNub++;
        sigmaW = ArithUtil.add(sigmaW, parameter);
        if (backNub == downNub) {//进行新的梯度计算
            backNub = 0;
            gradient = ArithUtil.mul(activeFunction.functionG(outNub), sigmaW);
            updatePower(eventId);//修改阈值
        }
    }

    protected void updatePower(long eventId) throws Exception {//修改阈值
        double h = ArithUtil.mul(gradient, studyPoint);//梯度下降
        threshold = ArithUtil.add(threshold, -h);//更新阈值
        updateW(h, eventId);
        sigmaW = 0;//求和结果归零
        backSendMessage(eventId);
    }

    private void updateW(double h, long eventId) {//h是学习率 * 当前g（梯度）
        List<Double> list = features.get(eventId);
        for (Map.Entry<Integer, Double> entry : dendrites.entrySet()) {
            int key = entry.getKey();//上层隐层神经元的编号
            double w = entry.getValue();//接收到编号为KEY的上层隐层神经元的权重
            double bn = list.get(key - 1);//接收到编号为KEY的上层隐层神经元的输入
            double wp = ArithUtil.mul(bn, h);//编号为KEY的上层隐层神经元权重的变化值
            w = ArithUtil.add(w, wp);//修正后的编号为KEY的上层隐层神经元权重
            double dm = ArithUtil.mul(w, gradient);//返回给相对应的神经元
            wg.put(key, dm);//保存上一层权重与梯度的积
            dendrites.put(key, w);//保存修正结果
        }
        features.remove(eventId); //清空当前上层输入参数参数
    }

    protected boolean insertParameter(long eventId, double parameter) {//添加参数
        boolean allReady = false;
        List<Double> featuresList;
        if (features.containsKey(eventId)) {
            featuresList = features.get(eventId);
        } else {
            featuresList = new ArrayList<>();
            features.put(eventId, featuresList);
        }
        featuresList.add(parameter);
        if (featuresList.size() >= upNub) {
            allReady = true;
        }
        return allReady;
    }

    protected void initFeatures(long eventId) {//初始化九个参数和
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            list.add(0.0);
        }
        features.put(eventId, list);
    }

    protected void destoryParameter(long eventId) {//销毁参数
        features.remove(eventId);
    }

    protected double calculation(long eventId) {//计算当前输出结果
        double sigma = 0;
        List<Double> featuresList = features.get(eventId);
        for (int i = 0; i < featuresList.size(); i++) {
            double value = featuresList.get(i);
            double w = dendrites.get(i + 1);
            //System.out.println("w==" + w + ",value==" + value);
            sigma = ArithUtil.add(ArithUtil.mul(w, value), sigma);
            //logger.debug("name:{},eventId:{},id:{},myId:{},w:{},value:{}", name, eventId, i + 1, id, w, value);
        }
        //logger.debug("当前神经元线性变化已经完成,name:{},id:{}", name, getId());
        return ArithUtil.sub(sigma, threshold);
    }

    //进行卷积运算
    protected Matrix convolution(Matrix matrix, long eventId, boolean isStudy) throws Exception {
        Matrix kernel = getKernel();
        int xn = matrix.getX();
        int yn = matrix.getY();
        int x = xn / 3;//求导后矩阵的行数
        int y = yn / 3;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//最终合成矩阵
        for (int i = 0; i < xn - 3; i += 3) {//遍历行
            for (int j = 0; j < yn - 3; j += 3) {//遍历每行的列
                //进行卷积运算
                double dm = MatrixOperation.convolution(matrix, kernel, i, j);
                if (isStudy) {//如果是学习的话，拿到分块矩阵,对对应权重的值进行求和
                    Matrix matrix1 = matrix.getSonOfMatrix(i, j, 3, 3);
                    sigma(matrix1, eventId);
                }
                if (dm > 0) {//存在边缘
                    myMatrix.setNub(i, j, dm);
                }
            }
        }
        return myMatrix;
    }

    //对每一项进行求和，只有在学习的时候执行它
    private void sigma(Matrix matrix, long eventId) throws Exception {
        int n = 0;
        List<Double> list = features.get(eventId);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                list.set(n, ArithUtil.add(list.get(n), matrix.getNumber(i, j)));
                n++;
            }
        }
    }

    private Matrix getKernel() throws Exception {
        Matrix kernel = new Matrix(3, 3);
        for (int i = 1; i < 10; i++) {
            double w = dendrites.get(i);
            int t = i - 1;
            //将权重填入卷积核当中
            int x = t / 3;
            int y = t % 3;
            kernel.setNub(x, y, w);
        }
        return kernel;
    }

    private void initPower() {//初始化权重及阈值
        if (upNub > 0) {
            Random random = new Random();
            for (int i = 1; i < upNub + 1; i++) {
                dendrites.put(i, random.nextDouble());//random.nextDouble()
            }
            //生成随机阈值
            threshold = random.nextDouble();
        }
    }

    public void initKernel() {//对卷积核进行初始化
        Random random = new Random();
        for (int i = 1; i < 10; i++) {
            dendrites.put(i, random.nextDouble());
        }
        //生成随机阈值
        threshold = random.nextDouble();
    }

    public int getId() {
        return id;
    }


    public void connect(List<Nerve> nerveList) {
        son.addAll(nerveList);//连接下一层
    }

    public void connectFathor(List<Nerve> nerveList) {
        fathor.addAll(nerveList);//连接上一层
    }
}
