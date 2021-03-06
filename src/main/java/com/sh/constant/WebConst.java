package com.sh.constant;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Donghua.Chen on 2018/4/29.
 */
@Component
public class WebConst {

    /**
     * 一些网站配置
     */
    public static Map<String, String> initConfig = new HashMap<>();

    /**
     * session的key
     */
    public static String LOGIN_SESSION_KEY = "login_user";

    public static final String USER_IN_COOKIE = "S_L_ID";

    /**
     * aes加密加盐
     */
    public static String AES_SALT = "0123456789abcdef";

    /**
     * 最大获取文章条数
     */
    public static final int MAX_POSTS = 9999;

    /**
     * 最大页码
     */
    public static final int MAX_PAGE = 100;

    /**
     * 文章最多可以输入的文字数
     */
    public static final int MAX_TEXT_COUNT = 200000;

    /**
     * 文章标题最多可以输入的文字个数
     */
    public static final int MAX_TITLE_COUNT = 200;

    /**
     * 点击次数超过多少更新到数据库
     */
    public static final int HIT_EXCEED = 10;

    /**
     * 上传文件最大5M
     */
    public static Integer MAX_FILE_SIZE = 1024*1024*5;

    //服务器真实地址(linux根目录需要以/开头)
    public static String FILE_REAL_PATH = "/www/server/apache-tomcat-8.5.32/webapps/jxmstory/uploadfile/";
//    public static String FILE_REAL_PATH = "D:/apache-tomcat-7.0.65/webapps/jxmstory/uploadfile/";

    //URL访问地址
    public static String FILE_DIRECTORY = "http://47.95.225.29/jxmstory/uploadfile/";
//    public static String FILE_DIRECTORY = "http://192.168.0.11/jxmstory/uploadfile/";


}
