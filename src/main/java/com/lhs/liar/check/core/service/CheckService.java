package com.lhs.liar.check.core.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lhs.liar.check.core.utils.CheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckService {

    private static final Integer FOX_NUM = 1;


    public List<String> findLiarAccount(List<String> accounts , String token){

        //被标记的账户列表
        List<String> foxAccounts = new ArrayList<>(100);
        //api接口目前只能支持单一账户查询,所以这边进行从excel上解析下来的所有账号进行遍历
        //注意这边可能会导入较大数量的数据,需要优化一次,如果对方api接口不限流,可以用多线程轮询等方式处理(需要将excl解析出来的数据进行分割)
        for (String account : accounts) {
            JSONObject result = CheckUtil.sendCheckRequest(account,token);
            String code = result.getString("code");
            if("0".equals(code)){
                //处理成功状态
                JSONObject data = result.getJSONObject("data");
                Integer foxNum = data.getInteger("fox");
                if(foxNum >= FOX_NUM){
                    //标记次数大于等于1时，输出该账户
                    foxAccounts.add(account);
                }
            }else{
                //请求失败了
                System.out.println("请求失败,返回:"+result.toJSONString());
            }
        }

        System.out.println("被标记次数大于等于1的账户:"+ JSON.toJSONString(foxAccounts));
        return foxAccounts;
    }

    public JSONArray findLiarAccountByJSONArray(JSONArray accounts, String token){

        //被标记的账户列表
        JSONArray foxAccounts = new JSONArray(100);
        //api接口目前只能支持单一账户查询,所以这边进行从excel上解析下来的所有账号进行遍历
        //注意这边可能会导入较大数量的数据,需要优化一次,如果对方api接口不限流,可以用多线程轮询等方式处理(需要将excl解析出来的数据进行分割)

        for (int i = 0; i < accounts.size(); i++) {
            JSONObject data = accounts.getJSONObject(i);
            String usrName = data.getString("user_name");
            String wxId = data.getString("wxid");
            boolean flag = false;
            if(StrUtil.isNotBlank(usrName)){
                flag = needAdd(usrName,token);
            }
            if(StrUtil.isNotBlank(wxId)){
                if(!flag){
                    flag = needAdd(wxId,token);
                }
            }
            if(flag){
                foxAccounts.add(data);
            }
        }
        return foxAccounts;
    }


    private boolean needAdd(String account,String token){
        JSONObject result = CheckUtil.sendCheckRequest(account,token);
        String code = result.getString("code");
        if("0".equals(code)){
            //处理成功状态
            JSONObject data = result.getJSONObject("data");
            Integer foxNum = data.getInteger("fox");
            if(foxNum >= FOX_NUM){
                //标记次数大于等于1时，输出该账户
                return true;
            }
        }else{
            //请求失败了
            System.out.println("请求失败,返回:"+result.toJSONString());
        }
        return false;
    }

}
