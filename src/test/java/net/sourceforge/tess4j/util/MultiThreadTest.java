package net.sourceforge.tess4j.util;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yinlei on 17-2-8.
 */
public class MultiThreadTest {

    public static void main(String[] args) {
        ITesseract instance = new Tesseract1(); // 这个是基于类实现的
        instance.setDatapath("/usr/local/tesseract-3.04.01/tessdata/");
        instance.setLanguage("shz4");

        Task task1 = new Task(instance, 1);

        // jna instance is not thread safety
        ITesseract instance2 = new Tesseract(); // 这个是基于接口实现的
        instance2.setDatapath("/usr/local/tesseract-3.04.01/tessdata/");
        instance2.setLanguage("shz4");

        // jna的两种方式都不是线程安全的，每次都要new一个实例

        Task task2 = new Task(instance2, 2);

        ITesseract instance3 = new Tesseract(); // 这个是基于接口实现的
        instance3.setDatapath("/usr/local/tesseract-3.04.01/tessdata/");
        instance3.setLanguage("shz4");
        Task task3 = new Task(instance3, 3);
        Task task4 = new Task(instance, 4);

        ExecutorService service = Executors.newFixedThreadPool(4);
        service.submit(task1);
        service.submit(task2);
        service.submit(task3);
        //service.submit(task4);
    }
}
