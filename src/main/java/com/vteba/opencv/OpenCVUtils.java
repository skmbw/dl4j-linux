package com.vteba.opencv;

import com.alibaba.fastjson.JSON;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * opencv图像处理相关的工具类
 *
 * @author yinlei
 * @since 2017-1-17
 */
public class OpenCVUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCVUtils.class);

    public static final Map<String, String> ADDRESS = new HashMap<>();

    public static final Map<String, Integer> PROVINCE = new HashMap<>();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try {
            File pathFile = new File("");
            String path = pathFile.getAbsolutePath();
            File file = new File(path + "/src/main/resources/JSON.txt");
//            System.out.println(file.getAbsolutePath());
//            System.out.println(file.getPath());

            FileInputStream fis = new FileInputStream(file);
            String json = IOUtils.toString(fis, Charset.forName("UTF-8"));
            Map<String, Object> ob = JSON.parseObject(json);
            for (Map.Entry<String, Object> entry : ob.entrySet()) {
                ADDRESS.put(entry.getKey(), entry.getValue().toString());
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PROVINCE.put("安徽", 1);
        PROVINCE.put("上海", 1);
        PROVINCE.put("浙江", 1);
        PROVINCE.put("江苏", 1);
        PROVINCE.put("山东", 1);
        PROVINCE.put("江西", 1);
        PROVINCE.put("福建", 1);
        PROVINCE.put("辽宁", 1);
        PROVINCE.put("黑龙江", 1);
        PROVINCE.put("吉林", 1);
        PROVINCE.put("河北", 1);
        PROVINCE.put("北京", 1);
        PROVINCE.put("天津", 1);
        PROVINCE.put("内蒙古", 1);
        PROVINCE.put("山西", 1);
        PROVINCE.put("陕西", 1);
        PROVINCE.put("宁夏", 1);
        PROVINCE.put("甘肃", 1);
        PROVINCE.put("新疆", 1);
        PROVINCE.put("西藏", 1);
        PROVINCE.put("青海", 1);
        PROVINCE.put("贵州", 1);
        PROVINCE.put("四川", 1);
        PROVINCE.put("重庆", 1);
        PROVINCE.put("云南", 1);
        PROVINCE.put("湖北", 1);
        PROVINCE.put("湖南", 1);
        PROVINCE.put("河南", 1);
        PROVINCE.put("广东", 1);
        PROVINCE.put("广西", 1);
        PROVINCE.put("海南", 1);
        PROVINCE.put("香港", 1);
        PROVINCE.put("澳门", 1);
        PROVINCE.put("台湾", 1);


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

        Mat mat = Imgcodecs.imread("/home/yinlei/idcard2/id88.jpg");

        mat = resize(mat, 1000D); // 要统一归一化，否则，后面不好处理

        Map<String, Mat> map = FindContoursTest.getCardSlice2(mat);

        Mat codeMat = map.get("code");

        Mat newCode = codeContour(codeMat);
        String code = codeRecognize(newCode);
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
        String address = textRecognize(newAddress, 3);
        Imgcodecs.imwrite("/tmp/aa_address.png", newAddress);

        postProcess(code, address);

        splitBinaryAddress(newAddress);
    }

    public static void splitBinaryAddress(Mat addressMat) {
        addressMat = Imgcodecs.imread("/home/yinlei/Sample-1000/6.png");
        Imgproc.resize(addressMat, addressMat, new Size(addressMat.width() * 2, addressMat.height() * 2));

        Mat blur = new Mat();
        Imgproc.blur(addressMat, blur, new Size(3, 3));

        Mat gray = new Mat();
        Imgproc.cvtColor(blur, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 25, 10);
        Imgcodecs.imwrite("/tmp/address_binary.png", binary);

        List<Mat> rowMatList = sliceRows(binary); // 按行切割图片
        Map<Integer, List<Mat>> charListMap = splitChar(rowMatList);
        int row = charListMap.size();
        for (int i = 1; i <= row; i++) {
            List<Mat> charList = charListMap.get(i);
            int j = 1;
            for (Mat charMat : charList) {
                Imgcodecs.imwrite("/tmp/" + i + j++ + ".png", charMat);
            }
        }

        List<MatOfPoint> contourList = new ArrayList<>();

        Mat newAddr = new Mat();
        Mat erodeMat = new Mat();
        binary.copyTo(newAddr);
        binary.copyTo(erodeMat);

        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2, 2));
        // peng zhuang
        Mat dilated = new Mat();
        Imgproc.dilate(newAddr, dilated, dilateKernel);
        Imgcodecs.imwrite("/tmp/dilated_address.png", dilated);

        // fu shi
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(4, 4));
        Mat eroded = new Mat();
        Imgproc.erode(erodeMat, eroded, erodeKernel, new Point(-1, -1), 2);


        Imgcodecs.imwrite("/tmp/dilated_eroded_address.png", dilated);

        Mat hierarchy = new Mat();
        Imgproc.findContours(newAddr, contourList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        int i = 1;
        for (MatOfPoint contour : contourList) {
            double area = Imgproc.contourArea(contour);
            if (area < 100) {
                continue;
            }
            Rect rect = Imgproc.boundingRect(contour);

//            Mat mat = new Mat(binary, rect);
//            Imgcodecs.imwrite("/tmp/cc" + i++ + ".png", mat);
        }

        List<Mat> images = new ArrayList<>();
        Mat dst = new Mat();
        binary.copyTo(dst);
        images.add(dst);
        MatOfInt channels = new MatOfInt(0);

        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0, 255F);

        Mat mask = new Mat();

        Imgproc.calcHist(images, channels, mask, hist, histSize, ranges, false);

        int width = hist.cols(); // 1
        int height = hist.rows();

        for (int j = 0; j < height; j++) {

            for (int k = 0; k < width; k++) {
//                Mat col = hist.col(k);
//                int h = col.height();
//                for (int l = 0; l < h; l++) {
//                    double[] dd = col.get(l, k);
//                    System.out.println(Arrays.toString(dd));
//                }
                double[] piex = hist.get(0,k);
//                System.out.println(piex.length + " : " + piex[0]);
            }
        }

        Imgcodecs.imwrite("/tmp/address_binary_hist.png", hist);
    }

    /**
     * 统计灰度图的直方图
     * @param binary 图片
     * @param vertical 是否垂直方向（按列）统计，false（按行统计）
     * @return 直方图数组
     */
    public static double[] projection(Mat binary, boolean vertical) {
        int cols;
        int rows;
        if (vertical) {
            cols = binary.rows();
            rows = binary.cols();
        } else {
            cols = binary.cols();
            rows = binary.rows();
        }

        double[] calcHistRows = new double[rows];

        for (int i = 0; i < rows; i++) {
            Mat cell;
            if (vertical) {
                cell = binary.col(i);
            } else {
                cell = binary.row(i);
            }

            double colval = 0;
            for (int j = 0; j < cols; j++) {
                // the row is 0, because the image is one channel image, is rgb image, vals's size is 3
                double[] vals;
                if (vertical) {
                    vals = cell.get(j, 0);
                } else {
                    vals = cell.get(0, j);
                }
                if (vals == null) {
                    //System.out.println("row=[" + i + "], col=[" + j + "]");
                    continue;
                }
                colval += vals[0];
            }
            calcHistRows[i] = colval;
        }
        return calcHistRows;
    }

    /**
     * 将图片按行切分。
     * @param binary
     * @return
     */
    public static List<Mat> sliceRows(Mat binary) {

        //binary = Imgcodecs.imread("/tmp/aa_nation.png");

        List<Mat> matList = new ArrayList<>();

        int cols = binary.cols();
        int rows = binary.rows();
        double[] calcHistRows = projection(binary, false);
//        double[] calcHistRows = new double[rows];

//        for (int i = 0; i < rows; i++) {
//            Mat row = binary.row(i);
//
//            double colval = 0;
//            for (int j = 0; j < cols; j++) {
//                double[] vals = row.get(0, j); // the row is 0, because the image is one channel image, is rgb image, vals's size is 3
//                if (vals == null) {
//                    System.out.println("row=[" + i + "], col=[" + j + "]");
//                    continue;
//                }
//                colval += vals[0];
//            }
//            calcHistRows[i] = colval;
//        }
//        System.out.println(calcHistRows);

        int maxRowNunber = rows / 5;
        if (maxRowNunber < 22) {
            maxRowNunber = 22;
        }

        Map<String, Integer> pointMap = new HashMap<>();
        pointMap.put("first", -1);
        pointMap.put("second", -1);
        pointMap.put("third", -1);

//        int index = 0;
        int span = 0;
        // 迭代3次最好了，分别处理三个行坐标
        for (int j = 0; j < rows; j++) {
            if (calcHistRows[j] == 0D) {
                int firstX = pointMap.get("first");
                if (firstX != -1) { // 第一个点出现过
                    int diff = j - span;
                    if (diff < maxRowNunber) { // 小于最小行数，用第二个0点替换第一个0点
                        pointMap.put("first", j);
                        span = j;
                        continue; // 现在在处理第一个点，可以直接下一次循环
                    } else {
                        pointMap.put("firstEnd", j);
                        span = j;
                        break;
                    }
                } else { // 第一个点还没有出现
                    pointMap.put("first", j);
                    span = j;
                    continue;
                }



            }

//            index++;
        }

//        index = 0;
//        span = 0;

        LOGGER.info("span={}", span);

        // 迭代3次最好了，分别处理三个行坐标
        for (int j = span; j < rows; j++) {
            if (calcHistRows[j] == 0D) {
                int firstX = pointMap.get("second");
                if (firstX != -1) { // 第一个点出现过
                    int diff = j - span;
                    if (diff < maxRowNunber) { // 小于最小行数，用第二个0点替换第一个0点
                        pointMap.put("second", j);
                        span = j;
                        continue; // 现在在处理第一个点，可以直接下一次循环
                    } else {
                        pointMap.put("secondEnd", j);
                        span = j;
                        break;
                    }
                } else { // 第一个点还没有出现
                    pointMap.put("second", j);
                    span = j;
                    continue;
                }



            }

//            index++;
        }

//        index = 0;
//        span = 0;
        LOGGER.info("span={}", span);
        // 迭代3次最好了，分别处理三个行坐标
        if (rows - span >= maxRowNunber) {
            for (int j = span; j < rows; j++) {
                if (calcHistRows[j] == 0D) {
                    int firstX = pointMap.get("third");
                    if (firstX != -1) { // 第一个点出现过
                        int diff = j - span;
                        if (diff < maxRowNunber) { // 小于最小行数，用第二个0点替换第一个0点
                            pointMap.put("third", j);
                            span = j;
                            continue; // 现在在处理第一个点，可以直接下一次循环
                        } else {
                            pointMap.put("thirdEnd", j);
//                            span = j;
                            break;
                        }
                    } else { // 第一个点还没有出现
                        pointMap.put("third", j);
                        span = j;
                        continue;
                    }



                }

//                index++;
            }
        }


        Integer y1 = pointMap.get("first");
        Integer h1 = pointMap.get("firstEnd");
        Rect rowRect = new Rect(0, y1, cols, h1 - y1);
        Mat fmat = new Mat(binary, rowRect);
        matList.add(fmat);
        Imgcodecs.imwrite("/tmp/hist_1.png", fmat);


        Integer y2 = pointMap.get("second");
        Integer h2 = pointMap.get("secondEnd");
        if (y2 != null && y2 != -1 && h2 != null) {
            Rect rowRect2 = new Rect(0, y2, cols, h2 - y2 + 7);
            Mat smat = new Mat(binary, rowRect2);
            matList.add(smat);
            Imgcodecs.imwrite("/tmp/hist_2.png", smat);
        }


        Integer y3 = pointMap.get("third");
        Integer h3 = pointMap.get("thirdEnd");
        if (y3 != null && y3 != -1 && (h3 != null || (h3 == null && (rows - y3) >=maxRowNunber))) {
            Rect rowRect3 = new Rect(0, y3, cols, (h3 == null ? rows : h3) - y3);
            Mat smat = new Mat(binary, rowRect3);
            matList.add(smat);
            Imgcodecs.imwrite("/tmp/hist_3.png", smat);
        }

        return matList;
    }

    /**
     * 竖直分割列，也就是将行按列分割成字符
     * @param colHists 列统计的直方图
     * @param startIndex 下一次递归开始的索引
     * @param maxReursion 最大递归数
     * @param rowOrColLimit 行数或者列数
     * @param minRowOrColNunber 每一行或者列的最小值
     * @param pointMap 要返回的点
     * @return 切分后的字符
     */
    public static void sliceCols(double[] colHists, int startIndex, int maxReursion, int rowOrColLimit, int minRowOrColNunber, Map<String, Integer> pointMap) {
//        List<Mat> resultMats = new ArrayList<>();

        int span = 0;
        for (int j = startIndex; j < rowOrColLimit; j++) {
            if (colHists[j] == 0D) {
                String start = getKey(maxReursion, true);
                Integer firstX = pointMap.get(start);
                if (firstX != null) { // 第一个点出现过
                    int diff = j - span;
                    if (diff < minRowOrColNunber) { // 小于最小行数，用第二个0点替换第一个0点
                        pointMap.put(start, j);
                        span = j;
                        continue; // 现在在处理第一个点，可以直接下一次循环
                    } else {
                        pointMap.put(getKey(maxReursion, false), j);
//                        span = j;
                        // 迭代一次，将递归次数减1
                        if (maxReursion > 1) {
                            sliceCols(colHists, j, --maxReursion, rowOrColLimit, minRowOrColNunber, pointMap);
                        }
                        break;
                    }
                } else { // 第一个点还没有出现
                    pointMap.put(start, j);
                    span = j;
                    continue;
                }
            }
        }
//        return resultMats;
    }

    /**
     * 组和map中的key，懒得去排序了，最大99个字符
     * @param index 序号
     * @param start 是否是开始
     * @return key string
     */
    private static String getKey(int index, boolean start) {
        index = 12 - index;
        if (start) {
            if (index < 10) {
                return "fa0" + index;
            } else {
                return "fa" + index;
            }
        } else {
            if (index < 10) {
                return "fe0" + index;
            } else {
                return "fe" + index;
            }
        }
    }

    public static Map<Integer, List<Mat>> splitChar(List<Mat> rowMatList) {
        if (rowMatList == null || rowMatList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<Mat>> charListMap = new HashMap<>();
        int j = 1;
        for (Mat row : rowMatList) {
            double[] colHists = projection(row, true);
            int colLimit = row.cols();
            // 可能的列数，但是对于数字1，是一个问题。这个数字可以设置一个很小的经验值，比如10（这样会保留噪音），但是，可以对这个切割下来的字符
            // 再做一次水平方向的投影，一般可以去掉这个噪音
            int colNumber = 10; // colLimit / 14 - 1;
            int theoryMaxCol = colLimit / 14;
            int theoryMinCol = colLimit / 26;
            SortedMap<String, Integer> pointMap = new TreeMap<>();

            sliceCols(colHists, 0, 11, colLimit, colNumber, pointMap);
//            sliceColCharList.add(pointMap);


            int height = row.height();

            List<Mat> charMatList = new ArrayList<>();
            int len = pointMap.size();
            int i = len / 2 + 1; // 只需要循环一半
            for (SortedMap.Entry<String, Integer> entry : pointMap.entrySet()) {
                if (i > 0) {
                    String key = entry.getKey();
                    Integer x2 = getPos(key, pointMap);
                    if (x2 == null) {
                        continue;
                    }
                    Integer x = entry.getValue();
                    int width = x2 - x;
                    if (theoryMinCol < width && width <= theoryMaxCol) {
                        width = theoryMaxCol;
                    }
                    Rect rect = new Rect(x, 0, width, height);
                    Mat cell = new Mat(row, rect);
                    charMatList.add(cell);
                } else {
                    break;
                }
                i--;
            }
            charListMap.put(j++, charMatList);
        }
//        LOGGER.info("切分后的字符={}", sliceColCharList);

//        for (Map<String, Integer> map : sliceColCharList) { // 处理每一行字符
//
//        }

        return charListMap;
    }

    private static Integer getPos(String x, Map<String, Integer> pointMap) {
        String key = x.replace('a', 'e');
        return pointMap.get(key);
    }

    /**
     * 根据身份证去校正地址
     * @param code 身份证
     * @param address 地址
     */
    public static String postProcess(String code, String address) {
        if (StringUtils.isBlank(code) || StringUtils.isBlank(address)) {
            return address; // 不做处理，直接返回原来的地址
        }
        if (code.length() < 6 || address.length() < 9) {
            return address;
        }

        String targetAddress = "";
        int type = 0;

        String province = code.substring(0, 2);
        String provinceName = ADDRESS.get(province + "0000");
        if (StringUtils.isBlank(provinceName)) {
            return address;
        } else {
            targetAddress += provinceName;
            type = 1;
        }
        String actualProvince = address.substring(0, 2);

        Integer exist = PROVINCE.get(actualProvince);
        if (exist != null && exist == 1) {
            String pn = provinceName.substring(0, 2);
            if (!pn.equals(actualProvince)) { // 身份证号码的省份和实际不符合，是迁户口的，按实际的来
                String result = actualProvince + "省" + address.substring(3); // 也可能不带省字，这个可以根据34个省挨个判断
                LOGGER.info("校验后地址：{}", result);
                return result;
            }
        }

        String city = code.substring(0, 4);
        String cityName = ADDRESS.get(city + "00");

        if (StringUtils.isBlank(cityName)) { // 可能为空，中山市

        } else {
            targetAddress += cityName;
            type = 2;
        }

        String county = code.substring(0, 6);
        String countyName = ADDRESS.get(county);
        if (StringUtils.isBlank(countyName)) {
            // 比较省市

        } else {
            if (countyName.endsWith("市") && cityName.endsWith("市")) { // 县级市，地址中一般不会再包含地级代管市
                targetAddress = provinceName + countyName;
                type = 4;
            } else {
                targetAddress += countyName;
                type = 3;
            }
        }


        int len = targetAddress.length();
        String uncheckedAddress = address.substring(0, len); // 直接按查找到的进行校验


//        if (type == 1) {
//            uncheckedAddress = address.substring(0, 3);
//        } else if (type == 2) {
//            uncheckedAddress = address.substring(0, 6);
//        } else if (type == 3) {
//            uncheckedAddress = address.substring(0, 9);
//        } else if (type == 4) {
//            uncheckedAddress = address.substring(0, 6);
//        } else {
//            return address; // 不会出现 never occur
//        }


        double errorRate = similar(uncheckedAddress, targetAddress, len);
        System.out.println(errorRate);

        if (errorRate < 0.5) {
            String suffix = address.substring(len);
//            if (type == 1) {
//                suffix = address.substring(3);
//            } else if (type == 2 || type == 4) {
//                suffix = address.substring(6);
//            } else if (type == 3) {
//                suffix = address.substring(9);
//            }
            String checkedAddress = targetAddress + suffix;
            LOGGER.info("校验后地址1：{}", checkedAddress);
            return checkedAddress;
        } else if (errorRate > 0.6) {
            // 把市去掉，再次尝试一下，因为有的地址，市是省略的
            targetAddress = provinceName + countyName;
            len = targetAddress.length();
            uncheckedAddress = address.substring(0, len);
            errorRate = similar(uncheckedAddress, targetAddress, len);
            if (errorRate <= 0.34) {
                String suffix = address.substring(len);
                String checkedAddress = targetAddress + suffix;
                LOGGER.info("校验后地址2：{}", checkedAddress);
                return checkedAddress;
            } else {
                return address;
            }
        } else {
            return address; // 相似度底，还是返回原来的地址
        }

    }

    public static double similar(String source, String target, int len) {
        int similarDegree = SimilarTest.editDistance(source, target);
        double errorRate = similarDegree * 1.0 / len; // 根据字符长度来判断错误率
        return errorRate;
    }

    public static String textRecognize(Mat image, int type) {
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
            return result;
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String codeRecognize(Mat image) {
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
                return null;
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
            return result; // 很多都应该返回的，先把身份证返回吧
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return null;
    }
}
