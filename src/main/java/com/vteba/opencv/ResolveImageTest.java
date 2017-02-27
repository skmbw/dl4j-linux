package com.vteba.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinlei on 17-2-22.
 */
public class ResolveImageTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // 重命名文件
//        File files = new File("/home/yinlei/idcard2/");
//        int ii = 1;
//        for (File file : files.listFiles()) {
//            if (file.isFile() && file.getName().endsWith(".jpg")) {
//                boolean r = file.renameTo(new File("/home/yinlei/idcard2/id" + ii++ + ".jpg")); // 这个rename，如果在相同的目录里，有重名的文件，重名的文件会被覆盖
//                System.out.println(r);
//            }
//        }

        String home = "/home/yinlei/idcard2t/";

        // 尝试以下批量切割
        File files = new File("/home/yinlei/idcard2/");
        int i = 1;
        int j = 1;
        int k = 1;
        int l = 1;
        for (File file : files.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jpg")) {
                try {
                    String name = file.getName();
                    Mat mat = Imgcodecs.imread("/home/yinlei/idcard2/" + name);
                    Map<String, Mat> resultMap = getCardSlice(mat);

                    for (Map.Entry<String, Mat> entry : resultMap.entrySet()) {
                        String key = entry.getKey();
                        Mat value = entry.getValue();
                        if (key.equals("name")) {
                            Imgcodecs.imwrite(home + key + "/" + key + i++ + ".png", value);
                        } else if (key.equals("nation")) {
                            Imgcodecs.imwrite(home + key + "/" + key + j++ + ".png", value);
                        } else if (key.equals("code")) {
                            Imgcodecs.imwrite(home + key + "/" + key + k++ + ".png", value);
                        } else if (key.equals("address")) {
                            Imgcodecs.imwrite(home + key + "/" + key + l++ + ".png", value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


//        try {
//            int seq = 1;
//            String name = "id" + seq + ".";
//            Mat mat = Imgcodecs.imread("/home/yinlei/idcard2/" + name + "jpg");
//            Map<String, Mat> resultMap = getCardSlice(mat);
//
//            for (Map.Entry<String, Mat> entry : resultMap.entrySet()) {
//                String key = entry.getKey();
//                Mat value = entry.getValue();
//                Imgcodecs.imwrite("/home/yinlei/idcard2/" + key + "/" + key + seq + ".png", value);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //guiyi();
    }

    public static void guiyi() {
        Mat mat = Imgcodecs.imread("/home/yinlei/testimg2.jpg");
        Map<String, Mat> resultMap = getCardSlice(mat);

        for (Map.Entry<String, Mat> entry : resultMap.entrySet()) {
            String key = entry.getKey();
            Mat value = entry.getValue();
            Imgcodecs.imwrite("/home/yinlei/idcard2/test/" + key + ".png", value);
        }
    }

    public static Map<String, Mat> getCardSlice(Mat image) {
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
            int addressX = (int) (width * 0.182);
            int addressY = (int) (height * 0.494);
            int addressWidth = (int) (width * 0.44);
            int addressHeight = (int) (height * 0.272);

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
