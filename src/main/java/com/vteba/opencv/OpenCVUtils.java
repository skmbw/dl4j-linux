package com.vteba.opencv;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import java.text.SimpleDateFormat;
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
        int sourceHeight = code.height();

        int areas = sourceWidth * sourceHeight;

        //LOGGER.info("code height=[{}], width=[{}], area=[{}]", sourceHeight, sourceWidth, areas);

//        int limit = 100;
//        if (areas >= 300000) {
//            limit = 200;
//        } else if (areas < 300000 && areas >= 90000) {
//            limit = 150;
//        } else if (areas < 90000 && areas >= 40000) {
//            limit = 120;
//        }

        // 过滤小的切片，同时选取最大最小的xy坐标，以及宽度和高度
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 100) {
                continue;
            }
            Rect rect = Imgproc.boundingRect(matOfPoint);
            if (rect.width + 2 == sourceWidth && rect.height + 2 == sourceHeight) { // 原图，去掉
                continue;
            }
            xlist.add(rect.x);
            ylist.add(rect.y);

            heightSet.add(rect.height);
            widthSet.add(rect.width);
        }


        int maxx = Collections.max(xlist);
        int minx = Collections.min(xlist);

        Collections.sort(ylist);
        // ylist.remove(ylist.size() - 1); // 删除最大的
        // ylist.remove(0); // 删除最小的
        int miny = Collections.min(ylist); // 取最小的值

        int meanWidth = MathUtils.media(widthSet); //MathUtils.mean(widthSet);

        int width = maxx - minx + meanWidth;
        int height = Collections.max(heightSet) + 8; // 切割的太靠近边缘了，加几个像素

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
        int sourceHeight = name.height();

        int areas = sourceHeight * sourceWidth;

