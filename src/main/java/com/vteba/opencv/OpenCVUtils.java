package com.vteba.opencv;

import org.apache.commons.lang3.math.NumberUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * opencv图像处理相关的工具类
 *
 * @author yinlei
 * @since 2017-1-17
 */
public class OpenCVUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCVUtils.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 将Java输入流转成OpenCV的Matrics结构
     *
     * @param stream 图片文件流
     * @return opencv Matrics
     */
    public static Mat toMat(InputStream stream) {
        Mat result;
        try {
            BufferedImage image = ImageIO.read(stream);
            result = Mat.eye(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            result.put(0, 0, pixels);
        } catch (IOException e) {
            LOGGER.error("OpenCVUtils stream to mat error.", e);
            throw new RuntimeException("OpenCVUtils stream to mat error.", e);
        }
        return result;
    }

    /**
     * 将opencv的matrics转成字节数组
     *
     * @param mat 待转换的matrics
     * @param ext 文件扩展名，如 .png .jpg（jpg格式压缩更多）
     * @return 转换后的字节数组
     */
    public static byte[] toBytes(Mat mat, String ext) {
        byte[] result;
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(ext, mat, matOfByte);
            result = matOfByte.toArray();
        } catch (Exception e) {
            LOGGER.error("OpenCVUtils mat to string error, file ext is [{}].", ext, e);
            throw new RuntimeException("OpenCVUtils mat to stream error, file ext is[" + ext, e);
        }
        return result;
    }

    public Mat read(String filePath) {
        try {
            return Imgcodecs.imread(filePath);
        } catch (Exception e) {
            LOGGER.error("read file[{}] to opencv mat error.", filePath, e);
        }
        return null;
    }

    public boolean write(Mat mat, String filePath) {
        try {
            return Imgcodecs.imwrite(filePath, mat);
        } catch (Exception e) {
            LOGGER.error("write opencv mat to file[{}] error.", filePath, e);
        }
        return false;
    }

    /**
     * <p>将图片切割为4个部分：姓名，民族，地址，身份证号码。其中没有切割生日和性别，因为这两项都能够从身份证号码中获取到，而数字的识别正确率远高于汉字。</p>
     *
     * @param filePath 待切割图片路径
     * @return 切割后的图片，包含4个部分
     */
    public static Map<String, Mat> getCardSlice(String filePath) {
        Mat image = Imgcodecs.imread(filePath);
        return getCardSlice(image);
    }

    /**
     * <p>将图片切割为4个部分：姓名，民族，地址，身份证号码。其中没有切割生日和性别，因为这两项都能够从身份证号码中获取到，而数字的识别正确率远高于汉字。</p>
     *
     * @param stream 待切割图片流
     * @return 切割后的图片，包含4个部分
     */
    public static Map<String, Mat> getCardSlice(InputStream stream) {
        Mat image = toMat(stream);
        return getCardSlice(image);
    }

    /**
     * <p>将图片切割为4个部分：姓名，民族，地址，身份证号码。其中没有切割生日和性别，因为这两项都能够从身份证号码中获取到，而数字的识别正确率远高于汉字。</p>
     * <p>身份证的宽高比是85.6mm * 54mm～=1.5852</p>
     * @param image 待切割图片
     * @return 切割后的图片，包含4个部分
     */
    public static Map<String, Mat> getCardSlice(Mat image) {
        Map<String, Mat> result = new HashMap<String, Mat>(4);

        Size size = new Size(475, 300);

//        Mat resizeMat = new Mat();
        Imgproc.resize(image, image, size);
//        Imgcodecs.imwrite("/tmp/resize.png", resizeMat);

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

            int codeX = (int) (width * 0.346);
            int codeY = (int) (height * 0.795);
            int codeWidth = (int) (width * 0.585);
            int codeHeight = (int) (height * 0.125);

            Rect codeRect = new Rect(codeX, codeY, codeWidth, codeHeight);
            Mat codeMat = new Mat(image, codeRect);

            Mat codeResult = new Mat();
            codeMat.copyTo(codeResult);
            result.put("code", codeResult);

            // 切割住址，省市区可以通过身份证号码的前三位进行验证，但是不一定正确，因为存在市区合并或者撤销的情况，但是身份证号码不变了
            // 另外有一些身份证地址是以市开头的，并没有省，例如杭州市
            int addressX = (int) (width * 0.19);
            int addressY = (int) (height * 0.5);
            int addressWidth = (int) (width * 0.448);
            int addressHeight = (int) (height * 0.272);

            Rect addressRect = new Rect(addressX, addressY, addressWidth, addressHeight);
            Mat addressMat = new Mat(image, addressRect);

            Mat addressResult = new Mat();
            addressMat.copyTo(addressResult);
            result.put("address", addressResult);
        } catch (Exception e) {
            LOGGER.error("slice identity card mat(image) error.", e);
        }

        return result;
    }

    /**
     * 获取身份证号码的精确轮廓
     * @param code 身份证code Mat
     * @return 精确的轮廓Mat
     */
    public static Mat codeContour(Mat code) {
        Mat dst = new Mat();

        Size size = new Size(4, 4); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(code, dst, size); // 平滑，降噪

        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        Mat binary = new Mat(); // 二值化
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 13, 10);

        // 查找轮廓
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // 如果是截取整块的话，可以找到最小的xy坐标，和最大的xy坐标，然后切割

        List<Integer> xlist = new ArrayList<>();
        List<Integer> ylist = new ArrayList<>();

        Set<Integer> heightSet = new HashSet<>();
        Set<Integer> widthSet = new HashSet<>();

        int sourceWidth = code.width();

        // 过滤小的切片，同时选取最大最小的xy坐标，以及宽度和高度
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 150) {
                continue;
            }
            Rect rect = Imgproc.boundingRect(matOfPoint);
            if (rect.width == sourceWidth - 2) { // 原图，去掉
                continue;
            }
            xlist.add(rect.x);
            ylist.add(rect.y);

            heightSet.add(rect.height);
            widthSet.add(rect.width);
        }


        int maxx = Collections.max(xlist);
        int minx = Collections.min(xlist);
        int miny = Collections.min(ylist);

        int meanWidth = MathUtils.media(widthSet); //MathUtils.mean(widthSet);

        int width = maxx - minx + meanWidth;
        int height = Collections.max(heightSet) + 8; // 切割的太靠近边缘了，加几个像素

        int sourceHeight = code.height();
        // 防止宽度溢出
        if (minx + width > sourceWidth) {
            width = sourceWidth - minx;
        }
        // 防止高度溢出
        if (miny + height> sourceHeight) {
            height = sourceHeight - miny;
        }

        Rect re = new Rect(minx, miny, width, height);
        Mat codeMat = new Mat(code, re);
        Mat newCode = new Mat();
        codeMat.copyTo(newCode);

        return newCode;
    }

    /**
     * 获取姓名的精确轮廓，可以再优化
     * @param name name Mat
     * @return 精确的轮廓Mat
     */
    public static Mat nameContour(Mat name) {
        Mat dst = new Mat();

        Size size = new Size(4, 4); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(name, dst, size); // 平滑，降噪

        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        Mat binary = new Mat(); // 二值化
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 13, 10);

        // 查找轮廓
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Map<Integer, Rect> xlist = new HashMap<>();
        Map<Integer, Rect> ylist = new HashMap<>();


        int sourceWidth = name.width();
