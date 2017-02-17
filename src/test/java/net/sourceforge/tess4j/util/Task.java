package net.sourceforge.tess4j.util;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by yinlei on 17-2-8.
 */
public class Task implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Task.class);
    private ITesseract instance;
    private int index;

    public Task(ITesseract instance, int i) {
        this.instance = instance;
        this.index = i;
    }

    @Override
    public void run() {
        logger.info("doOCR on a id card image [[" + index + "]]");
        File imageFile = new File("/home/yinlei/s5name.png");
        String result = null;
        try {
            result = instance.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        logger.info("image [[" + index + "]] result=[" + result);
    }
}