//        int limit = 100;
//        if (areas >= 100000) {
//            limit = 300;
//        } else if (areas < 100000 && areas >= 60000) {
//            limit = 200;
//        } else if (areas < 60000 && areas >= 40000) {
//            limit = 150;
//        }

        //LOGGER.info("name height=[{}], width=[{}], area=[{}]", sourceHeight, sourceWidth, areas);

        int i = 1;
        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 100) { // 这个参数不好控制，大了，过滤掉有用信息，小了，起不到过滤作用
                continue;
            }
            Rect rect = Imgproc.boundingRect(matOfPoint);
            if (sourceWidth == rect.width + 2 && sourceHeight == rect.height + 2) {
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

        minx = minx - abs;

//        if (x > sourceWidth * 0.33) { // x坐标，大于整个宽度的三分之一
//            x = (int) (sourceWidth * 0.33) - abs - 20;
//        }

        int width = maxx - minx + rmaxx.width + abs;
        int height = maxy - miny + rmaxy.height;

        // 防止宽度溢出
        if (minx + width > sourceWidth) {
            width = sourceWidth - minx;
        }
        // 防止高度溢出
        if (miny + height> sourceHeight) {
            height = sourceHeight - miny;
        }

        if (minx <= 0) {
            minx = 1;
        }

        if (miny <= 0) {
            miny = 1;
        }

        Rect re = new Rect(minx, miny, width, height);
        Mat nameMat = new Mat(name, re);
        return nameMat;
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

        Map<Integer, Rect> xmap = new HashMap<>();
        Map<Integer, Rect> ymap = new HashMap<>();


        int sourceWidth = nation.width();
        int sourceHeight = nation.height();

        int areas = sourceHeight * sourceWidth;

//        int limit = 100;
//        if (areas >= 100000) {
//            limit = 300;
//        } else if (areas < 100000 && areas >= 60000) {
//            limit = 200;
//        } else if (areas < 60000 && areas >= 40000) {
//            limit = 150;
//        }

        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 100) { // 这个参数不好控制
                continue;
            }
            Rect rect = Imgproc.boundingRect(matOfPoint);
            if (sourceWidth == rect.width + 2 && sourceHeight == rect.height + 2) { // 原图，去掉
                continue;
            }
            xmap.put(rect.x, rect);
            ymap.put(rect.y, rect);
        }

        // 如果是截取整块的话，可以找到最小的xy坐标，和最大的xy坐标，然后切割


        List<Integer> xlist = new ArrayList<>(xmap.keySet());
        List<Integer> ylist = new ArrayList<>(ymap.keySet());

        Collections.sort(xlist);
        Collections.sort(ylist);

        int xsize = xlist.size();
        int ysize = ylist.size();

        int maxx = xlist.get(xsize - 1); // Collections.max(xmap.keySet());
        int minx = xlist.get(0); // Collections.min(xmap.keySet());

        int maxy = ylist.get(ysize - 1); // Collections.max(ymap.keySet());
        int miny = ylist.get(0); // Collections.min(ymap.keySet());

        // 这样做，是要加上最大的切片的width和height
        Rect rmaxx = xmap.get(maxx);
        Rect rmaxy = ymap.get(maxy);

        // 因为第一个字可能只切割了一半，所以要，将他变成方形，增加不足的部分
        Rect rminx = xmap.get(minx);
        Rect rminy = xmap.get(maxx); // 单取一个可能不行，取两个比较以下，选择大的

        int minxWidth = rminx.width;
        int minxHeight = rminx.height;
        int minxAbs = Math.abs(minxWidth - minxHeight);

        int minyWidth = rminy.width;
        int minyHeight = rminy.height;
        int minyAbs = Math.abs(minyWidth - minyHeight);

        int abs = Math.max(minxAbs, minyAbs);

        //LOGGER.info("nation height=[{}], width=[{}], area=[{}]", sourceHeight, sourceWidth, sourceHeight * sourceWidth);

        int width = maxx - minx + Math.max(rmaxx.width, rmaxy.width) + abs;

        int maxxHeight = rmaxx.height;
        int maxyHeight = rmaxy.height;

        // 因为，民族就1行，取行高即可
        Integer[] heights = new Integer[4];
        heights[0] = minxHeight;
        heights[1] = minyHeight;
        heights[2] = maxxHeight;
        heights[3] = maxyHeight;

        int avg = MathUtils.mean(heights);
        int media = MathUtils.media(heights);

        // int hgt1 = Math.max(avg, media); // 取中位数和平均值的最大值
        // int hgt2 = maxy - miny; // 最大行高，减最小行高
        int hgt = NumberUtils.max(maxyHeight, maxxHeight, minxHeight, minyHeight, avg, media, maxy - miny);

        int height = hgt; // maxy - miny + Math.max(rmaxy.height, rmaxx.height);

        // 防止宽度溢出
        if (minx + width > sourceWidth) {
            width = sourceWidth - minx;
        }
        // 防止高度溢出
        if (miny + height> sourceHeight) {
            height = sourceHeight - miny;
        }

        minx = minx - abs;
        if (minx <= 0) {
            minx = 1;
        }

        if (miny <= 0) {
            miny = 1;
        }

        Rect re = new Rect(minx, miny, width, height);
        Mat nationMat = new Mat(nation, re);
        return nationMat;
    }

    public static Mat addressContour(Mat address) {
        Mat dst = new Mat();
//        Imgcodecs.imwrite("/tmp/aaa_address.png", address);
        Size size = new Size(4, 4); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(address, dst, size); // 平滑，降噪

        Mat gray = new Mat();
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        Mat binary = new Mat(); // 二值化
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 13, 10);

        // 查找轮廓
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Map<Integer, Rect> xmap = new HashMap<>();
        Map<Integer, Rect> ymap = new HashMap<>();

        int sourceWidth = address.width();
        int sourceHeight = address.height();
        int areas = sourceWidth * sourceHeight;

