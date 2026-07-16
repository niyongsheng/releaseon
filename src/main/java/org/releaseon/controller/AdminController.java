package org.releaseon.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.releaseon.domain.entity.User;
import org.releaseon.service.RoleService;
import org.releaseon.service.UserService;
import org.releaseon.utils.file.PathManager;
import org.releaseon.utils.response.BaseResponse;
import org.releaseon.utils.response.ResponseUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;

    /**
     * 用户管理列表页
     */
    @GetMapping("/users")
    public String users(HttpServletRequest request) {
        Subject subject = SecurityUtils.getSubject();
        User currentUser = (User) subject.getPrincipal();
        if (!isAdmin(currentUser)) {
            return "redirect:/account/signin";
        }

        List<User> users = this.userService.findAll();
        // 预格式化时间，避免 Thymeleaf 日期工具兼容问题
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        Map<String, String> createDates = new HashMap<>();
        Map<String, String> loginDates = new HashMap<>();
        for (User u : users) {
            createDates.put(u.getId(), u.getCreateTime() > 0 ? dtf.format(Instant.ofEpochMilli(u.getCreateTime())) : "-");
            loginDates.put(u.getId(), u.getLastLoginTime() > 0 ? dtf.format(Instant.ofEpochMilli(u.getLastLoginTime())) : "从未登录");
        }
        request.setAttribute("createDates", createDates);
        request.setAttribute("loginDates", loginDates);
        request.setAttribute("users", users);
        request.setAttribute("roles", this.roleService.findAll());
        request.setAttribute("isAdmin", true);
        request.setAttribute("token", currentUser.getToken());
        request.setAttribute("username", currentUser.getUsername());
        request.setAttribute("basePath", PathManager.request(request).getBaseURL() + "/");
        return "admin/users";
    }

    /**
     * 创建/更新用户
     */
    @PostMapping("/users/save")
    @ResponseBody
    public BaseResponse save(@RequestBody SaveUserRequest body) {
        if (!isAdmin(getCurrentUser())) {
            return ResponseUtil.unauthz();
        }

        // 校验用户名
        if (body.getUsername() == null || body.getUsername().isBlank()) {
            return ResponseUtil.fail("用户名不能为空");
        }

        // 校验用户名唯一性
        User existing = this.userService.findByUsername(body.getUsername().trim());
        if (existing != null && !existing.getId().equals(body.getId())) {
            return ResponseUtil.fail("用户名已被使用");
        }

        if (body.getId() != null && !body.getId().isBlank()) {
            // 更新
            User user = this.userService.updateUser(
                    body.getId(), body.getUsername().trim(),
                    body.getPassword(), body.getRoleId(), body.getEnabled());
            if (user == null) {
                return ResponseUtil.fail("用户不存在");
            }
            return ResponseUtil.ok("更新成功");
        } else {
            // 创建
            if (body.getPassword() == null || body.getPassword().isBlank()) {
                return ResponseUtil.fail("密码不能为空");
            }
            if (body.getRoleId() == null || body.getRoleId().isBlank()) {
                return ResponseUtil.fail("请选择角色");
            }
            this.userService.createUser(body.getUsername().trim(), body.getPassword(), body.getRoleId(), body.getEnabled());
            return ResponseUtil.ok("创建成功");
        }
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/delete/{id}")
    @ResponseBody
    public BaseResponse delete(@PathVariable("id") String id) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return ResponseUtil.unauthz();
        }
        if (currentUser.getId().equals(id)) {
            return ResponseUtil.fail("不能删除自己");
        }
        this.userService.deleteById(id);
        return ResponseUtil.ok("删除成功");
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/users/check-username")
    @ResponseBody
    public BaseResponse checkUsername(@RequestParam("username") String username,
                                      @RequestParam(value = "excludeId", required = false) String excludeId) {
        if (!isAdmin(getCurrentUser())) {
            return ResponseUtil.unauthz();
        }
        User existing = this.userService.findByUsername(username.trim());
        if (existing != null && !existing.getId().equals(excludeId)) {
            return ResponseUtil.fail("用户名已被使用");
        }
        return ResponseUtil.ok("用户名可用");
    }

    // ─── helpers ───

    private boolean isAdmin(User user) {
        if (user.getRoleList() == null) return false;
        return user.getRoleList().stream()
                .anyMatch(role -> "管理员".equals(role.getName()));
    }

    private User getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        return (User) subject.getPrincipal();
    }

    // ─── request body ───

    public static class SaveUserRequest {
        private String id;
        private String username;
        private String password;
        private String roleId;
        private Boolean enabled;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }
}
