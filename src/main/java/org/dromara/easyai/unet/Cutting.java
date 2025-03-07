package org.dromara.easyai.unet;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.tools.Picture;

/**
 * @author lidapeng
 * @time 2025/3/7 08:00
 * @des 图像语义最终裁切
 */
public class Cutting {
    private final float th;

    public Cutting(float th) {
        this.th = th;
    }

    public void cut(ThreeChannelMatrix backGround, ThreeChannelMatrix outPicture, OutBack outBack) throws Exception {
        int x = backGround.getX();
        int y = backGround.getY();
        ThreeChannelMatrix picture = Picture.getMyPicture(x, y);
        int tx = outPicture.getX();
        int ty = outPicture.getY();
        Matrix r = backGround.getMatrixR();
        Matrix g = backGround.getMatrixG();
        Matrix b = backGround.getMatrixB();
        Matrix pr = picture.getMatrixR();
        Matrix pg = picture.getMatrixG();
        Matrix pb = picture.getMatrixB();
        Matrix outMatrix = outPicture.CalculateAvgGrayscale();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (i < tx && j < ty) {
                    if (outMatrix.getNumber(i, j) > th) {//需要拿出来的像素
                        pr.setNub(i, j, r.getNumber(i, j));
                        pg.setNub(i, j, g.getNumber(i, j));
                        pb.setNub(i, j, b.getNumber(i, j));
                    }
                }
            }
        }
        outBack.getBackThreeChannelMatrix(picture);
    }
}
