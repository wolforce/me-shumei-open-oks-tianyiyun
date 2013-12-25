package me.shumei.open.oks.tianyiyun;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;

/**
 * 使签到类继承CommonData，以方便使用一些公共配置信息
 * @author wolforce
 *
 */
public class Signin extends CommonData {
    String resultFlag = "false";
    String resultStr = "未知错误！";
    
    /**
     * <p><b>程序的签到入口</b></p>
     * <p>在签到时，此函数会被《一键签到》调用，调用结束后本函数须返回长度为2的一维String数组。程序根据此数组来判断签到是否成功</p>
     * @param ctx 主程序执行签到的Service的Context，可以用此Context来发送广播
     * @param isAutoSign 当前程序是否处于定时自动签到状态<br />true代表处于定时自动签到，false代表手动打开软件签到<br />一般在定时自动签到状态时，遇到验证码需要自动跳过
     * @param cfg “配置”栏内输入的数据
     * @param user 用户名
     * @param pwd 解密后的明文密码
     * @return 长度为2的一维String数组<br />String[0]的取值范围限定为两个："true"和"false"，前者表示签到成功，后者表示签到失败<br />String[1]表示返回的成功或出错信息
     */
    public String[] start(Context ctx, boolean isAutoSign, String cfg, String user, String pwd) {
        //把主程序的Context传送给验证码操作类，此语句在显示验证码前必须至少调用一次
        CaptchaUtil.context = ctx;
        //标识当前的程序是否处于自动签到状态，只有执行此操作才能在定时自动签到时跳过验证码
        CaptchaUtil.isAutoSign = isAutoSign;
        
        try{
            //存放Cookies的HashMap
            HashMap<String, String> cookies = new HashMap<String, String>();
            //Jsoup的Response
            Response res;
            //Jsoup的Document
            Document doc;
            
            //登录页面的URL
            String loginPageUrl = "http://cloud.189.cn/udb/udb_login.jsp?redirectURL=http://cloud.189.cn/main.action";
            //提交登录信息的URL
            String loginSubmitUrl = "";
            //提交签到信息的URL
            String signSubmitUrl = "http://cloud.189.cn/userSign.action";
            
            //访问登录页面
            res = Jsoup.connect(loginPageUrl).userAgent(UA_CHROME).timeout(TIME_OUT).referrer(loginPageUrl).followRedirects(true).ignoreContentType(true).method(Method.GET).execute();
            //保存Cookies
            cookies.putAll(res.cookies());
            
            HashMap<String, String> postDatas = new HashMap<String, String>();
            postDatas.put("userName", user);
            postDatas.put("password", pwd);
            postDatas.put("cb_SaveName", "on");
            postDatas.put("Readed", "on");
            postDatas.put("ibtn_Login", "");
            postDatas.put("HiddenReg", "http://e.189.cn/registerMobile.do?appKey=cloud");
            
            loginSubmitUrl = res.url().toString();
            //提交登录信息
            res = Jsoup.connect(loginSubmitUrl).data(postDatas).cookies(cookies).userAgent(UA_CHROME).timeout(TIME_OUT).referrer(loginPageUrl).ignoreContentType(true).followRedirects(true).method(Method.POST).execute();
            cookies.putAll(res.cookies());
            
            //提交签到请求
            //{"isSign":false,"netdiskBonus":1,"signTime":"2013-04-18T11:13:45","userId":5620662,"userSignId":4452182} //今日未签到
            //{"isSign":true,"netdiskBonus":1,"signTime":"2013-04-18T11:13:45","userId":5620662,"userSignId":4452182}  //今日已签到
            res = Jsoup.connect(signSubmitUrl).cookies(cookies).userAgent(UA_CHROME).timeout(TIME_OUT).referrer(loginPageUrl).ignoreContentType(true).method(Method.POST).execute();
            cookies.putAll(res.cookies());
            //System.out.println(res.body());
            
            JSONObject jsonObj = new JSONObject(res.body());
            boolean isSign = jsonObj.getBoolean("isSign");
            int netdiskBonus = jsonObj.getInt("netdiskBonus");
            String signTime = jsonObj.getString("signTime");
            
            if(isSign)
            {
                resultFlag = "true";
                resultStr = "签到成功，获得" + netdiskBonus + "M空间，本日签到时间：" + signTime;
            }
            else
            {
                resultFlag = "true";
                resultStr = "今日已签过到，获得" + netdiskBonus + "M空间，本日签到时间：" + signTime;
            }
            
        } catch (IOException e) {
            this.resultFlag = "false";
            this.resultStr = "连接超时";
            e.printStackTrace();
        } catch (Exception e) {
            this.resultFlag = "false";
            this.resultStr = "未知错误！";
            e.printStackTrace();
        }
        
        return new String[]{resultFlag, resultStr};
    }
    
    
}
