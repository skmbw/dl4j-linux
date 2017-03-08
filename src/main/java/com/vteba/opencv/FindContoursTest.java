package com.vteba.opencv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sun.nio.cs.ext.MacThai;

import java.util.*;

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
            if (area < 500) {
                continue;
            }
            simpleContourList.add(matOfPoint);
        }

        // 如果是截取整块的话，可以找到最小的xy坐标，和最大的xy坐标，然后切割

        List<MatOfPoint> result = new ArrayList<>();
        int i = 1;

        Map<Integer, Rect> xlist = new HashMap<>();
        Map<Integer, Rect> ylist = new HashMap<>();

        int w = 0;
        int h = 0;

        Collections.sort(simpleContourList, (o1, o2) -> {
            Rect o1Rect = Imgproc.boundingRect(o1);
            Rect o2Rect = Imgproc.boundingRect(o2);

            xlist.put(o1Rect.x, o1Rect);
            //xlist.add(o2Rect.x);
            ylist.put(o1Rect.y, o1Rect);
            //ylist.add(o2Rect.y);

            if (o1Rect.y > o2Rect.y) {
                if (o1Rect.x < o2Rect.x) {
                    return -1;
                } else if (o1Rect.x > o2Rect.x) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (o1Rect.y < o2Rect.y) {
                if (o1Rect.x < o2Rect.x) {
                    return 1;
                } else if (o1Rect.x > o2Rect.x) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }

//            int h1 = o1Rect.height;
//            int h2 = o2Rect.height;
//            int h = Math.max(h1, h2);
//
//
//
//            int abs = Math.abs(o1Rect.y - o2Rect.y);
//
//            if (abs < h) { // 同一行
//                if (o1Rect.x < o2Rect.x) {
//                    return -1;
//                } else if (o1Rect.x > o2Rect.x) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            } else if (abs == h) {
//                return 0;
//            } else {
//                return -1;
//            }
        });

        int maxx = Collections.max(xlist.keySet());
        int minx = Collections.min(xlist.keySet());
        int maxy = Collections.max(ylist.keySet());
        int miny = Collections.min(ylist.keySet());

        // 这样做，是要加上最大的切片的width和height
        Rect rmaxx = xlist.get(maxx);
        Rect rmaxy = ylist.get(maxy);

        // 因为第一个字可能只切割了一半，所以要，将他变成方形，增加不足的部分
        Rect rminx = xlist.get(minx);
        // Rect rminy = ylist.get(miny);
        Rect rminy = xlist.get(maxx); // 单取一个可能不行，取两个比较以下，选择大的

        int minxWidth = rminx.width;
        int minxHeight = rminx.height;
        int minxAbs = Math.abs(minxWidth - minxHeight);

        int minyWidth = rminy.width;
        int minyHeight = rminy.height;
        int minyAbs = Math.abs(minyWidth - minyHeight);

        int abs = Math.max(minxAbs, minyAbs);

//        int minxWidth = rminx.width;
//        int minxHeight = rminx.height;
//        int abs = Math.abs(minxWidth - minxHeight);


        Rect re = new Rect(minx - abs, miny, maxx - minx + rmaxx.width + abs, maxy - miny + rmaxy.height);
        Mat addressMat = new Mat(source, re);
        Mat newAddress = new Mat();
        addressMat.copyTo(newAddress);
        Imgcodecs.imwrite("/tmp/addressNew.png", newAddress);

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
        Mat yl03 = Imgcodecs.imread("/home/yinlei/Sample/hx.jpg");
        Map<String, Mat> matMap = getCardSlice2(yl03);
        Mat dst = new Mat();

        Size size = new Size(4, 4); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(matMap.get("address"), dst, size); // 平滑，降噪

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

        List<MatOfPoint> boxList = findRegion(mat, matMap.get("address"));

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

    public static Map<String, Mat> getCardSlice2(Mat image) {
        Map<String, Mat> result = new HashMap<String, Mat>(4);

        //Size size = new Size(475, 300);
        //Imgproc.resize(image, image, size);

        int height = image.rows();
        int width = image.cols();

        try {
            // 切割姓名
            int nameX = (int) (width * 0.18);
            int nameY = (int) (height * 0.12);
            int nameWidth = (int) (width * 0.28);
            int nameHeight = (int) (height * 0.14);
            Rect nameRect = new Rect(nameX, nameY, nameWidth, nameHeight);
            Mat nameMat = new Mat(image, nameRect);
            Mat nameResult = new Mat();
            nameMat.copyTo(nameResult);
            result.put("name", nameResult);

            // 切割民族

            int nationX = (int) (width * 0.38);
            int nationY = (int) (height * 0.246);
            int nationWidth = (int) (width * 0.18);
            int nationHeight = (int) (height * 0.12);
            Rect nationRect = new Rect(nationX, nationY, nationWidth, nationHeight);
            Mat nationMat = new Mat(image, nationRect);
            Mat nationResult = new Mat();
            nationMat.copyTo(nationResult);
            result.put("nation", nationResult);

            // 切割身份证号码

            int codeX = (int) (width * 0.338);
            int codeY = (int) (height * 0.79);
            int codeWidth = (int) (width * 0.593);
            int codeHeight = (int) (height * 0.13);

            Rect codeRect = new Rect(codeX, codeY, codeWidth, codeHeight);
            Mat codeMat = new Mat(image, codeRect);

            Mat codeResult = new Mat();
            codeMat.copyTo(codeResult);
            result.put("code", codeResult);

            // 切割住址，省市区可以通过身份证号码的前三位进行验证，但是不一定正确，因为存在市区合并或者撤销的情况，但是身份证号码不变了
            // 另外有一些身份证地址是以市开头的，并没有省，例如杭州市
            int addressX = (int) (width * 0.18);
            int addressY = (int) (height * 0.49);
            int addressWidth = (int) (width * 0.446);
            int addressHeight = (int) (height * 0.276);

            Rect addressRect = new Rect(addressX, addressY, addressWidth, addressHeight);
            Mat addressMat = new Mat(image, addressRect);

            Mat addressResult = new Mat();
            addressMat.copyTo(addressResult);
            result.put("address", addressResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
