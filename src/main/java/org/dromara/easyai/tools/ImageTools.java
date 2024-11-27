package org.dromara.easyai.tools;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.yolo.OutBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * 图片输出工具类
 *
 * @author lidapeng
 */
public class ImageTools {

    public static void writeImage(ThreeChannelMatrix img, String url) {
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

    public static void drawBox(String fileURL, List<OutBox> borderFoods, String outFileName, int fontSize) throws Exception {
        File file = new File(fileURL);
        BufferedImage image2 = ImageIO.read(file);
        int width = image2.getWidth();
        int height = image2.getHeight();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.drawImage(image2, 0, 0, width, height, null);
        g2.setFont(new Font(null, Font.BOLD, fontSize));
        for (OutBox borderFood : borderFoods) {//输出
            Rectangle2D rect = new Rectangle2D.Double(borderFood.getX(), borderFood.getY(), borderFood.getWidth(), borderFood.getHeight());//声明并创建矩形对象，矩形的左上角是(20，30)，宽是300，高是40
            g2.setColor(Color.RED);
            g2.draw(rect);
            g2.setColor(Color.BLUE);
            g2.drawString(borderFood.getTypeID(), borderFood.getX() + 10, borderFood.getY() + 10);
        }
        ImageIO.write(bi, "jpg", new FileOutputStream(outFileName));
    }

    public static ByteArrayOutputStream drawImage(ThreeChannelMatrix img) throws Exception {
        final BufferedImage bufferedImage = getBufferedImage(img);
        ByteArrayOutputStream ar = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", ar);
        return ar;
    }

    public static File drawImage(ThreeChannelMatrix img, String imageName) throws Exception {
        final BufferedImage bufferedImage = getBufferedImage(img);
        File ar = new File(imageName);
        ImageIO.write(bufferedImage, "PNG", ar);
        return ar;
    }

    private static BufferedImage getBufferedImage(ThreeChannelMatrix img) throws Exception {
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
                } else if (r < 0) {
                    r = 0;
                }
                if (g > 255) {
                    g = 255;
                } else if (g < 0) {
                    g = 0;
                }
                if (b > 255) {
                    b = 255;
                } else if (b < 0) {
                    b = 0;
                }
                g2.setColor(new Color(r, g, b));
                g2.drawRect(j, i, 1, 1);
            }
        }
        return bi;
    }


}
