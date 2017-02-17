package com.vteba.opencv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
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
        Mat sobel = new Mat();
        Imgproc.Sobel(gray, sobel, CvType.CV_8U, 1, 0);

        Mat binary = new Mat();
        Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        Mat ele1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(30, 9));
        Mat ele2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 6));

        Mat dilate = new Mat();
        Imgproc.dilate(binary, dilate, ele2);

        Mat erosion = new Mat();
        Imgproc.erode(dilate, erosion, ele1);

        Imgproc.dilate(erosion, result, ele2);

        return result;
    }

    public static List<MatOfPoint> findRegion(Mat binary) {
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> result = new ArrayList<>();

        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 100) {
                continue;
            }

            MatOfPoint2f point2f = new MatOfPoint2f(matOfPoint.toArray());

            double epsilon = 0.001 * Imgproc.arcLength(point2f, true);

            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(point2f, approx, epsilon, true);

            RotatedRect rect = Imgproc.minAreaRect(point2f);

            MatOfPoint points = new MatOfPoint();
            Imgproc.boxPoints(rect, points);

            double height = points.rows();
            double width = points.cols();
//            if (height > width * 1.2) {
//                continue;
//            }
            result.add(points);
        }

        return result;
    }

    public static void main(String[] args) {
        Mat yl03 = Imgcodecs.imread("/tmp/address.png");
        Mat dst = new Mat();

        Size size = new Size(1, 1); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(yl03, dst, size); // 平滑，降噪
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

        List<MatOfPoint> boxList = findRegion(mat);

        Imgproc.drawContours(yl03, boxList, 0, new Scalar(0, 255, 0), 2);

        Imgcodecs.imwrite("/tmp/contour.png", yl03);
    }
}
