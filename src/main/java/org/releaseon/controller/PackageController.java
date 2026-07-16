package org.releaseon.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.releaseon.domain.entity.App;
import org.releaseon.domain.entity.User;
import org.releaseon.service.AppService;
import org.releaseon.service.PackageService;
import org.releaseon.service.UserService;
import org.releaseon.storage.StorageUtil;
import org.releaseon.utils.file.PathManager;
import org.releaseon.utils.response.BaseResponse;
import org.releaseon.utils.response.ResponseUtil;
import org.releaseon.utils.webhook.WebHookClient;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class PackageController {

    @Resource
    private AppService appService;
    @Resource
    private PackageService packageService;
    @Resource
    private UserService userService;
    @Resource
    private StorageUtil storageUtil;

    /**
     * 上传包
     */
    @RequestMapping("/app/upload")
    @ResponseBody
    public BaseResponse upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            User user = getUser(request);
            if (user == null) {
                return ResponseUtil.unauthz();
            }
            String filePath = storageUtil.checkAndTransfer(
                    file.getInputStream(), file.getContentType(), file.getOriginalFilename());
            if (filePath == null) {
                return ResponseUtil.fail(401, "不支持的文件类型");
            }
            Map<String, String> extra = getExtraParams(request);
            App app = this.appService.addPackage(filePath, extra, user);
            PathManager pathManager = PathManager.request(request);
            String codeURL = pathManager.getBaseURL()
                    + "/p/code/" + app.getCurrentPackage().getId();
            WebHookClient.sendMessage(app, pathManager.getBaseURL(), storageUtil);
            return ResponseUtil.ok(codeURL);
        } catch (Exception e) {
            return ResponseUtil.badArgument();
        }
    }

    /**
     * 删除包
     */
    @RequiresPermissions("/p/delete")
    @RequestMapping("/p/delete/{id}")
    @ResponseBody
    public BaseResponse deleteById(@PathVariable("id") String id) {
        this.packageService.deleteById(id);
        return ResponseUtil.ok();
    }

    @NotNull
    private Map<String, String> getExtraParams(HttpServletRequest request) {
        Map<String, String> extra = new java.util.HashMap<>();
        String jobName = request.getParameter("jobName");
        String buildNumber = request.getParameter("buildNumber");
        if (StringUtils.hasLength(jobName)) {
            extra.put("jobName", jobName);
        }
        if (StringUtils.hasLength(buildNumber)) {
            extra.put("buildNumber", buildNumber);
        }
        return extra;
    }

    private User getUser(HttpServletRequest request) {
        String token = request.getParameter("token");
        User user = this.userService.findByToken(token);
        if (user == null) {
            Subject currentUser = SecurityUtils.getSubject();
            user = (User) currentUser.getPrincipal();
        }
        return user;
    }
}
