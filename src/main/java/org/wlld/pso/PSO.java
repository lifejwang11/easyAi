package org.wlld.pso;

import org.wlld.i.PsoFunction;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private double globalValue = -1;//当前全局最优值
    private int times;//迭代次数
    private List<Particle> allPar = new ArrayList<>();//全部粒子集合
    private PsoFunction psoFunction;//粒子群执行函数
    private double inertialFactor = 0.5;//惯性因子
    private double selfStudyFactor = 2;//个体学习因子
    private double socialStudyFactor = 2;//社会学习因子
    private boolean isMax;//取最大值还是最小值
    private double[] allBest;//全局最佳位置
    private Random random = new Random();
    private int[] minBorder, maxBorder;
    private double maxSpeed;

    public PSO(int dimensionNub, int[] minBorder, int[] maxBorder,
               int times, int particleNub, PsoFunction psoFunction,
               double inertialFactor, double selfStudyFactor, double socialStudyFactor
            , boolean isMax, double maxSpeed) {
        this.times = times;
        this.psoFunction = psoFunction;
        this.isMax = isMax;
        allBest = new double[dimensionNub];
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

    public void setAllPar(List<Particle> allPar) {//外置粒子群注入
        this.allPar = allPar;
    }

    public void start(int fatherX, int fatherY) throws Exception {//开始进行迭代
        int size = allPar.size();
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < size; j++) {
                move(allPar.get(j), j);
            }
        }
        //粒子群移动结束
        draw("/Users/lidapeng/Desktop/test/testOne/e2.jpg", fatherX, fatherY);
    }

    private void draw(String path, int fatherX, int fatherY) throws Exception {
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedImage image2 = ImageIO.read(fileInputStream);
        int width = image2.getWidth();
        int height = image2.getHeight();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setColor(Color.CYAN);
        g2.drawImage(image2, 0, 0, width, height, null);
        int size = allPar.size();
        for (int j = 0; j < size; j++) {//输出
            Particle particle = allPar.get(j);
            double[] parameter = particle.getParameter();
            int x = (int) (fatherX + parameter[0]);
            int y = (int) (fatherY + parameter[1]);
            Rectangle2D rect = new Rectangle2D.Double(y, x, 1, 1);//声明并创建矩形对象，矩形的左上角是(20，30)，宽是300，高是40
            g2.draw(rect);
        }
        String savePath = "/Users/lidapeng/Desktop/test/testTwo/a.jpg";
        ImageIO.write(bi, "JPEG", new FileOutputStream(savePath));
    }

    private void move(Particle particle, int id) throws Exception {//粒子群开始移动
        double[] parameter = particle.getParameter();//当前粒子的位置
        BestData[] bestData = particle.bestDataArray;//该粒子的信息
        double value = psoFunction.getResult(parameter, id);
        double selfValue = particle.selfBestValue;//局部最佳值
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
            double speed = bestData[i].speed;//当前维度的速度
            double pid = bestData[i].selfBestPosition;//当前自己的最佳位置
            double selfPosition = parameter[i];//当前自己的位置
            double pgd = allBest[i];//当前维度的全局最佳位置
            //当前维度更新后的速度
            speed = inertialFactor * speed + selfStudyFactor * random.nextDouble() * (pid - selfPosition)
                    + socialStudyFactor * random.nextDouble() * (pgd - selfPosition);
            if (Math.abs(speed) > maxSpeed) {
                if (speed > 0) {
                    speed = maxSpeed;
                } else {
                    speed = -maxSpeed;
                }
            }
            bestData[i].speed = speed;
            //更新该粒子该维度新的位置
            double position = selfPosition + speed;
            if (position < minBorder[i]) {
                position = minBorder[i];
            }
            if (position > maxBorder[i]) {
                position = maxBorder[i];
            }
            bestData[i].selfPosition = position;
        }
    }

    class Particle {//粒子
        private BestData[] bestDataArray;
        private double selfBestValue = -1;//自身最优的值

        private double[] getParameter() {//获取粒子位置信息
            double[] parameter = new double[bestDataArray.length];
            for (int i = 0; i < parameter.length; i++) {
                parameter[i] = bestDataArray[i].selfPosition;
            }
            return parameter;
        }

        protected Particle(int dimensionNub) {//初始化随机位置
            bestDataArray = new BestData[dimensionNub];
            for (int i = 0; i < dimensionNub; i++) {
                int min = minBorder[i];
                int max = maxBorder[i];
                int region = max - min + 1;
                int position = random.nextInt(region) + min;//初始化该维度的位置
                bestDataArray[i] = new BestData(position);
            }
        }
    }

    class BestData {//数据保存

        private BestData(double selfPosition) {
            this.selfBestPosition = selfPosition;
            this.selfPosition = selfPosition;
        }

        private double speed = 1;//该粒子当前维度的速度
        private double selfBestPosition;//当前维度自身最优的历史位置/自己最优位置的值
        private double selfPosition;//当前维度自己现在的位置/也就是当前维度自己的值
    }
}
