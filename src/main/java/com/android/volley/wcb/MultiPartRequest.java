package com.android.volley.wcb;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/*
*  * 用途：
 * 各种数据上传到服务器的内容格式：
 * <p/>
 * 文件上传（内容格式）：multipart/form-data
 * String字符串传送（内容格式）：application/x-www-form-urlencoded
 * json传递（内容格式）：application/json
*
*
* */
public class MultiPartRequest<T> extends Request<T> {
    private  static  final  String TAG=MultiPartRequest.class.getSimpleName();
    /**
     * 解析后的实体类
     */
    private final Class<T> clazz;

    private final Response.Listener<T> listener;

    /**
     * 自定义header:
     */
    private Map<String, String> headers;
    private final Gson gson = new Gson();
    /**
     * 字符编码格式
     */
    private static final String PROTOCOL_CHARSET = "utf-8";

    private static final String BOUNDARY = "----------" + System.currentTimeMillis();
    /**
     * Content type for request.
     */
    private static final String PROTOCOL_CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;

    /**
     * 文件列表。参数1是文件名，参数2是文件
     */
    private Map<String, File> fileList;
    /**
     * 多个文件间的间隔
     */
    private static final String FILEINTERVAL = "\r\n";

    public MultiPartRequest(int method, String url,
                            Class<T> clazz,
                            Response.Listener<T> listener, Response.ErrorListener errorListenerr) {
        super(method, url, errorListenerr);
        this.clazz = clazz;
        this.listener = listener;
        headers = new HashMap<>();
        fileList = new HashMap<>();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    "utf-8");
            T t = gson.fromJson(json, clazz);
            return Response.success(t, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T t) {
        listener.onResponse(t);
    }


    /**
     * 重写getHeaders(),添加自定义的header
     *
     * @return
     * @throws AuthFailureError
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    /**
     * 设置请求的标头
     * @param key
     * @param content
     * @return
     */
    public Map<String, String> setHeader(String key, String content) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(content)) {
            headers.put(key, content);
        }
        return headers;
    }

    /**
     * 添加文件名和文件数据
     *
     * @param fileName
     * @param file
     */
    public void addFile(String fileName, File file) {
        if (!TextUtils.isEmpty(fileName) && file != null) {
            fileList.put(fileName, file);
        }
    }


    /**
     * 重写Content-Type:设置为json
     */
    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    /**
     * post参数类型
     */
    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * post参数
     */
    @Override
    public byte[] getPostBody() throws AuthFailureError {

        return getBody();
    }

    /**
     * 将string编码成byte
     *
     * @return
     * @throws AuthFailureError
     */
    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] body;
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            Set<Map.Entry<String, File>> set = fileList.entrySet();
            int i=1;
            for (Map.Entry<String,File> entry : set) {
                //添加文件的头部格式
                writeByte(outputStream, getFileHead( entry.getKey()));
                //添加文件数据
                writeByte(outputStream,fileTranstateToByte(entry.getValue()));
                //添加文件间的间隔
                if (set.size() > 1&&i<set.size()) {
                    i++;
                    Log.i(TAG,"添加文件间隔");
                    writeByte(outputStream, FILEINTERVAL.getBytes(PROTOCOL_CHARSET));
                }
            }
            writeByte(outputStream, getFileFoot());
            outputStream.flush();
            body = outputStream.toByteArray();
            return body == null ? null : body;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * 将file转成byte[]数据
     */
    public byte[] fileTranstateToByte(File file){
        byte[] data=null;
        FileInputStream fileInputStream=null;
        ByteArrayOutputStream outputStream = null;
        try {
            fileInputStream=new FileInputStream(file);
            byte[] buffer=new byte[1024];
            int length=0;
            while ((length=fileInputStream.read(buffer))!=-1){
                outputStream.write(buffer,0,length);
            }
            outputStream.flush();
            data= outputStream.toByteArray();
        }catch (Exception e){
            data=null;
            e.printStackTrace();
        }finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
            } catch (Exception e) {

            }
        }
        return data;
    }
    public void writeByte(ByteArrayOutputStream outputStream, byte[] bytes) {
        if(bytes!=null){
            outputStream.write(bytes, 0, bytes.length);
        }
    }


    /**
     * 获取到文件的head
     *
     * @return
     */
    public byte[] getFileHead(String fileName) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("--");
            buffer.append(BOUNDARY);
            buffer.append("\r\n");
            buffer.append("Content-Disposition: form-data;name=\"media\";filename=\"");
            buffer.append(fileName);
            buffer.append("\"\r\n");
            buffer.append("Content-Type:application/octet-stream\r\n\r\n");
            String s = buffer.toString();
            return s.getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件的foot
     *
     * @return
     */
    public byte[] getFileFoot() {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("\r\n--");
            buffer.append(BOUNDARY);
            buffer.append("--\r\n");
            String s = buffer.toString();
            return s.getBytes("utf-8");
        } catch (Exception e) {
            return null;
        }
    }

}