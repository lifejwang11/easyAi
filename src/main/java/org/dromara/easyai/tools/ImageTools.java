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

    private static final int MAX_COLOR_VALUE = 255;
    private static final int MIN_COLOR_VALUE = 0;

    public static void writeImage(ThreeChannelMatrix img, String url) throws IOException {
        try (ByteArrayOutputStream b = drawImage(img);
             FileOutputStream fileOutputStream = new FileOutputStream(url)) {
            b.writeTo(fileOutputStream);
        } catch (RuntimeException e) {
            throw new IOException("Failed to write image: " + e.getMessage(), e);
        }
    }

    public static void drawBox(String fileURL, List<OutBox> borderFoods, String outFileName, int fontSize) throws Exception {
        File file = new File(fileURL);
        BufferedImage image2 = ImageIO.read(file);
        
        if (image2 == null) {
            throw new IOException("无法读取图像文件：" + fileURL);
        }
        
        int width = image2.getWidth();
        int height = image2.getHeight();
        
        try (BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
             Graphics2D g2 = (Graphics2D) bi.getGraphics()) {
            
            g2.drawImage(image2, 0, 0, width, height, null);
            g2.setFont(new Font(Font.SERIF, Font.BOLD, fontSize));
            
            for (OutBox borderFood : borderFoods) {
                Rectangle2D rect = new Rectangle2D.Float(
                    borderFood.getX(), 
                    borderFood.getY(), 
                    borderFood.getWidth(), 
                    borderFood.getHeight()
                );
                g2.setColor(Color.RED);
                g2.draw(rect);
                g2.setColor(Color.BLUE);
                g2.drawString(borderFood.getTypeID(), borderFood.getX() + 10, borderFood.getY() + 10);
            }
            
            try (FileOutputStream fos = new FileOutputStream(outFileName)) {
                ImageIO.write(bi, "jpg", fos);
            }
        }
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
        BufferedImage bi = new BufferedImage(y, x, BufferedImage.TYPE_INT_RGB);
        
        try (Graphics2D g2 = (Graphics2D) bi.getGraphics()) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    int r = clamp((int) (matrixR.getNumber(i, j) * MAX_COLOR_VALUE), MIN_COLOR_VALUE, MAX_COLOR_VALUE);
                    int g = clamp((int) (matrixG.getNumber(i, j) * MAX_COLOR_VALUE), MIN_COLOR_VALUE, MAX_COLOR_VALUE);
                    int b = clamp((int) (matrixB.getNumber(i, j) * MAX_COLOR_VALUE), MIN_COLOR_VALUE, MAX_COLOR_VALUE);
                    
                    g2.setColor(new Color(r, g, b));
                    g2.drawRect(j, i, 1, 1);
                }
            }
        }
        
        return bi;
    }
    
    /**
     * 将值限制在指定范围内
     * @param value 原始值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值
     */
    private static int clamp(int value, int min, int max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

}
