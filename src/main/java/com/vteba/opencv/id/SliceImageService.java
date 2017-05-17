package com.vteba.opencv.id;

import com.vteba.opencv.FindContoursTest;
import com.vteba.opencv.OpenCVUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinlei on 17-5-9.
 */
public class SliceImageService {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static volatile AtomicInteger integer = new AtomicInteger(1);

    public static void main(String[] args) {
        Mat imageMat = Imgcodecs.imread("/home/yinlei/sfz_test/lin/bi.png", 0);

        imageMat = OpenCVUtils.resize(imageMat, 1000D); // 要统一归一化，否则，后面不好处理

        Map<String, Mat> map = FindContoursTest.getCardSlice2(imageMat);

        Mat addressMat = map.get("name");
        Imgcodecs.imwrite("/tmp/" + 2345 + ".png", addressMat);

        Map<Integer, List<Mat>> charListMap = OpenCVUtils.splitAddressChar2(addressMat);

        boolean isName = true;

        int row = charListMap.size();
        for (int i = 1; i <= row; i++) {
            List<Mat> charList = charListMap.get(i);
            int j = 1;
            for (Mat charMat : charList) {
                int width = charMat.cols();
                int height = charMat.rows();
                if (isName) {
                    Imgcodecs.imwrite("/tmp/test/21" + integer.getAndIncrement() + ".png", charMat);
                } else {
                    if (width > 40) {
                        List<Mat> resultMatList = ResizeTestImage.removeBoundary(charMat);
                        int k = 0;
                        for (Mat mat : resultMatList) {
                            int h = mat.rows();
                            int w = mat.cols();
                            if (h != 1 && w != 1) {
                                Imgcodecs.imwrite("/tmp/test/15" + integer.getAndIncrement() + ".png", mat);
                            } else {
                                System.out.println("qu宽度为1.");
                            }
                        }
                    } else {
                        if (height > 40) {
                            Mat topBottom = ResizeTestImage.removeTopBottom(charMat);
                            if (topBottom == null) {
                                continue;
                            }
                            int height2 = topBottom.height();
                            int width2 = topBottom.width();
                            if (width2 != 1 && height2 != 1) {
                                Imgcodecs.imwrite("/tmp/test/15" + integer.getAndIncrement() + ".png", topBottom);
                            } else {
                                System.out.println("宽度为1.");
                            }
                        } else {
                            if (height != 1 && width != 1) {
                                Imgcodecs.imwrite("/tmp/test/15" + integer.getAndIncrement() + ".png", charMat);
                            } else {
                                System.out.println("天生宽度为1.");
                            }

                        }
                    }
                }


            }
        }
    }


}
