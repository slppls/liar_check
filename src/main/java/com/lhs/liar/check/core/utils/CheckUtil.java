package com.lhs.liar.check.core.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lhs.liar.check.core.po.AccountPo;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author seanlau
 */
@Component
public class CheckUtil {

    /**
     * 用于检查账户是否为标记账户的第三方网址
     */
    private static final String REQ_URL = "https://taotaohuitong.com/wx/wxorqq/check";

    /**
     * 构建base request
     * @param url 需要请求的url
     * @param requestParams 非公共请求参数
     * @param headers 请求头(包含公共参数)
     * @return http响应对象
     */
    public static HttpResponse sendPostRequest(String url, Map<String,Object> requestParams, Map<String,String> headers){
        headers.put("Content-Type","application/json");
        return HttpUtil.createPost(url).body(JSON.toJSONString(requestParams)).addHeaders(headers).execute();
    }

    /**
     * 构建post request
     * @param account 需要校验的账户
     * @param token 登陆后取到的token
     * @return
     */
    public static JSONObject sendCheckRequest(String account,String token){
        //构建公共参数请求头 目前对于登陆处理，暂时为了方便，从网页登陆一次后拿到token直接传入
        //后续可以通过模拟登陆获取到token用于程序自动化处理逻辑
        Map<String,String> headers = new HashMap<>(2);
        headers.put("token",token);

        HashMap<String,Object> requestParams = new HashMap<>(2);
        requestParams.put("wxorqq",account);

        HttpResponse response = sendPostRequest(REQ_URL,requestParams,headers);
        JSONObject result = null;
        if(null != response){
            String bodyStr = response.body();
            result = JSON.parseObject(bodyStr);
            System.out.println("请求查询成功:查询账户名:"+account+"----- api输出结果:"+result.toJSONString());
        }else {
            System.out.println("网络请求异常!");
        }
        return result;
    }

    public static List<String> readerExcel(InputStream excelIst) {
        ExcelReader reader = ExcelUtil.getReader(excelIst);
        List<AccountPo> accountPos = reader.readAll(AccountPo.class);
        List<String> accounts = new ArrayList<>(500);
        for (AccountPo accountPo : accountPos) {
            if(null != accountPo.getUser_name()){
                accounts.add(accountPo.getUser_name());
            }
            if(null != accountPo.getWxid()){
                accounts.add(accountPo.getWxid());
            }
        }
        System.out.println("excle解析出来的数据:"+JSON.toJSONString(accountPos));
        return accounts;
    }

    public static HttpServletResponse writerExcel(List<String> rows, HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(rows, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=filter.xlsx");

        ServletOutputStream out=response.getOutputStream();

        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);

        return response;
    }

    public static HttpServletResponse writerJSON(JSONArray rows, HttpServletResponse response) throws IOException {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition","attachment;filename=filter.json");
        response.setCharacterEncoding("UTF-8");

        ServletOutputStream out=response.getOutputStream();
        out.write(rows.toJSONString().getBytes("UTF-8"));
        out.flush();
        IoUtil.close(out);

        return response;
    }

    public static void inputStreamToFile(InputStream ins,File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
