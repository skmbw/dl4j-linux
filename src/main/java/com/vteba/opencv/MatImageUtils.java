package com.vteba.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

//A Note on HIghGUI image read
//http://docs.opencv.org/java/2.4.2/org/opencv/highgui/Highgui.html#imread(java.lang.String, int)
//Currently, the following file formats are supported:
//
//Windows bitmaps - *.bmp, *.dib (always supported)
//JPEG files - *.jpeg, *.jpg, *.jpe (see the *Notes* section)
//JPEG 2000 files - *.jp2 (see the *Notes* section)
//Portable Network Graphics - *.png (see the *Notes* section)
//Portable image format - *.pbm, *.pgm, *.ppm (always supported)
//Sun rasters - *.sr, *.ras (always supported)
//TIFF files - *.tiff, *.tif (see the *Notes* section)


/**
 * 将OpenCV的mat转成BUfferedImage
 *
 * @author yinlei
 * @since 2017-1-17
 */
public class MatImageUtils {

    private Mat matrix;
    private MatOfByte mob;
    private String fileExten;

    /**
     * 将OpenCV的Mat结构转成Java的BufferedImage<br>
     * The file extension string should be ".jpg", ".png", etc
     * @param matrix 图片数据
     * @param fileExtension 文件扩展名
     */
    public MatImageUtils(Mat matrix, String fileExtension) {
        this.matrix = matrix;
        fileExten = fileExtension;
        mob = new MatOfByte();
    }

    public BufferedImage getImage() {
        // convert the matrix into a matrix of bytes appropriate for
        // this file extension
        Imgcodecs.imencode(fileExten, matrix, mob);
        // convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }
}