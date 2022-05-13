package com.clearlove3.idserver.controller;

import com.clearlove3.idserver.domain.Msg;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 图片服务器的Java代码
 * 使用Tomcat作为服务器，SpringBoot 2.6.6版本作为开发框架，Java 11作为开发语言，Maven作为依赖管理
 * 具体流程：
 *  1.搭建好服务器，监听8080端口的image/upload的网络请求
 *  2.获取请求中附带的图片，将其保存到MODNet工作的目录中的input文件夹下，作为程序的输入图片
 *  3.通过命令行的方式调用抠图模块，执行python代码，通过Java的Runtime类的exec方法执行，
 *      由于抠图代码运行需要时间，设置一个10秒的休眠时间等待代码执行完毕，然后获取生成的图片，
 *      将其转换为比特数组返回给客户端即可
 * @author clearlove3
 * @date 2022/4/14 14:15
 */
@Controller
@RequestMapping(value = "/image",produces = "application/json;charset=UTF-8")
public class ImageController {

    private Msg msg=null;

    /**
     * 用于查询抠图执行结果的Controller
     * 返回自定义的错误信息实体类来向客户端响应抠图结果
     * @return 自定义的错误信息
     */
    @ResponseBody
    @RequestMapping(value = "/query")
    public Msg query(){
        return msg;
    }

    /**
     * 获取请求中的图片并且进行抠图的Controller
     * 使用@ResponseBody注解来让SpringBoot根据@RequestMapping注解中的produces属性
     * 来进行返回值的封装，使其包装为图片文件返回到客户端中
     * @param file 请求中附带的文件
     * @return 抠图结果的二进制数组
     */
    @RequestMapping(value = "/upload",
            produces = {MediaType.IMAGE_JPEG_VALUE,MediaType.IMAGE_PNG_VALUE})
    @ResponseBody
    public byte[] Upload(MultipartFile file) {
        //首先上传文件到MODNet指定的文件目录
        //定义输入输出流作为接下来的使用
        InputStream in=null;
        OutputStream out=null;
        //图片要保存的路径，使用绝对路径进行保存
        String filePath="F:/code/FinalProject/MODNet/input_phone1/1.jpg";
        //使用try catch捕获可能发生的异常
        try {
            //获取上传文件的流数据
            in= file.getInputStream();
            //获取保存路径的流数据
            out=new FileOutputStream(filePath);
            System.out.println("接收到图片,图片保存路径为"+filePath);
            //将文件流复制到本地流
            FileCopyUtils.copy(in,out);
        } catch (IOException e) {
            e.printStackTrace();
            msg=new Msg(2,"文件储存失败!");
        } finally {
            //使用finally来保证流最后会被关闭
            try {
                //刷新输出流，然后关闭输入输出流完成文件的保存
                out.flush();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //接着运行Python代码,生成证件照
        Runtime rt=Runtime.getRuntime();
        try {
            System.out.println("执行抠图程序...");
            //执行本地的cmd代码，运行抠图程序
            Process process=rt.exec("python src/inference.py",null,
                    new File("F:/code/FinalProject/MODNet"));
        } catch (IOException e) {
            e.printStackTrace();
            msg=new Msg(3,"抠图程序启动失败!");
        }
        //最后返回生成的证件照
        try {
            //首先等待一段时间让抠图程序运行，防止过早获取图片获取不到
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //获取生成的图片的绝对路径
        String outPath="F:/code/FinalProject/MODNet/align1/1_fg.jpg";
        //使用BufferedImage类来保存图片
        BufferedImage image= null;
        try {
            //打开完成抠图的图片，同样要使用try catch进行环绕
            image = ImageIO.read(new FileInputStream(outPath));
            System.out.println("抠图完毕,生成证件照路径为"+outPath);
            System.out.println("正在返回生成的证件照...");
        } catch (IOException e) {
            e.printStackTrace();
            msg=new Msg(3,"未检测到人像!");
        }
        //使用比特流来保存图片的流数据
        ByteArrayOutputStream res=new ByteArrayOutputStream();
        try {
            //将图片的流数据写入到数组之中
            ImageIO.write(image,"jpg",res);
            System.out.println("证件照返回完毕!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //返回流的二进制数组，完成图片的返回
        msg=new Msg(1,"成功!");
        return res.toByteArray();
    }
}
