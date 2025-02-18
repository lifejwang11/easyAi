package org.dromara.easyai.pso;

import org.dromara.easyai.i.PsoFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 粒子群
 */
public class PSO {
    private float globalValue = -1;//当前全局最优值
    private int times;//迭代次数
    private List<Particle> allPar = new ArrayList<>();//全部粒子集合
    private PsoFunction psoFunction;//粒子群执行函数
    private float inertialFactor = 0.5f;//惯性因子
    private float selfStudyFactor = 2;//个体学习因子
    private float socialStudyFactor = 2;//社会学习因子
    private boolean isMax;//取最大值还是最小值
    private float[] allBest;//全局最佳位置
    private Random random = new Random();
    private float[] minBorder, maxBorder;
    private float maxSpeed;
    private float initSpeed;//初始速度
    /**
     * 初始化
     *
     * @param dimensionNub 维度
     * @param minBorder  最小边界
     * @param maxBorder 最大边界
     * @param times 迭代次数
     * @param particleNub 粒子数量
     * @param psoFunction 适应函数
     * @param inertialFactor 惯性因子
     * @param selfStudyFactor 个体学习因子
     * @param socialStudyFactor 社会学习因子
     * @param isMax 最大值是否为最优
     * @param maxSpeed 最大速度
     * @param initSpeed 初始速度
     * @throws Exception
     */
    public PSO(int dimensionNub, float[] minBorder, float[] maxBorder,
               int times, int particleNub, PsoFunction psoFunction,
               float inertialFactor, float selfStudyFactor, float socialStudyFactor
            , boolean isMax, float maxSpeed, float initSpeed) {
        this.initSpeed = initSpeed;
        this.times = times;
        this.psoFunction = psoFunction;
        this.isMax = isMax;
        allBest = new float[dimensionNub];
        this.minBorder = minBorder;
        this.maxBorder = maxBorder;
        this.maxSpeed = maxSpeed;
        if (inertialFactor > 0) {
            this.inertialFactor = inertialFactor;
        }
        if (selfStudyFactor >= 0 && selfStudyFactor <= 4) {
            this.selfStudyFactor = selfStudyFactor;
        }
        if (socialStudyFactor >= 0 && socialStudyFactor <= 4) {
            this.socialStudyFactor = socialStudyFactor;
        }
        for (int i = 0; i < particleNub; i++) {//初始化生成粒子群
            Particle particle = new Particle(dimensionNub);
            allPar.add(particle);
        }

    }

    public float[] getAllBest() {
        return allBest;
    }

    public void setAllPar(List<Particle> allPar) {//外置粒子群注入
        this.allPar = allPar;
    }

    public void start() throws Exception {//开始进行迭代
        int size = allPar.size();
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < size; j++) {
                move(allPar.get(j), j);
            }
        }
        //粒子群移动结束
        // draw("/Users/lidapeng/Desktop/test/testOne/e2.jpg", fatherX, fatherY);
    }

    private void move(Particle particle, int id) throws Exception {//粒子群开始移动
        float[] parameter = particle.getParameter();//当前粒子的位置
        BestData[] bestData = particle.bestDataArray;//该粒子的信息
        float value = psoFunction.getResult(parameter, id);
        float selfValue = particle.selfBestValue;//局部最佳值
        if (isMax) {//取最大值
            if (value > globalValue) {//更新全局最大值
                globalValue = value;
                //更新全局最佳位置
                for (int i = 0; i < allBest.length; i++) {
                    allBest[i] = parameter[i];
                }
            }
            if (value > selfValue) {//更新局部最大值
                particle.selfBestValue = value;
                //更新局部最佳位置
                for (int i = 0; i < bestData.length; i++) {
                    bestData[i].selfBestPosition = parameter[i];
                }
            }
        } else {//取最小值
            if (globalValue < 0 || value < globalValue) {//更新全局最小值
                globalValue = value;
                //更新全局最佳位置
                for (int i = 0; i < allBest.length; i++) {
                    allBest[i] = parameter[i];
                }
            }
            if (selfValue < 0 || value < selfValue) {//更新全局最小值
                particle.selfBestValue = value;
                //更新局部最佳位置
                for (int i = 0; i < bestData.length; i++) {
                    bestData[i].selfBestPosition = parameter[i];
                }
            }
        }
        //先更新粒子每个维度的速度
        for (int i = 0; i < bestData.length; i++) {
            float speed = bestData[i].speed;//当前维度的速度
            float pid = bestData[i].selfBestPosition;//当前自己的最佳位置
            float selfPosition = parameter[i];//当前自己的位置
            float pgd = allBest[i];//当前维度的全局最佳位置
            //当前维度更新后的速度
            speed = inertialFactor * speed + selfStudyFactor * random.nextFloat() * (pid - selfPosition)
                    + socialStudyFactor * random.nextFloat() * (pgd - selfPosition);
            if ((float)Math.abs(speed) > maxSpeed) {
                if (speed > 0) {
                    speed = maxSpeed;
                } else {
                    speed = -maxSpeed;
                }
            }
            bestData[i].speed = speed;
            //更新该粒子该维度新的位置
            float position = selfPosition + speed;
            if (minBorder != null) {
                if (position < minBorder[i]) {
                    position = minBorder[i];
                }
                if (position > maxBorder[i]) {
                    position = maxBorder[i];
                }
            }
            bestData[i].selfPosition = position;
        }
    }

    class Particle {//粒子
        private BestData[] bestDataArray;
        private float selfBestValue = -1;//自身最优的值

        private float[] getParameter() {//获取粒子位置信息
            float[] parameter = new float[bestDataArray.length];
            for (int i = 0; i < parameter.length; i++) {
                parameter[i] = bestDataArray[i].selfPosition;
            }
            return parameter;
        }

        protected Particle(int dimensionNub) {//初始化随机位置
            bestDataArray = new BestData[dimensionNub];
            for (int i = 0; i < dimensionNub; i++) {
                float position;
                if (minBorder != null && maxBorder != null) {
                    float min = minBorder[i];
                    float max = maxBorder[i];
                    float region = max - min + 1;
                    position = random.nextInt((int) region) + min;//初始化该维度的位置
                } else {
                    position = random.nextFloat();
                }
                bestDataArray[i] = new BestData(position, initSpeed);
            }
        }
    }

    class BestData {//数据保存

        private BestData(float selfPosition, float initSpeed) {
            this.selfBestPosition = selfPosition;
            this.selfPosition = selfPosition;
            speed = initSpeed;
        }

        private float speed;//该粒子当前维度的速度
        private float selfBestPosition;//当前维度自身最优的历史位置/自己最优位置的值
        private float selfPosition;//当前维度自己现在的位置/也就是当前维度自己的值
    }
}
