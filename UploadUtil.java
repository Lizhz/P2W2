package abc.p2w2;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class UploadUtil {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10 * 1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码

    /**
     * android上传文件到服务器
     *
     * @param file       需要上传的文件
     * @param RequestURL  请求的url
     * @return 返回响应的内容
     */
    public static String uploadImage(File file, String RequestURL) {
        String result = "error";
        String BOUNDARY = UUID.randomUUID().toString();//边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";//内容类型
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);//允许输入流
            conn.setDoOutput(true);//允许输出流
            conn.setUseCaches(false);//不允许使用缓存
            conn.setRequestMethod("POST");//请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.connect();

            if (file != null) {
                //当文件不为空，把文件包装并且上传
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuilder sb = new StringBuilder();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /*这里重点注意：
                name里面的值为服务器端需要key,只有这个key才可以得到对应的文件
                filename是文件的名字，包含后缀名的。比如:abc.png */
                /*sb.append("Content-Disposition: form-data; name=\"inputName\"; filename=\"" + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                sb.append("Content-Type: " + getMIMEType(file) + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());*/
                //此写法会导致无法上传 dos.writeBytes(PREFIX + BOUNDARY + LINE_END);
                dos.writeBytes("Content-Disposition: " +
                        "form-data; " + "name=\"image\";filename=\"" + file.getName() + "\"" + LINE_END);
                dos.writeBytes(LINE_END);
                FileInputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                Log.d("hape",file.getAbsolutePath());
                /*
                 *  获取响应码  200=成功
                 *  当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                Log.d("hape",String.valueOf(res));
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    printResponseHeader(conn);

                    String str = conn.getHeaderField("message");
                    Log.d("hape","str = "+str);
                    if (str.contains("?utf-8?")) {
                        try {
                            result = str.substring(10, str.length() - 2);
                            Log.d("hape", "result = " + result);
                            result = new String(Base64.decode(result, Base64.DEFAULT));
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else {
                        result = str;
                    }
                    /*StringBuilder sbs = new StringBuilder();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sbs.append((char) ss);
                    }
                    result = sbs.toString();*/
                    String mediaStorageDirPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) + "/MyCameraApp" ;
                    downLoadImage(new File(mediaStorageDirPath + File.separator + "receive.jpg"),input);
                    Log.d("hape","成功下载图片");
                    return result;
                }
            }
        } catch (IOException e) {
            Log.e("hape","文件没找到。。。");
            e.printStackTrace();
        }
        return result;
    }

    /**
     *   
     *      * 下载图片  
     *      * @param file  文件  
     *      * @param is    从URL取得的输入流  
     *      
     */
    private static void downLoadImage(File file, InputStream is) {
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fs.write(buffer, 0, len);
            }
            fs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void printResponseHeader(HttpURLConnection http) throws UnsupportedEncodingException {
        Map<String, String> header = getHttpResponseHeader(http);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey() + ":" : "";
            Log.d("hape",key + entry.getValue());
        }
    }

    private static Map<String, String> getHttpResponseHeader(
            HttpURLConnection http) throws UnsupportedEncodingException {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null)
                break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }

}

