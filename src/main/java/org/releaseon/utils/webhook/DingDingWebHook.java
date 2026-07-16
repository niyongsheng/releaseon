package org.releaseon.utils.webhook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.releaseon.domain.entity.App;
import org.releaseon.domain.entity.Package;
import org.releaseon.domain.entity.WebHook;
import org.releaseon.storage.StorageUtil;
import org.releaseon.utils.file.PathManager;
import org.releaseon.utils.image.ImageUtils;
import org.releaseon.utils.image.QRCodeUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DingDingWebHook implements IWebHook {

    private static final Logger log = LoggerFactory.getLogger(DingDingWebHook.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();

    /**
     * 发送钉钉消息
     *
     * @param jsonString 消息内容
     * @param webhook    钉钉自定义机器人webhook
     * @return
     */
    private static boolean sendToDingding(String jsonString, String webhook) {
        try {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonString);
            Request.Builder builder = new Request.Builder().url(webhook);
            builder.addHeader("Content-Type", "application/json; charset=utf-8").post(body);

            Request request = builder.build();
            try (Response response = client.newCall(request).execute()) {
                String string = response.body().string();
                log.info("send ding message:{}", string);
                JSONObject result = JSONObject.parseObject(jsonString);
                log.info("errcode:{}", result.getInteger("errcode"));
            }
            return true;
        } catch (Exception e) {
            log.error("send ding message error", e);
            return false;
        }
    }

    @Override
    public void sendMessage(App app, String baseURL, StorageUtil storageUtil) {
        if (app.getWebHookList() == null || app.getWebHookList().size() < 1) {
            return;
        }
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", app.getName());
        String currentPackageURL = baseURL + "/s/" + app.getShortCode() + "?id=" + app.getCurrentPackage().getId();
        String appURL = "/apps/" + app.getId();
        String platform = "iOS";
        if (app.getPlatform().equalsIgnoreCase("android")) {
            platform = "Android";
        }

        String appInfo = String.format("[%s(%s)更新](%s)", app.getName(), platform, appURL);
        Resource resource = storageUtil.loadAsResource(app.getCurrentPackage().getIconFile().getKey());
        // 将图片转为 base64, 内网 ip 钉钉无法访问，直接给图片数据
        String codePath = PathManager.getTempFilePath("jpg");
        File codeFile = new File(codePath);
        // 图片不存在，生成图片
        if (!codeFile.exists()) {
            try {
                QRCodeUtil.encode(currentPackageURL).withSize(150, 150).withIcon(resource.getInputStream()).writeTo(codeFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String icon = "data:image/jpg;base64," + ImageUtils.convertImageToBase64(codePath);
        String pathInfo = String.format("![%s](%s)", app.getName(), icon);
        String otherInfo = String.format("链接：[前往下载🛫](%s) \n\n 版本：%s (Build: %s)", currentPackageURL, app.getCurrentPackage().getVersion(), app.getCurrentPackage().getBuildVersion());
        String message = this.getPackageMessage(app.getCurrentPackage());
        String text = appInfo + " \n\n " + pathInfo + " \n\n " + otherInfo;
        if (message.length() > 0) {
            text += "\n\n" + message;
        }
        markdown.put("text", text);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgtype", "markdown");
        jsonObject.put("markdown", markdown);
        String json = jsonObject.toJSONString();
        for (WebHook webHook :
                app.getWebHookList()) {
            sendToDingding(json, webHook.getUrl());
        }
    }

    /**
     * 获取扩展消息
     *
     * @return
     */
    private String getPackageMessage(Package aPackage) {
        String message = "";
        if (StringUtils.hasLength(aPackage.getExtra())) {
            Map<String, String> extra = (Map<String, String>) JSON.parse(aPackage.getExtra());
            if (extra.containsKey("jobName")) {
                message += "任务名:" + extra.get("jobName");
            }

            if (extra.containsKey("buildNumber")) {
                message += " 编号:#" + extra.get("buildNumber");
            }
        }
        return message;
    }
}
