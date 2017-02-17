package com.vteba.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by yinlei on 17-1-12.
 */
public class SplitImageTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static final int s = 7;

    public static void main(String[] args) {
        Mat img = Imgcodecs.imread("/tmp/yinlei2.png");

//        Size size = new Size(4, 4); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
//        Imgproc.blur(img, img, size); // 平滑，降噪

        int height = img.height();
        int width = img.width();
//        int h = img.rows(); // 和上面是等价的
//        int w = img.cols();

        int startWidth = (int) (width * 0.085);
        int startHeight = (int) (height * 0.12);

        int endWidth = width - startWidth - (int) (width * 0.07);
        int endHeight = height - startHeight - (int) (height * 0.1);

        Rect sliceRect = new Rect(startWidth, startHeight, endWidth, endHeight);
        Mat sliceMat = new Mat(img, sliceRect);
        Mat result = new Mat();
        sliceMat.copyTo(result);
        boolean r = Imgcodecs.imwrite("/home/yinlei/s" + s + "slice.png", result);
        System.out.println("操作结果：" + r);

        slice(img, height, width);
    }

    public static void slice(Mat img, int height, int width) {
//        Mat resizeMat = new Mat();
//        Size size2 = new Size(475, 300);
//        Imgproc.resize(img, resizeMat, size2);
//        Imgcodecs.imwrite("/tmp/bi.png", resizeMat);

        width = 475;
        height = 300;

        int startX = (int) (width * 0.19);
        int startY = (int) (height * 0.12);

        int endWidth = (int) (width * 0.448); //width - (int) (width * 0.555); //startX - (int) (width * 0.353);
        int endHeight = (int) (height * 0.552); //height - (int) (height * 0.448); //startY - (int) (height * 0.325);

        Rect sliceRect = new Rect(startX, startY, endWidth, endHeight);
        Mat sliceMat = new Mat(img, sliceRect);
        Mat result = new Mat();
        sliceMat.copyTo(result);
        boolean r = Imgcodecs.imwrite("/home/yinlei/s" + s + "slice2.png", result);
        System.out.println("操作结果slice：" + r);

        // 切割姓名和性别，民族

        int nameX = startX;
        int nameY = startY;
        int nameWidth = (int) (width * 0.27);
        int nameHeight = (int) (height * 0.23);
        Rect nameRect = new Rect(nameX, nameY, nameWidth, nameHeight);
        Mat nameMat = new Mat(img, nameRect);
        Mat nameResult = new Mat();
        nameMat.copyTo(nameResult);
        r = Imgcodecs.imwrite("/home/yinlei/s" + s + "name.png", nameResult);
        System.out.println("切割名字和民族结果：" + r);

        // 切割身份证号码

        int codeX = (int) (width * 0.346);
        int codeY = (int) (height * 0.795);
        int codeWidth = (int) (width * 0.585);
        int codeHeight = (int) (height * 0.125);

        Rect codeRect = new Rect(codeX, codeY, codeWidth, codeHeight);
        Mat codeMat = new Mat(img, codeRect);

        Mat codeResult = new Mat();
        codeMat.copyTo(codeResult);
        r = Imgcodecs.imwrite("/home/yinlei/s" + s +"code.png", codeResult);
        System.out.println("操作结果身份证号：" + r);

        // 切割住址
//        int addressX = (int) (width * 0.196);
//        int addressY = (int) (height * 0.496);
//        int addressWidth = (int) (width * 0.45);
//        int addressHeight = (int) (height * 0.182);
        int addressX = (int) (width * 0.19);
        int addressY = (int) (height * 0.5);
        int addressWidth = endWidth; // (int) (width * 0.45);
        int addressHeight = (int) (height * 0.182);

        Rect addressRect = new Rect(addressX, addressY, addressWidth, addressHeight);
        Mat addressMat = new Mat(img, addressRect);

        Mat addressResult = new Mat();
        addressMat.copyTo(addressResult);
        r = Imgcodecs.imwrite("/home/yinlei/s"+s+"address.png", addressResult);
        System.out.println("操作结果住址：" + r);

        // 二值化

        Mat dst = new Mat();
        Size size = new Size(2, 2); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(addressResult, dst, size); // 平滑，降噪

        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图
        // 二值化
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 10);

        boolean r5 = Imgcodecs.imwrite("/home/yinlei/s"+s+"binary2.png", binary);
        System.out.println("地址二值化" + r5);
    }
}
