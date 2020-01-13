package com.lhs.liar.check.core.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.lhs.liar.check.core.utils.CheckUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

@Controller
public class HomeController {

    @RequestMapping("/index")
    public String index(){
        return "index";
    }
}
