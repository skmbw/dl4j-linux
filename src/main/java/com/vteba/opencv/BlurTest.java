package com.vteba.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.BORDER_DEFAULT;

/**
 * Created by yinlei on 17-2-15.
 */
public class BlurTest {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String s = "23";

    public static void main(String[] args) {
        Mat yl03 = Imgcodecs.imread("/home/yinlei/3.jpg");
        Mat dst = new Mat();
        Size size = new Size(1, 1); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(yl03, dst, size); // 平滑，降噪
        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图
        // 二值化
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);

        Imgcodecs.imwrite("/home/yinlei/s"+s+"blur.png", binary);


    }
}
