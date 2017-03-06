package com.vteba.opencv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sun.nio.cs.ext.MacThai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yinlei on 17-2-17.
 */
public class FindContoursTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat preprocess(Mat gray) {
        Mat result = new Mat();
//        Mat sobel = new Mat();
//        Imgproc.Sobel(gray, sobel, CvType.CV_8U, 1, 0);


        Mat binary = new Mat();
        //Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 13, 10);
        Imgcodecs.imwrite("/tmp/binary.png", binary);

//        Mat ele1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
//        Mat ele2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
//
//        Mat dilate = new Mat();
//        Imgproc.dilate(binary, dilate, ele2);
//
//        Mat erosion = new Mat();
//        Imgproc.erode(dilate, erosion, ele1);
//
//        Imgproc.dilate(erosion, result, ele2);
//
//        return result;

        return binary;
    }

    public static List<MatOfPoint> findRegion(Mat binary, Mat source) {
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        List<MatOfPoint> simpleContourList = new ArrayList<>();
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 200) {
                continue;
            }
            simpleContourList.add(matOfPoint);
        }

        List<MatOfPoint> result = new ArrayList<>();
        int i = 1;

        Collections.sort(simpleContourList, (o1, o2) -> {
            Rect o1Rect = Imgproc.boundingRect(o1);
            Rect o2Rect = Imgproc.boundingRect(o2);

            int h1 = o1Rect.height;
            int h2 = o2Rect.height;
            int h = Math.max(h1, h2);

            if (Math.abs(o1Rect.y - o2Rect.y) < h) { // 同一行
                if (o1Rect.x < o2Rect.x) {
                    return -1;
                } else if (o1Rect.x > o2Rect.x) {
                    return 1;
                }
                return 0;
            } else {
                return -1;
            }
        });

        for (MatOfPoint matOfPoint : simpleContourList) {
//            double area = Imgproc.contourArea(matOfPoint);
//            if (area < 200) {
//                continue;
//            }

            Rect boundingRect = Imgproc.boundingRect(matOfPoint);
            System.out.println(i + " height: " + boundingRect.height + ", width: " + boundingRect.width);
            Mat mat = new Mat(source, boundingRect);
            Imgcodecs.imwrite("/tmp/" + i++ + ".png", mat);


//            MatOfPoint2f point2f = new MatOfPoint2f(matOfPoint.toArray());

            // 轮廓近似，作用比较小
//            double epsilon = 0.001 * Imgproc.arcLength(point2f, true);
//
//            MatOfPoint2f approx = new MatOfPoint2f();
//            Imgproc.approxPolyDP(point2f, approx, epsilon, true);

//            RotatedRect rect = Imgproc.minAreaRect(point2f); // 寻找最小包围矩形
//
//            Rect minRect = rect.boundingRect();
//            Mat mat = new Mat(source, minRect);
//            Imgcodecs.imwrite("/tmp/" + i++ + ".png", mat);

//            MatOfPoint points = new MatOfPoint();
//            Imgproc.boxPoints(rect, points);
//
//            double height = points.rows();
//            double width = points.cols();
////            if (height > width * 1.2) {
////                continue;
////            }

//            result.add(points);
        }

        return result;
    }

    public static void main(String[] args) {
        Mat yl03 = Imgcodecs.imread("/home/yinlei/Sample/guomin.jpg");
        Mat dst = new Mat();

        Size size = new Size(3, 3); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(yl03, dst, size); // 平滑，降噪

//        Imgproc.GaussianBlur(yl03, dst, size, 1);


        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        // 二值化
        //Mat binary = new Mat();
        //Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 10);

        Mat mat = preprocess(gray);

//        List<MatOfPoint> contourList = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//        System.out.println();

        List<MatOfPoint> boxList = findRegion(mat, yl03);

//        Imgproc.drawContours(yl03, boxList, 0, new Scalar(0, 255, 0, 0), 2);

//        Imgcodecs.imwrite("/tmp/contour.png", yl03);

        //testContours();
    }

    public static void testContours() {
        Mat yl03 = Imgcodecs.imread("/home/yinlei/idcard2select/id37.jpg");
        Mat dst = new Mat();

        Size size = new Size(1, 1); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(yl03, dst, size); // 平滑，降噪
        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        int i = 1;
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 100) {
                continue;
            }

            Rect rect = Imgproc.boundingRect(matOfPoint);

            Imgproc.rectangle(yl03, rect.tl(), rect.br(), new Scalar(0, 255, 0));

            Mat poi = new Mat(yl03, rect);

            Imgcodecs.imwrite("/tmp/" + i++ + ".png", poi);
        }
    }
}