//        int i = 1;
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 200) { // 这个参数不好控制
                continue;
            }
            Rect rect = Imgproc.boundingRect(matOfPoint);
            if (sourceWidth == rect.width + 2) {
                continue;
            }
            xlist.put(rect.x, rect);
            ylist.put(rect.y, rect);


//            Mat temp = new Mat(name, rect);
//            Imgcodecs.imwrite("/tmp/aa_name" + i++ + ".png", temp);
        }

        // 如果是截取整块的话，可以找到最小的xy坐标，和最大的xy坐标，然后切割


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


        Rect re = new Rect(minx, miny, maxx - minx + rmaxx.width + abs, maxy - miny + rmaxy.height);
        Mat nameMat = new Mat(name, re);
        return nameMat;
//        Mat newName = new Mat();
//        nameMat.copyTo(newName);
//        return newName;
    }

    /**
     * 获取民族的精确轮廓，可以再优化
     * @param nation nation Mat
     * @return 精确的轮廓Mat
     */
    public static Mat nationContour(Mat nation) {
        Mat dst = new Mat();

        Size size = new Size(4, 4); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(nation, dst, size); // 平滑，降噪

        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        Mat binary = new Mat(); // 二值化
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 13, 10);

        // 查找轮廓
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Map<Integer, Rect> xlist = new HashMap<>();
        Map<Integer, Rect> ylist = new HashMap<>();


        int sourceWidth = nation.width();
//        int i = 1;
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 300) { // 这个参数不好控制
                continue;
            }
            Rect rect = Imgproc.boundingRect(matOfPoint);
            if (sourceWidth == rect.width + 2) {
                continue;
            }
            xlist.put(rect.x, rect);
            ylist.put(rect.y, rect);


//            Mat temp = new Mat(name, rect);
//            Imgcodecs.imwrite("/tmp/aa_name" + i++ + ".png", temp);
        }

        // 如果是截取整块的话，可以找到最小的xy坐标，和最大的xy坐标，然后切割


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

        Rect re = new Rect(minx - abs, miny, maxx - minx + rmaxx.width + abs, maxy - miny + rmaxy.height);
        Mat nameMat = new Mat(nation, re);
        return nameMat;
    }

    public static Mat getSlice(Mat image, Rect rect) {
        Mat slice = new Mat(image, rect);

        Mat result = new Mat();
        slice.copyTo(result);
        return result;
    }

    public static void main(String[] a) throws Exception {
//        FileInputStream fis = new FileInputStream("/home/yinlei/yinlei.png");
//        Mat mat = toMat(fis);
//
//        toBytes(mat, ".jpg");

        Mat mat = Imgcodecs.imread("/home/yinlei/Sample/hx.jpg");

        Map<String, Mat> map = FindContoursTest.getCardSlice2(mat);

        Mat codeMat = map.get("code");

        Mat newCode = codeContour(codeMat);

        Imgcodecs.imwrite("/tmp/aa_code.png", newCode);

        Mat nameMat = map.get("name");
        Mat newName = nameContour(nameMat);

        Imgcodecs.imwrite("/tmp/aa_name.png", newName);

        Mat nationMat = map.get("nation");
        Mat newNation = nationContour(nationMat);
        Imgcodecs.imwrite("/tmp/aa_nation.png", newNation);
    }
}
