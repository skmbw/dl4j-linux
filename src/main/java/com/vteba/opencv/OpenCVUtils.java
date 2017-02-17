package com.vteba.opencv;

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
import java.util.HashMap;
import java.util.Map;

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

        Map<String, Mat> map = getCardSlice("/home/yinlei/idcard3.jpg");

        for (Map.Entry<String, Mat> entry : map.entrySet()) {
            Imgcodecs.imwrite("/tmp/" + entry.getKey() + "3.png", entry.getValue());
        }
    }
}