//        int limit = 100;
//        if (areas >= 200000) {
//            limit = 300;
//        } else if (areas < 200000 && areas >= 60000) {
//            limit = 200;
//        } else if (areas < 60000 && areas >= 40000) {
//            limit = 150;
//        }

        //LOGGER.info("address height=[{}], width=[{}], area=[{}]", sourceHeight, sourceWidth, areas);

        for (MatOfPoint matOfPoint : contourList) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area < 180) {
                continue;
            }

            Rect rect = Imgproc.boundingRect(matOfPoint);

            if (sourceWidth == rect.width + 2 && sourceHeight == rect.height + 2) {
                continue;
            }

            xmap.put(rect.x, rect);
            ymap.put(rect.y, rect); // 可能会被覆盖
        }


        List<Integer> xlist = new ArrayList<>(xmap.keySet());
        List<Integer> ylist = new ArrayList<>(ymap.keySet());

        Collections.sort(xlist);
        Collections.sort(ylist);

        int xsize = xlist.size();
        int ysize = ylist.size();

        int maxx = xlist.get(xsize - 1);
        int minx = xlist.get(0);

        int maxy = ylist.get(ysize - 1);
        int miny = ylist.get(0);

        // 这样做，是要加上最大的切片的width和height
        Rect rmaxx = xmap.get(maxx);
        Rect rmaxy = ymap.get(maxy);

        Rect rminx = xmap.get(minx);
        Rect rminy = ymap.get(miny);

        // 因为第一个字可能只切割了一半，所以要，将他变成方形，增加不足的部分
        // 单取一个可能不行，取两个比较以下，选择大的

        int minxWidth = rminx.width;
        int minxHeight = rminx.height;
        int minxAbs = Math.abs(minxWidth - minxHeight);

        int maxxWidth = rmaxx.width;
        int maxxHeight = rmaxx.height;

        int minyWidth = rminy.width;
        int minyHeight = rminy.height;
        int minyAbs = Math.abs(minyWidth - minyHeight);

        int maxyWidth = rmaxy.width;
        int maxyHeight = rmaxy.height;

        int abs = Math.max(minxAbs, minyAbs);

        minx = minx - abs - 3;

        if (minx <= 0) {
            minx = 0;
        }

        miny -= 3;
        if (miny <= 0) {
            miny = 0;
        }

        int maxWidth = Math.max(rmaxx.width, rmaxy.width);
