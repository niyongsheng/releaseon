package org.releaseon.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.releaseon.domain.entity.User;
import org.releaseon.service.AppService;
import org.releaseon.config.SecurityUtil;
import org.releaseon.utils.file.PathManager;
import org.releaseon.utils.response.BaseResponse;
import org.releaseon.utils.response.ResponseUtil;
import org.releaseon.vo.AppViewModel;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AppController {

    @Resource
    private AppService appService;

    @RequiresAuthentication
    @GetMapping("/apps")
    public String apps(HttpServletRequest request) {
        try {
            Subject currentUser = SecurityUtils.getSubject();
            User user = (User) currentUser.getPrincipal();
            List<AppViewModel> apps;
            if (SecurityUtil.isAdmin(user)) {
                apps = this.appService.findAll(request);
            } else {
                apps = this.appService.findByUser(user, request);
            }
            request.setAttribute("apps", apps);
            request.setAttribute("baseURL", PathManager.request(request).getBaseURL());
            request.setAttribute("token", user.getToken());
            request.setAttribute("isAdmin", SecurityUtil.isAdmin(user));
            request.setAttribute("username", user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "index";
    }

    @RequiresPermissions("/apps/get")
    @GetMapping("/apps/{appID}")
    public String getAppById(@PathVariable("appID") String appID, HttpServletRequest request) {
        Subject currentUser = SecurityUtils.getSubject();
        User user = (User) currentUser.getPrincipal();
        AppViewModel appViewModel = this.appService.getById(appID, user, request);
        request.setAttribute("package", appViewModel);
        request.setAttribute("apps", appViewModel.getPackageList());
        request.setAttribute("token", user.getToken());
        return "list";
    }

    @RequiresPermissions("/packageList/get")
    @RequestMapping("/packageList/{appID}")
    @ResponseBody
    public Map<String, Object> getAppPackageList(@PathVariable("appID") String appID, HttpServletRequest request) {
        Subject currentUser = SecurityUtils.getSubject();
        User user = (User) currentUser.getPrincipal();
        AppViewModel appViewModel = this.appService.getById(appID, user, request);
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("packages", appViewModel.getPackageList());
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
        }
        return map;
    }

    @RequestMapping("/app/delete/{id}")
    @ResponseBody
    public BaseResponse deleteById(@PathVariable("id") String id, HttpServletRequest request) {
        try {
            Subject currentUser = SecurityUtils.getSubject();
            User user = (User) currentUser.getPrincipal();
            if (user == null) {
                return ResponseUtil.unauthz();
            }
            AppViewModel viewModel = this.appService.getById(id, user, request);
            if (viewModel == null) {
                return ResponseUtil.unauthz();
            }
            if (SecurityUtil.isAdmin(user) || viewModel.getUserId().equals(user.getId())) {
                this.appService.deleteById(id);
                return ResponseUtil.ok("删除成功");
            }
            return ResponseUtil.unauthz();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail();
        }
    }

}
