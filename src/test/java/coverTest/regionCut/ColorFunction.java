package coverTest.regionCut;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.PsoFunction;
import org.wlld.imageRecognition.ThreeChannelMatrix;
import org.wlld.tools.Frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ColorFunction extends Frequency implements PsoFunction {
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private Map<Integer, double[]> pixels = new HashMap<>();

    public ColorFunction(ThreeChannelMatrix threeChannelMatrix) {
        matrixR = threeChannelMatrix.getMatrixR();
        matrixG = threeChannelMatrix.getMatrixG();
        matrixB = threeChannelMatrix.getMatrixB();
    }

    @Override
    public double getResult(double[] parameter, int id) throws Exception {
        int x = (int) parameter[0];
        int y = (int) parameter[1];
        double[] rgb = new double[]{matrixR.getNumber(x, y) / 255, matrixG.getNumber(x, y) / 255,
                matrixB.getNumber(x, y) / 255};
        pixels.put(id, rgb);
        //计算当前方差
        return getDist();
    }

    private double getDist() {//计算当前均方误差
        double[] r = new double[pixels.size()];
        double[] g = new double[pixels.size()];
        double[] b = new double[pixels.size()];
        for (Map.Entry<Integer, double[]> entry : pixels.entrySet()) {
            double[] rgb = entry.getValue();
            int key = entry.getKey();
            r[key] = rgb[0];
            g[key] = rgb[1];
            b[key] = rgb[2];
        }
        return dc(r) + dc(g) + dc(b);
    }

}
