package com.vteba.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Created by yinlei on 17-1-13.
 */
public class SliceTest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        Mat img = Imgcodecs.imread("/home/yinlei/idcard/s4.jpg");
//        Mat rotated = new Mat();
//        img.copyTo(rotated);
//        Mat dst = new Mat();
//        Size size = new Size(1450, 930);
//        Imgproc.resize(img, dst, size);
//
//        Imgcodecs.imwrite("/home/yinlei/obama.png", dst);


//        Mat rotImg = rotateImage1(rotated, -9);
//        Imgcodecs.imwrite("/home/yinlei/rote.png", rotImg);

        Rect rect = new Rect(14, 562, 482, 310);

        Mat sliceMat = new Mat(img, rect);

        Imgcodecs.imwrite("/home/yinlei/idcard/s4.png", sliceMat);
    }

//    public static Mat guiyihuaMatByRoi(Mat cpsrcMat, RotatedRect rotatedRect) {
//            /*Point[] rectPoints=new Point[4];
//            rotatedRect.points(rectPoints);//存储矩形4点坐标
//            for(int i=0;i<rectPoints.length;i++){
//                Imgproc.line(cpsrcMat,rectPoints[i],rectPoints[(i+1)%rectPoints.length],new Scalar(255, 0, 0),2);
//            }*/
//        Mat rotated;
//        //角度
//        double angle = 0;
//        if (rotatedRect.size.width < rotatedRect.size.height) {
//            angle = rotatedRect.angle + 180;
//        } else {
//            angle = rotatedRect.angle + 90;
//        }
//        angle = angle - 90;
//        //缩放比例
//        double rad = 1;
//        double maxrectbian = rotatedRect.size.height > rotatedRect.size.width ? rotatedRect.size.height : rotatedRect.size.width;
//        double minrectbian = rotatedRect.size.height < rotatedRect.size.width ? rotatedRect.size.height : rotatedRect.size.width;
//        double radw = cpsrcMat.width() / maxrectbian;
//        double radh = cpsrcMat.height() / minrectbian;
//        rad = radw < radh ? radw : radh;
//        rad *= 0.95;
//        //执行旋转
//        rotated = rotateImage1(cpsrcMat, angle);
//        //Mat M = Imgproc.getRotationMatrix2D(rotatedRect.center, angle, rad);
//        //Imgproc.warpAffine(cpsrcMat, rotated, M, new Size(cpsrcMat.width(),cpsrcMat.height()), Imgproc.INTER_CUBIC,0,new Scalar(255,255,255));
//        //裁剪
//        int pading = 5;
//        double angleHUD = angle * Math.PI / 180.; // 弧度
//        double sin = Math.abs(Math.sin(angleHUD)), cos = Math.abs(Math.cos(angleHUD)), tan = Math.abs(Math.tan(angleHUD));
//        double oldx = rotatedRect.center.x, oldy = rotatedRect.center.y;
//        double newpX = 0;
//        double newpY = 0;
//        if (angle < 0) {
//            newpX = cpsrcMat.height() * sin + oldx * cos - oldy * sin;//新坐标系下rect中心坐标
//            newpY = oldy / cos + (oldx - oldy * tan) * sin;//新坐标系下rect中心坐标
//        } else if (angle >= 0) {
//            newpX = oldx * cos + oldy * sin;
//            newpY = oldy / cos + (cpsrcMat.width() - (oldx + oldy * tan)) * sin;
//        }
//        //Imgproc.circle(rotated,new Point(newpX,newpY),5,new Scalar(255,0,0),2);
//
//        int startrow = (int) (newpY - minrectbian / 2) - pading;
//        if (startrow < 0) startrow = 0;
//
//        int endrow = (int) (newpY + minrectbian / 2) + pading;
//        if (endrow >= rotated.height()) endrow = rotated.height();
//
//        int startcls = (int) (newpX - maxrectbian / 2) - pading;
//        if (startcls < 0) startcls = 0;
//
//        int endcls = (int) (newpX + maxrectbian / 2) + pading;
//        if (endcls >= rotated.width()) endcls = rotated.width();
//        rotated = rotated.submat(startrow, endrow, startcls, endcls);
//        //Imgproc.circle(rotated,new Point(startcls,startrow),5,new Scalar(0,255,0),2);
//        //Imgproc.circle(rotated,new Point(endcls,startrow),5,new Scalar(0,255,0),2);
//        //Imgproc.circle(rotated,new Point(startcls,endrow),5,new Scalar(0,255,0),2);
//        //Imgproc.circle(rotated,new Point(endcls,endrow),5,new Scalar(0,255,0),2);
//        return rotated;
//    }
//
//    //旋转图像内容不变，尺寸相应变大
//    public static Mat rotateImage1(Mat img, double degree) {
//        double angle = degree * Math.PI / 180.; // 弧度
//        double a = Math.sin(angle), b = Math.cos(angle);
//        int width = img.width();
//        int height = img.height();
//        int width_rotate = (int) (height * Math.abs(a) + width * Math.abs(b));
//        int height_rotate = (int) (width * Math.abs(a) + height * Math.abs(b));
//        //旋转数组map
//        // [ m0  m1  m2 ] ===>  [ A11  A12   b1 ]
//        // [ m3  m4  m5 ] ===>  [ A21  A22   b2 ]
//        Mat map_matrix = new Mat(2, 3, CvType.CV_32F);
//        // 旋转中心
//        Point center = new Point(width / 2, height / 2);
//        map_matrix = Imgproc.getRotationMatrix2D(center, degree, 1.0);
//        map_matrix.put(0, 2, map_matrix.get(0, 2)[0] + (width_rotate - width) / 2);
//        map_matrix.put(1, 2, map_matrix.get(1, 2)[0] + (height_rotate - height) / 2);
//        Mat rotated = new Mat();
//        Imgproc.warpAffine(img, rotated, map_matrix, new Size(width_rotate, height_rotate), Imgproc.INTER_LINEAR | Imgproc.WARP_FILL_OUTLIERS, 0, new Scalar(255, 255, 255));
//        return rotated;
//    }
}
