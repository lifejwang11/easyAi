package org.wlld.tools;

import org.wlld.MatrixTools.Matrix;
import org.wlld.entity.ThreeChannelMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class ImageTools {

    public void writeImage(ThreeChannelMatrix img, String url) throws Exception {
        ByteArrayOutputStream b = drawImage(img);
        FileOutputStream fileOutputStream = new FileOutputStream(url);
        b.writeTo(fileOutputStream);
    }

    public ByteArrayOutputStream drawImage(ThreeChannelMatrix img) throws Exception {
        Matrix matrixR = img.getMatrixR();
        Matrix matrixG = img.getMatrixG();
        Matrix matrixB = img.getMatrixB();
        int x = img.getX();
        int y = img.getY();
        BufferedImage bi = new BufferedImage(img.getY(), img.getX(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int r = (int) (matrixR.getNumber(i, j) * 255D);
                int g = (int) (matrixG.getNumber(i, j) * 255D);
                int b = (int) (matrixB.getNumber(i, j) * 255D);
                if (r > 255) {
                    r = 255;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b > 255) {
                    b = 255;
                }
                g2.setColor(new Color(r, g, b));
                g2.drawRect(j, i, 1, 1);
            }
        }
        ByteArrayOutputStream ar = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", ar);
        return ar;
    }
}