//        int maxWidth = NumberUtils.max(minxWidth, minyWidth, maxxWidth, maxyWidth);
        //maxWidth = (int) (maxWidth * 1.5);

        int width = maxx - minx + maxWidth + abs;
        int height = maxy - miny + Math.max(rmaxy.height, rmaxx.height) + 3;

        // 防止宽度溢出
        if (minx + width > sourceWidth) {
            width = sourceWidth - minx; // 这个已经是最大的了，不需要加abs
        }
        // 防止高度溢出
        if (miny + height> sourceHeight) {
            height = sourceHeight - miny;
        }

        Rect re = new Rect(minx , miny, width, height);
        Mat addressMat = new Mat(address, re);
        return addressMat;
    }

    public static Mat getSlice(Mat image, Rect rect) {
        Mat slice = new Mat(image, rect);

        Mat result = new Mat();
        slice.copyTo(result);
        return result;
    }

    public static Mat resize(Mat image, double width) {
        int w = image.width();
        if (w == width) { // 无需缩放
            return image;
        }
        int h = image.height();
        double rate = width / w;
        double height = h * rate;
        Size size = new Size(width, height);
        Imgproc.resize(image, image, size);
        return image;
    }

    public static void main(String[] a) throws Exception {
//        FileInputStream fis = new FileInputStream("/home/yinlei/yinlei.png");
//        Mat mat = toMat(fis);
//
//        toBytes(mat, ".jpg");

        Mat mat = Imgcodecs.imread("/home/yinlei/Sample/hx.jpg");

        mat = resize(mat, 1000D); // 要统一归一化，否则，后面不好处理

        Map<String, Mat> map = FindContoursTest.getCardSlice2(mat);

        Mat codeMat = map.get("code");

        Mat newCode = codeContour(codeMat);
        codeRecognize(newCode);
        Imgcodecs.imwrite("/tmp/aa_code.png", newCode);

        Mat nameMat = map.get("name");
        Mat newName = nameContour(nameMat);
        textRecognize(newName, 1);
        Imgcodecs.imwrite("/tmp/aa_name.png", newName);

        Mat nationMat = map.get("nation");
        Mat newNation = nationContour(nationMat);
        textRecognize(newNation, 2);
        Imgcodecs.imwrite("/tmp/aa_nation.png", newNation);

        Mat addressMat = map.get("address");
        Mat newAddress = addressContour(addressMat);
        textRecognize(newAddress, 3);
        Imgcodecs.imwrite("/tmp/aa_address.png", newAddress);
    }

    public static void textRecognize(Mat image, int type) {
        ITesseract instance = new Tesseract();
        String prefix = "";
        if (type == 1) {
            prefix = "姓名：";
            instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE); // 单独一行
        } else if (type == 2) {
            instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
            prefix = "民族：";
        } else if (type == 3) {
            instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);
            prefix = "地址：";
        }
        //instance.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_CUBE_ONLY);

        instance.setLanguage("shz13");
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

        MatImageUtils matImage = new MatImageUtils(image, ".png");

        try {
            String result = instance.doOCR(matImage.getImage());
            result = MathUtils.trim(result);

            LOGGER.info(prefix + result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }



    public static void codeRecognize(Mat image) {
        ITesseract instance = new Tesseract();
        instance.setLanguage("shz12");
        instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE); // 单独一行
        // shz11是由地址训练的汉字，图片被缩放。
        // shz9也是数字的，168张身份证号码，图片被缩放。
        // shz10也是数字的，168张身份证号码，图片无缩放。
        // shz12是身份证号码，纯数字的，原始图，精简的40多张。（OK）
        // shz13是地址训练的汉字，230多张原始图片（OK）
        // shz14室shz13加我同学给做的5张图训练的汉字，字库更大
        List<String> configs = new ArrayList<>();
        configs.add("digits");
        instance.setConfigs(configs);
        instance.setDatapath("/usr/local/tesseract-3.04.01/tessdata/");

        MatImageUtils matImage = new MatImageUtils(image, ".png");

        try {
            String result = instance.doOCR(matImage.getImage());
            if (StringUtils.isBlank(result)) {
                LOGGER.error("未检测到身份证号码数据。");
                return;
            }
            result = MathUtils.trim(result);
            LOGGER.info("身份证号码：" + result);
            int len = result.length();
            if (len != 15 && len != 18) {
                LOGGER.error("身份证代码长度识别错误code=[{}]", result);
            } else {
                if (len == 18) {
                    String birthday = result.substring(6, 14);
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                        Date date = format.parse(birthday);
                        LOGGER.info("出生年月：[{}]", format.format(date));
                    } catch (Exception e) {
                        LOGGER.error("格式化18生日错误[{}]", birthday);
                    }
                    String g = result.substring(16, 17);
                    int i = Integer.parseInt(g);
                    String gender = "";
                    if (i % 2 == 0) {
                        gender = "女";
                    } else if (i % 2 == 1) {
                        gender = "男";
                    }
                    LOGGER.info("性别：[{}]", gender);
                } else if (len == 15) {
                    String birthday = result.substring(6, 12);
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
                        Date date = format.parse(birthday);
                        SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd");
                        LOGGER.info("出生年月：[{}]", format2.format(date));
                    } catch (Exception e) {
                        LOGGER.error("格式化15生日错误[{}]", birthday);
                    }
                    String g = result.substring(14, 15);
                    int i = Integer.parseInt(g);
                    String gender = "";
                    if (i % 2 == 0) {
                        gender = "女";
                    } else if (i % 2 == 1) {
                        gender = "男";
                    }
                    LOGGER.info("性别：[{}]", gender);
                }

            }

        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }
}
