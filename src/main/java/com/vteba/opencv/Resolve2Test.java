package com.vteba.opencv;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinlei on 17-2-15.
 */
public class Resolve2Test {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String s = "38";

    public static void main(String[] args) {
        Mat yl03 = Imgcodecs.imread("/mnt/downloads/200-399-2/220.png");
        //吉林南吉林市丰满区泰山
        //昭1-1-53号

//        Mat yl03 = Imgcodecs.imread("/tmp/aa_code.png");

//        Mat dst = new Mat();
//        Size size = new Size(1, 1); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
//        Imgproc.blur(yl03, dst, size); // 平滑，降噪
//        Mat gray = new Mat();
//        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

//        int height = (int) (yl03.rows() * 0.7);
//        int width = (int) (yl03.cols() * 0.92);
//        Rect rect = new Rect(0, 0, width, height);
//
//        Mat small = OpenCVUtils.getSlice(yl03, rect);

        ITesseract instance = new Tesseract();
        instance.setLanguage("shz18");
        // shz11是由地址训练的汉字，图片被缩放。
        // shz9也是数字的，168张身份证号码，图片被缩放。
        // shz10也是数字的，168张身份证号码，图片无缩放。
        // shz12是身份证号码，纯数字的，原始图，精简的40多张。（OK）
        // shz13是地址训练的汉字，230多张原始图片（OK）
        // shz14室shz13加我同学给做的5张图训练的汉字，字库更大
        List<String> configs = new ArrayList<>();
//        configs.add("digits");
        instance.setConfigs(configs);
        instance.setDatapath("/usr/local/tesseract-3.04.01/tessdata/");

        MatImageUtils matImage = new MatImageUtils(yl03, ".png");

        try {
            String result = instance.doOCR(matImage.getImage());
            System.out.println(result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }

//        Imgcodecs.imwrite("/tmp/code_.png", small);
    }
}
