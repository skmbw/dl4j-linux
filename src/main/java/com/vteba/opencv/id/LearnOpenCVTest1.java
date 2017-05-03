package com.vteba.opencv.id;

import com.vteba.opencv.FindContoursTest;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import sun.nio.cs.ext.MacThai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * Created by yinlei on 17-1-12.
 */
public class LearnOpenCVTest1 {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        Mat img1 = Imgcodecs.imread("/home/yinlei/timg48.jpg");
        Mat dst = new Mat();
        Size size = new Size(7, 7); // size 很大的情况下，就看不清楚了（加一个遮罩层，看不清图片）
        Imgproc.blur(img1, dst, size); // 平滑，降噪

        boolean r = Imgcodecs.imwrite("/home/yinlei/idimage/yinblur2.png", dst);
        System.out.println(r);

        Mat gray = new Mat();
        Imgproc.cvtColor(img1, gray, Imgproc.COLOR_RGB2GRAY); // 灰度图

        boolean r2 = Imgcodecs.imwrite("/home/yinlei/idimage/yingray.png", gray);
        System.out.println(r2);

        Mat border = doCanny(img1);

        boolean r3 = Imgcodecs.imwrite("/home/yinlei/idimage/yinborder.png", border);
        System.out.println(r3);

        Mat background = doBackgroundRemoval(img1);
        boolean r4 = Imgcodecs.imwrite("/home/yinlei/idimage/yinbg.png", background);
        System.out.println(r4);

        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(gray, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, 10);

        boolean r5 = Imgcodecs.imwrite("/home/yinlei/idimage/yinbinary.png", binary);
        System.out.println(r5);


        Map<String, Mat> map = FindContoursTest.getCardSlice2(binary);
        Mat addressMat = map.get("address");

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));

        Mat addressDst = new Mat();
        Imgproc.morphologyEx(addressMat, addressDst, Imgproc.MORPH_CLOSE, kernel);

        boolean r6 = Imgcodecs.imwrite("/home/yinlei/idimage/yinmorph.png", addressDst);
        System.out.println(r6);

        Mat horizonProject = new Mat();

        /**
         * 水平方向和垂直方向投影
         *
         * source 源图
         * dst 投影后的图
         * dim: 0 水平，1竖直
         * rtype 取值方法
         * dtype 负数和源图类型一样，否则是CV_MAKE_TYPE(cv_mat_depth(dtype), source.channels())
         */
        Core.reduce(addressDst, horizonProject, 0, Core.REDUCE_MAX, -1);

        Imgcodecs.imwrite("/home/yinlei/idimage/yinhorizonProject.png", horizonProject);
    }

    /**
     * Apply Canny。边缘检测.
     *
     * @param frame
     *            the current frame
     * @return an image elaborated with Canny
     */
    private static Mat doCanny(Mat frame) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, 10, 10 * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);

        return dest;
    }

    /**
     * 简单的移除背景
     * @param frame
     * @return
     */
    private static Mat doBackgroundRemoval(Mat frame) {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<Mat>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image

        Scalar average= Core.mean(hsvPlanes.get(0));
        double threshValue =average.val[0];
        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        thresholdImg.convertTo(thresholdImg, CvType.CV_8U);
        frame.copyTo(foreground, thresholdImg);//掩膜图像复制

        return foreground;
    }

//    Mat img=读入图像
//    Rect Roi=new Rect(new Point(5,5),new Size(400,800));
//
//    Mat image= img.submat(Roi);//子图
//
//    String xmlfilePath = "haarcascade_frontalface_alt2.xml";
//    MatOfRect faceDetections = new MatOfRect();
//    CascadeClassifier faceDetector = new CascadeClassifier(xmlfilePath);
//faceDetector.detectMultiScale(image , faceDetections, 1.1,2,0|Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size( ), new Size());
//faceCascade.detectMultiScale(image, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(), new Size());
//
//    image Matrix of the type CV_8U containing an image where objects are detected.
//    objects Vector of rectangles where each rectangle contains the detected object.包含人脸的矩形vector
//    scaleFactor Parameter specifying how much the image size is reduced at each image scale.看不懂默认1.1
//    minNeighbors Parameter specifying how many neighbors each candidate rectangle should have to retain it.
//    flags Parameter with the same meaning for an old cascade as in the function cvHaarDetectObjects. It is not used for a new cascade.//Objdetect.CASCADE_FIND_BIGGEST_OBJECT  找最大的人脸目标，一次只获得一个人脸
//    minSize Minimum possible object size. Objects smaller than that are ignored.//脸部区域最小下限
//    maxSize Maximum possible object size. Objects larger than that are ignored.//脸部区域最大上限

    // 以下是人脸检测小函数
    private static Mat detectAndDisplay(Mat frame) {
        Mat grayFrame = new Mat();
        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        //  equalize the frame histogram to  improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);
        //  compute minimum face size (20%  of the frame height, in our case)
        int absoluteFaceSize = Math.round(frame.height() * 0.2f);
        //  detect faces
        String xmlfilePath = "./data/haarcascades/haarcascade_frontalface_alt2.xml";
        MatOfRect faces = new MatOfRect();
        CascadeClassifier faceDetector = new CascadeClassifier(xmlfilePath);
        faceDetector.detectMultiScale(grayFrame,  faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        //  each rectangle in faces is a  face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
        {
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }
        return frame;
    }
}
