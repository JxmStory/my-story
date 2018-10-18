package cn.luischen.utils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Date: 2018-10-16 16:19
 * @Auther: 季小沫的故事
 */
@Controller
public class HttpUploadFile {

    private static final String FAR_SERVICE_DIR = "http://192.168.30.39:8080/receive";//远程服务器接受文件的路由
    private static final long yourMaxRequestSize = 10000000;

    public static String upload(MultipartFile[] files, HttpServletRequest request) throws Exception {
        // 判断enctype属性是否为multipart/form-data
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart)
            throw new IllegalArgumentException(
                    "上传内容不是有效的multipart/form-data类型.");

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // 设置上传内容的大小限制（单位：字节）
        upload.setSizeMax(yourMaxRequestSize);

        // Parse the request
        List<?> items = upload.parseRequest(request);

        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {

            } else {
                // 如果是文件字段
                String fieldName = item.getFieldName();
                String fileName = item.getName();
                String contentType = item.getContentType();
                boolean isInMemory = item.isInMemory();
                long sizeInBytes = item.getSize();
                // ...

                //上传到远程服务器
                InputStream uploadedStream = item.getInputStream();
                HashMap<String, InputStream> files = new HashMap<String, InputStream>();
                files.put(fileName, uploadedStream);
                uploadToFarService(files);
                uploadedStream.close();
            }
        }
        return "redirect:/";
    }

    public void uploadToFarService(HashMap<String, InputStream> files) {
        try {
            String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
            URL url = new URL(FAR_SERVICE_DIR);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);

            OutputStream out = new DataOutputStream(conn.getOutputStream());
            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
            Iterator iter = files.entrySet().iterator();
            int i=0;
            while (iter.hasNext()) {
                i++;
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                InputStream val = (InputStream) entry.getValue();
                String fname = key;
                File file = new File(fname);
                StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data;name=\"file" + i
                        + "\";filename=\"" + key + "\"\r\n");
                sb.append("Content-Type:application/octet-stream\r\n\r\n");

                byte[] data = sb.toString().getBytes();
                out.write(data);
                DataInputStream in = new DataInputStream(val);
                int bytes = 0;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    out.write(bufferOut, 0, bytes);
                }
                out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
                in.close();
            }
            out.write(end_data);
            out.flush();
            out.close();

            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (Exception e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
    }

}
