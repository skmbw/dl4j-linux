package com.vteba.opencv.id;

import com.vteba.opencv.OpenCVUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

/**
 * Created by yinlei on 17-4-25.
 */
public class ResizeTestImage {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static String s = "38";

    public static void main(String[] args) {
//        Mat image = Imgcodecs.imread("/home/yinlei/address2.png");
//        OpenCVUtils.splitBinaryAddress(image);

//        sliceAddress();

        verticalSplit();

//        Mat image = Imgcodecs.imread("/home/yinlei/sfz/sfz_gray5/149386858541424.png", 0);
//
//        Mat resultMat = removeTopBottom(image);
//        Imgcodecs.imwrite("/tmp/a22_yin" + ".png", resultMat);

//        List<Mat> resultMatList = removeBoundary(image);
//        int kk = 0;
//        for (Mat mat : resultMatList) {
//            Imgcodecs.imwrite("/tmp/a22_" + kk++ + ".png", mat);
//        }

//        Mat image = Imgcodecs.imread("/tmp/22.png", 0);
//        Mat dst = new Mat(36, 36, CvType.CV_8UC1);
//
//        Size size = new Size(33, 36); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
//
//        Rect rect = new Rect(0, 0, 33, 36);
//        Mat gray = new Mat(image, rect);
////        Imgcodecs.imwrite("/tmp/211_2.png", gray);
////        gray.put(0, 33, 0);
//
//        gray.copyTo(dst);
//
//
//        Size size2 = new Size(36, 36);
//        Imgproc.resize(dst, dst, size2);
//
//        Imgcodecs.imwrite("/tmp/22_1.png", dst);


    }

    public static void sliceAddress() {
        String path = "/home/yinlei/sfz_test/";
        File files = new File(path);
        for (File file : files.listFiles()) {
            if (file.isFile()) {
                String name = file.getName();
                if (name != null && (name.endsWith(".jpg") || name.endsWith(".png"))) {
                    try {
                        Mat image = Imgcodecs.imread(path + name, 0);
                        image = OpenCVUtils.resize(image, 420);
                        Map<Integer, List<Mat>> charListMap = OpenCVUtils.splitAddressChar(image);

                        int row = charListMap.size();
                        for (int i = 1; i <= row; i++) {
                            List<Mat> charList = charListMap.get(i);
                            int j = 1;
                            for (Mat charMat : charList) {
                                int width = charMat.cols();
                                int height = charMat.rows();
                                if (width > 40) {
                                    List<Mat> resultMatList = removeBoundary(charMat);
                                    int k = 0;
                                    for (Mat mat : resultMatList) {
                                        Imgcodecs.imwrite("/home/yinlei/sfz/sfz_gray/" + System.currentTimeMillis() + i + j++ + k++ + ".png", mat);
                                    }
                                } else {
                                    if (height > 40) {
                                        Mat topBottom = removeTopBottom(charMat);
                                        Imgcodecs.imwrite("/home/yinlei/sfz/sfz_gray/" + System.currentTimeMillis() + i + j++ + ".png", topBottom);
                                    } else {
                                        Imgcodecs.imwrite("/home/yinlei/sfz/sfz_gray/" + System.currentTimeMillis() + i + j++ + ".png", charMat);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                if (file.length() == 0) {
//                    file.delete();
//                }

            }
        }
    }

    public static List<Mat> removeBoundary(Mat source) {
//        double[] hor = OpenCVUtils.projection(source, false);
//        int len = hor.length;
//        int width = source.cols();
//
//        int start = 0;
//        int end = 0;
//        int span = 0;
//
//        for (int i = 0; i < len; i++) {
//            double d = hor[i];
//            if (d != 0) {
//                span = i - span;
//                if (span < 4) {
//                    start = i - 4;
//                }
//                end = i;
//            }
//        }
//
//        int height = end - start + 1;
//        Rect rect = new Rect(0, start, width, height);
//        Mat hmat = new Mat(source, rect);

        Mat hmat = removeTopBottom(source);
        if (hmat == null) {
            return Collections.emptyList();
        }
        int height = hmat.rows();

        double[] vers = OpenCVUtils.projection(hmat, true);
        // 找出分界点（0点）
        List<Integer> zeroList = new ArrayList<>();
        int vlen = vers.length;
        int temp = 0;
        for (int i = 0; i < vlen; i++) {
            double d = vers[i];
            if (d == 0D) {
                if (i - temp <= 2) {
                    continue;
                }
                zeroList.add(i);
                temp = i;
            }
        }

        // 根据分界点，组合坐标点
        int zeroLen = zeroList.size();
        SortedMap<Integer, Integer> pointCouple = new TreeMap<>();

        for (int j = 0; j < zeroLen; j++) {
            int index = zeroList.get(j);
            if (j == 0) {
                pointCouple.put(0, index);
            }
            if (j == zeroLen - 1) {
                pointCouple.put(index, vlen);
            } else {
                pointCouple.put(index, zeroList.get(j + 1));
            }
        }

        List<Mat> resultMatList = new ArrayList<>();
        // 切割
        for (Map.Entry<Integer, Integer> entry : pointCouple.entrySet()) {
            int s = entry.getKey();
            int e = entry.getValue();
            if (e - s < 5) { // 去掉不是字符的空白区域
                continue;
            }
            Rect charRect = new Rect(s, 0, e - s, height);
            Mat mat = new Mat(hmat, charRect);
            resultMatList.add(mat);
        }

        return resultMatList;
    }

    public static Mat removeTopBottom(Mat source) {
        double[] hor = OpenCVUtils.projection(source, false);
        int len = hor.length;
        int width = source.cols();

        int start = 0;
        int end = 0;

        int noneZero = 0;
        for (int i = 0; i < len / 2; i++) {
            double d = hor[i];
            if (d == 0) { // 找前半部分最后一个0
                if (noneZero < 4) { // 非0的小于6个，忽略
                    start = i;
                    noneZero = 0;
                }
            } else {
                noneZero++;
            }
        }

        int zero = 0;
        for (int i = len / 2; i < len; i++) {
            double d = hor[i];
            if (d != 0) {
                end = i;
                zero = 0;
            } else {
                if (zero > 6) {
                    break;
                }
                zero++;
            }
        }

        int height = end - start + 1;
        if (height <= 0) {
//            height = 1;
            return null;
        }
        try {
            Rect rect = new Rect(0, start, width, height);
            Mat hmat = new Mat(source, rect);
            return hmat;
        } catch (CvException cve) {
            cve.printStackTrace();
        }
        return null;
    }

    public static void verticalSplit() {
        String path = "/home/yinlei/sfz/shuozi/";
        File directory = new File(path);

        int i = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String name = file.getName();
                Mat image = Imgcodecs.imread(path + name, 0);
                List<Mat> matList = removeBoundary(image);
                int j = 0;
                for (Mat mat : matList) {
                    Imgcodecs.imwrite(path + "yl_" + i++ + j++ + ".png", mat);
                }
            }
        }
    }
}
