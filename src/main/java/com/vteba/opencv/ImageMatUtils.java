package com.vteba.opencv;

import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * 图像转换成OpenCV的Mat。使用静态方法也可以的。
 *
 * @author yinlei
 * @since 2017-1-17
 */
public class ImageMatUtils {

    private BufferedImage original;
    private int itype;
    private int mtype;

    /**
     * @param image
     * @param imgType bufferedImage的类型 如 BufferedImage.TYPE_3BYTE_BGR
     * @param matType 转换成mat的type 如 CvType.CV_8UC3
     */
    public ImageMatUtils(BufferedImage image, int imgType, int matType) {
        original = image;
        itype = imgType;
        mtype = matType;
    }

    public Mat getMat() {
        if (original == null) {
            throw new IllegalArgumentException("original image is null");
        }

        // Don't convert if it already has correct type
        if (original.getType() != itype) {

            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(),
                    original.getHeight(), itype);

            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

//        ImageIO.read(InputStream);

        // 对于15位的身份证 最后一位奇数男偶数女
        // 对于18位的身份证 倒数第二位的才是

        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), mtype);
        mat.put(0, 0, pixels);
        return mat;
    }
}

