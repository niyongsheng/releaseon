package org.releaseon.service;


import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.releaseon.domain.repository.PermissionRepository;
import org.releaseon.domain.repository.RoleRepository;
import org.releaseon.domain.repository.UserRepository;
import org.releaseon.domain.entity.Permission;
import org.releaseon.domain.entity.Role;
import org.releaseon.domain.entity.User;
import org.releaseon.utils.bcrypt.BCryptPasswordEncoder;
import org.releaseon.utils.bcrypt.TokenManager;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @Resource
    private UserRepository userDao;
    @Resource
    private PermissionRepository permissionDao;
    @Resource
    private RoleRepository roleDao;
    @Resource
    private Environment environment;

    @Transactional
    public User save(User user) {
        updateToken(user);
        return this.userDao.save(user);
    }

    /**
     * 更新token
     *
     * @param user
     */
    private void updateToken(User user) {
        String token = user.getToken();
        User tokenUser = this.userDao.findByToken(token);
        if (tokenUser == null) {
            token = TokenManager.generateToken(user.getUsername(), user.getPassword());
        } else {
            if (!(user.getUsername().equals(tokenUser.getUsername())
                    && user.getPassword().equals(tokenUser.getPassword()))) {
                token = TokenManager.generateToken(user.getUsername(), user.getPassword());
            }
        }
        user.setToken(token);
    }

    @Transactional
    public List<User> findAll() {
        Iterable<User> users = this.userDao.findAll();
        List<User> list = new ArrayList<>();
        for (User user : users) {
            // 提前加载懒加载字段，避免 Thymeleaf 渲染时 LazyInitializationException
            if (user.getRoleList() != null) {
                user.getRoleList().size();
            }
            list.add(user);
        }
        return list;
    }

    @Transactional
    public User login(String username, String password) {
        User user = this.userDao.findByUsername(username);
        if (user != null) {
            BCryptPasswordEncoder encoder = ENCODER;
            if (!encoder.matches(password, user.getPassword())) return null;
            // 禁用用户不允许登录
            if (user.getEnable() == null || !user.getEnable()) return null;
            // 级联查询
            user.getRoleList().forEach(role -> {
                role.getPermissionList().forEach(permission -> {
                });
            });
        }
        return user;
    }

    @Transactional
    public User findByToken(String token) {
        if (StringUtils.isEmpty(token)) return null;
        return this.userDao.findByToken(token);
    }

    @Transactional
    public void deleteById(String id) {
        User user = this.userDao.findById(id).get();
        if (user != null) {
            this.userDao.deleteById(id);
        }
    }

    @Transactional
    public void updateById(User user) {
        this.userDao.save(user);
    }

    @Transactional
    public User createUser(String username, String password) {
        return createUser(username, password, null, false);
    }

    @Transactional
    public User createUser(String username, String password, String roleId) {
        return createUser(username, password, roleId, true);
    }

    @Transactional
    public User createUser(String username, String password, String roleId, Boolean enabled) {
        User user = new User();
        user.setEnable(enabled != null ? enabled : false);
        user.setUsername(username);
        BCryptPasswordEncoder encoder = ENCODER;
        user.setPassword(encoder.encode(password));
        user.setCreateTime(System.currentTimeMillis());

        if (roleId != null) {
            Role role = this.roleDao.findById(roleId).orElse(null);
            if (role != null) {
                Set<Role> roleList = new HashSet<>();
                roleList.add(role);
                user.setRoleList(roleList);
            }
        } else {
            // 注册用户默认分配"普通用户"角色
            List<Role> userList = this.roleDao.findByName("普通用户");
            if (userList != null && userList.size() > 0) {
                Role role = userList.get(0);
                Set<Role> roleList = new HashSet<>();
                roleList.add(role);
                user.setRoleList(roleList);
            }
        }
        updateToken(user);
        this.userDao.save(user);
        return user;
    }

    @Transactional
    public void initUsers() {
        initAdminUser();
        initRegularRole();
    }

    /**
     * 创建/确保"普通用户"角色存在（注册用户默认分配此角色）
     */
    @Transactional
    public void initRegularRole() {
        List<Role> existing = this.roleDao.findByName("普通用户");
        if (existing != null && !existing.isEmpty()) {
            return; // 已存在
        }
        long now = System.currentTimeMillis();
        Role role = new Role();
        role.setName("普通用户");
        role.setDescription("普通用户（注册默认）");
        role.setEnabled(true);
        role.setCreateTime(now);
        this.roleDao.save(role);

        // 普通用户仅拥有基础的查看权限
        String[] perms = new String[]{
                "/apps",
                "/apps/get",
        };
        for (String permStr : perms) {
            Permission permission = new Permission();
            permission.setPermission(permStr);
            permission.setCreateTime(now);
            permission.setUpdateTime(now);
            permission.setRole(role);
            this.permissionDao.save(permission);
        }
    }

    /**
     * 初始化管理员用户（首次启动时）
     */
    private void initAdminUser() {
        String username = environment.getProperty("admin.username");
        String password = environment.getProperty("admin.password");
        if (this.userDao.findByUsername(username) != null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Role role = new Role();
        List<Role> adminList = this.roleDao.findByName("管理员");
        if (adminList != null && adminList.size() > 0) {
            role = adminList.get(0);
        } else {
            role.setCreateTime(currentTime);
            role.setDescription("管理员");
            role.setName("管理员");
            role.setEnabled(true);
            this.roleDao.save(role);
        }
        List<Permission> permissionList = new ArrayList<>();
        String[] perms = new String[]{
                "/apps",
                "/apps/get",
                "/app/delete",
                "/packageList/get"
        };
        for (String permStr : perms) {
            Permission permission = null;
            List<Permission> permissions = this.permissionDao.findByPermission(permStr, role.getId());
            if (permissions != null) {
                for (int i = 0; i < permissions.size(); i++) {
                    if (permissions.get(i).getRole().getId().equalsIgnoreCase(role.getId())) {
                        permission = permissions.get(i);
                        break;
                    }
                }
            }
            if (permission == null) {
                permission = new Permission();
                permission.setCreateTime(currentTime);
            }
            permission.setPermission(permStr);
            permission.setUpdateTime(currentTime);
            permission.setRole(role);
            this.permissionDao.save(permission);
            permissionList.add(permission);
        }

        User user = new User();
        user.setEnable(true);
        user.setUsername(username);
        BCryptPasswordEncoder encoder = ENCODER;
        user.setPassword(encoder.encode(password));
        user.setCreateTime(currentTime);

        Set<Role> roleList = new HashSet<>();
        roleList.add(role);
        user.setRoleList(roleList);
        updateToken(user);
        this.userDao.save(user);
    }

    @Transactional
    public User updateUser(String id, String username, String password, String roleId, Boolean enabled) {
        User user = this.userDao.findById(id).orElse(null);
        if (user == null) return null;

        if (StringUtils.hasText(username)) user.setUsername(username);
        if (StringUtils.hasText(password)) {
            BCryptPasswordEncoder encoder = ENCODER;
            user.setPassword(encoder.encode(password));
        }
        if (enabled != null) user.setEnable(enabled);
        if (StringUtils.hasText(roleId)) {
            Role role = this.roleDao.findById(roleId).orElse(null);
            if (role != null) {
                Set<Role> roleList = new HashSet<>();
                roleList.add(role);
                user.setRoleList(roleList);
            }
        }
        user.setUpdateTime(System.currentTimeMillis());
        updateToken(user);
        return this.userDao.save(user);
    }

    public User findByUsername(String username) {
        return this.userDao.findByUsername(username);
    }
}
