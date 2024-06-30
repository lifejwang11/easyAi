package org.wlld.tools;

import org.wlld.MatrixTools.Matrix;
import org.wlld.entity.Box;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.yolo.OutBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class ImageTools {

    public void writeImage(ThreeChannelMatrix img, String url) {
        ByteArrayOutputStream b = null;
        FileOutputStream fileOutputStream = null;
        try {
            b = drawImage(img);
            fileOutputStream = new FileOutputStream(url);
            b.writeTo(fileOutputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (b != null) {
                    b.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void drawBox(String fileURL, List<OutBox> borderFoods, String outFileName, int fontSize) throws Exception {
        File file = new File(fileURL);
        BufferedImage image2 = ImageIO.read(file);
        int width = image2.getWidth();
        int height = image2.getHeight();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setColor(Color.RED);
        g2.drawImage(image2, 0, 0, width, height, null);
        g2.setFont(new Font(null, Font.BOLD, fontSize));
        for (OutBox borderFood : borderFoods) {//输出
            Rectangle2D rect = new Rectangle2D.Double(borderFood.getX(), borderFood.getY(), borderFood.getWidth(), borderFood.getHeight());//声明并创建矩形对象，矩形的左上角是(20，30)，宽是300，高是40
            g2.draw(rect);
            g2.drawString(borderFood.getTypeID(), borderFood.getX() + 10, borderFood.getY() + 10);
        }
        ImageIO.write(bi, "jpg", new FileOutputStream(outFileName));
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
