package com.lhs.liar.check.core.controller;

import com.alibaba.fastjson.JSON;
import com.lhs.liar.check.core.service.CheckService;
import com.lhs.liar.check.core.utils.CheckUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/check")
@RestController
public class CheckController {

    @Autowired
    private CheckService checkService;

    @RequestMapping("/test")
    public String test(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxODY2NjYwMDEwMCIsImlkIjozODgwNywiZXhwIjoxNTc4NDAzNDkwLCJpYXQiOjE1NzgzMTcwOTAsImp0aSI6IjE4OWU3OTIzLTJjYzUtNGM2Ny1iMDBmLTgzMzI4MDM0MDdkNiIsInVzZXJuYW1lIjoiMTg2NjY2MDAxMDAifQ.JLnIUY-N8Z4Sfg9bJKMa7l7Zm-aRHLB34Qk1rB9umfM";
        List<String> accounts = new ArrayList<>(8);
        accounts.add("Tdc-76050278");
        accounts.add("wxid_6583755839312");
        accounts.add("a13403698147");
        accounts.add("o0927o");
        accounts.add("641406463");
        accounts.add("zuoyiwei999");
        checkService.findLiarAccount(accounts,token);
        return "success";
    }

    @RequestMapping("/filter")
    public void test2(@RequestParam("file")MultipartFile file, @RequestParam("token") String token,@RequestParam(value = "type",defaultValue = "1") Integer type,HttpServletResponse httpServletResponse){
        try {
            if(type == 1){
                //json格式文件
                File jsonFile  = null;
                if(file.equals("")||file.getSize()<=0){
                    file = null;
                }else {
                    InputStream ins = file.getInputStream();
                    jsonFile = new File(file.getOriginalFilename());
                    CheckUtil.inputStreamToFile(ins, jsonFile);
                }
                String jsonStr = FileUtils.readFileToString(jsonFile,"UTF-8");
                com.alibaba.fastjson.JSONArray datas = JSON.parseArray(jsonStr);
                com.alibaba.fastjson.JSONArray filterJSON = checkService.findLiarAccountByJSONArray(datas,token);
                httpServletResponse = CheckUtil.writerJSON(filterJSON,httpServletResponse);
                File del = new File(jsonFile.toURI());
                del.delete();
            }

            if(type == 2){
                //excel格式文件
                List<String> accounts =  CheckUtil.readerExcel(file.getInputStream());
                List<String> filterStr =  checkService.findLiarAccount(accounts,token);
                httpServletResponse = CheckUtil.writerExcel(filterStr,httpServletResponse);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
