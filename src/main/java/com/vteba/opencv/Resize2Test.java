package com.vteba.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Map;

/**
 * Created by yinlei on 17-3-1.
 */
public class Resize2Test {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        File files = new File("/home/yinlei/idcard2select/");
        for (File file : files.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jpg")) {
                try {
                    String name = file.getName();
                    Mat mat = Imgcodecs.imread("/home/yinlei/idcard2select/" + name);

                    if (mat.cols() > 700) {
                        Mat dst = new Mat();
                        Size size = new Size(mat.cols() / 2, mat.rows() / 2);
                        Imgproc.resize(mat, dst, size);
                        Imgcodecs.imwrite("/home/yinlei/idcard2select/" + name, dst);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
