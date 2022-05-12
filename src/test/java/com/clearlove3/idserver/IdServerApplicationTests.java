package com.clearlove3.idserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SpringBootTest
class IdServerApplicationTests {

    /**
     * 测试执行cmd代码的方法
     * 测试成功
     */
    @Test
    void contextLoads() {
        Runtime rt=Runtime.getRuntime();
        try {
            Process process=rt.exec("python src/inference.py",null,
                    new File("F:/code/FinalProject/MODNet"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试BufferedImage类的用法
     * 测试成功
     */
    @Test
    void testOut(){
        String outPath="F:/code/FinalProject/MODNet/output/1_fp.png";
        BufferedImage bufferedImage=null;
        File result=new File(outPath);
        System.out.println(result);
        try {
            bufferedImage = (BufferedImage) ImageIO.read(new File(outPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
