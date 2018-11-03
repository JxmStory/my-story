package com.sh.controller.admin;

import com.github.pagehelper.PageInfo;
import com.sh.api.QiniuCloudService;
import com.sh.constant.ErrorConstant;
import com.sh.constant.Types;
import com.sh.constant.WebConst;
import com.sh.dto.AttAchDto;
import com.sh.exception.BusinessException;
import com.sh.model.AttAchDomain;
import com.sh.model.UserDomain;
import com.sh.service.attach.AttAchService;
import com.sh.utils.APIResponse;

import com.sh.utils.Commons;
import com.sh.utils.ImgUtill;
import com.sh.utils.TaleUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

/**
 * 附件控制器
 * Created by Donghua.Chen on 2018/4/30.
 */
@Api("附件相关接口")
@Controller
@RequestMapping("admin/attach")
public class AttAchController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttAchController.class);

    public static final String CLASSPATH = TaleUtils.getUplodFilePath();


    @Autowired
    private AttAchService attAchService;



    @ApiOperation("文件管理首页")
    @GetMapping(value = "")
    public String index(
            @ApiParam(name = "page", value = "页数", required = false)
            @RequestParam(name = "page", required = false, defaultValue = "1")
            int page,
            @ApiParam(name = "limit", value = "条数", required = false)
            @RequestParam(name = "limit", required = false, defaultValue = "12")
            int limit,
            HttpServletRequest request
    ){
        PageInfo<AttAchDto> atts = attAchService.getAtts(page, limit);
        request.setAttribute("attachs", atts);
        request.setAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType(), Commons.site_url()));
        request.setAttribute("max_file_size", WebConst.MAX_FILE_SIZE / 1024);
        return "admin/attach";
    }




    @ApiOperation("markdown文件上传")
    @PostMapping("/uploadfile")
    public void fileUpLoadToTencentCloud(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @ApiParam(name = "editormd-image-file", value = "文件数组", required = true)
                                                @RequestParam(name = "editormd-image-file", required = true)
                                                MultipartFile file){
        //文件上传
        try {
            request.setCharacterEncoding( "utf-8" );
            response.setHeader( "Content-Type" , "text/html" );

            String fileName = TaleUtils.getFileKey(file.getOriginalFilename()).replaceFirst("/","");

            QiniuCloudService.upload(file, fileName);
            AttAchDomain attAch = new AttAchDomain();
            HttpSession session = request.getSession();
            UserDomain sessionUser = (UserDomain) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
            attAch.setAuthorId(sessionUser.getUid());
            attAch.setFtype(TaleUtils.isImage(file.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType());
            attAch.setFname(fileName);
            attAch.setFkey(QiniuCloudService.QINIU_UPLOAD_SITE + fileName);
            attAchService.addAttAch(attAch);
            response.getWriter().write( "{\"success\": 1, \"message\":\"上传成功\",\"url\":\"" + attAch.getFkey() + "\"}" );
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.getWriter().write( "{\"success\":0}" );
            } catch (IOException e1) {
                throw BusinessException.withErrorCode(ErrorConstant.Att.UPLOAD_FILE_FAIL)
                        .withErrorMessageArguments(e.getMessage());
            }
            throw BusinessException.withErrorCode(ErrorConstant.Att.UPLOAD_FILE_FAIL)
                    .withErrorMessageArguments(e.getMessage());
        }
    }

    @ApiOperation("七牛云文件上传")
    @PostMapping(value = "uploadCloud")
    @ResponseBody
    public APIResponse filesUploadToCloud(HttpServletRequest request,
                                          HttpServletResponse response,
                                          @ApiParam(name = "file", value = "文件数组", required = true)
                                          @RequestParam(name = "file", required = true)
                                          MultipartFile[] files){
        //文件上传
        try {
            request.setCharacterEncoding( "utf-8" );
            response.setHeader( "Content-Type" , "text/html" );

            for (MultipartFile file : files) {

                String fileName = TaleUtils.getFileKey(file.getOriginalFilename()).replaceFirst("/","");

                QiniuCloudService.upload(file, fileName);
                AttAchDomain attAch = new AttAchDomain();
                HttpSession session = request.getSession();
                UserDomain sessionUser = (UserDomain) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
                attAch.setAuthorId(sessionUser.getUid());
                attAch.setFtype(TaleUtils.isImage(file.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType());
                attAch.setFname(fileName);
                attAch.setFkey(QiniuCloudService.QINIU_UPLOAD_SITE + fileName);
                attAchService.addAttAch(attAch);
            }
            return APIResponse.success();
        } catch (IOException e) {
            e.printStackTrace();
            throw BusinessException.withErrorCode(ErrorConstant.Att.UPLOAD_FILE_FAIL)
                    .withErrorMessageArguments(e.getMessage());
        }
    }

    @ApiOperation("删除文件记录")
    @PostMapping(value = "/delete")
    @ResponseBody
    public APIResponse deleteFileInfo(
            @ApiParam(name = "id", value = "文件主键", required = true)
            @RequestParam(name = "id", required = true)
            Integer id,
            HttpServletRequest request
    ){
        try {
            AttAchDto attAch = attAchService.getAttAchById(id);
            if (null == attAch)
                throw BusinessException.withErrorCode(ErrorConstant.Att.DELETE_ATT_FAIL +  ": 文件不存在");
            attAchService.deleteAttAch(id);
            return APIResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            throw BusinessException.withErrorCode(e.getMessage());
        }
    }



    /**
     * 上传多张图片
     * @param files
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "upload")
    @ResponseBody
    public APIResponse upload(@RequestParam("file") MultipartFile[] files,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception{

        //文件存在
        if (files != null) {
            //遍历图片
            for (MultipartFile file : files) {
                //定义上传文件路径、文件名、文件格式
                java.util.Date Datenow=new java.util.Date();//获取当前日期
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
                String nowdate = formatter.format(Datenow).substring(0,6); //将日期格式化
                //服务器真实路径
                String realDirectory = WebConst.FILE_REAL_PATH + nowdate;
                //原文件名
                String fileName = file.getOriginalFilename();
                //文件类型
                String fileType="";
                if (fileName.substring(fileName.length()-4).equals("jpeg")) {
                    fileType=".jpeg";
                }else{
                    fileType = fileName.substring(fileName.length()-3);
                }
                //对文件进行重新命名
                String filename = System.currentTimeMillis() + "." + fileType;

                //文件上传
                if(file.getSize()>1024*1024*5){
                    //大于10M 压缩上传
                    //创建临时文件 将原文件file放入临时文件temFile
                    File temFile =  File.createTempFile("temp", "." + fileType);
                    file.transferTo(temFile);

                    String filedir = realDirectory+"/" + filename; // 以系统时间作为上传文件名称，设置上传文件的完整路径
                    ImgUtill.compressImage(temFile.getAbsolutePath(), filedir);

                    temFile.deleteOnExit(); // 令临时文件在JVM关闭的时候自动删除
                    temFile.delete(); // 立刻删除临时文件
                } else {
                    //普通上传
                    File fi = new File(realDirectory);
                    try {
                        if (!fi.isDirectory()) { // 如果文件夹不存在就新建
                            fi.mkdirs();
                        }
                        File fie = new File(realDirectory, filename);
                        file.transferTo(fie);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw BusinessException.withErrorCode(ErrorConstant.Att.UPLOAD_FILE_FAIL)
                                .withErrorMessageArguments(e.getMessage());
                    }
                }

                //保存信息
                AttAchDomain attAch = new AttAchDomain();
                HttpSession session = request.getSession();
                UserDomain sessionUser = (UserDomain) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
                attAch.setAuthorId(sessionUser.getUid());
                System.out.println();
//                    attAch.setFtype(TaleUtils.isImage(file.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType());
                attAch.setFtype(Types.IMAGE.getType());
                attAch.setFname(fileName);
                attAch.setFkey(WebConst.FILE_DIRECTORY + nowdate + "/" +filename);
                attAchService.addAttAch(attAch);

            }
            return APIResponse.success();
        } else {
            return APIResponse.fail("文件不存在");
        }

    }
}
