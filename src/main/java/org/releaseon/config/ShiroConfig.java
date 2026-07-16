package org.releaseon.config;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.releaseon.shiro.UserAuthorizingRealm;
import org.releaseon.shiro.UserWebSessionManager;

import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
public class ShiroConfig {

    // 会话超时时间：8 小时（毫秒）
    private static final long SESSION_TIMEOUT_MS = 8 * 60 * 60 * 1000L;

    @Bean
    public Realm realm() {
        return new UserAuthorizingRealm();
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 不需要权限可以访问的页面（来源：PublicPaths 常量）
        for (String exact : PublicPaths.EXACT) {
            filterChainDefinitionMap.put(exact, "anon");
        }
        for (String prefix : PublicPaths.PREFIXES) {
            filterChainDefinitionMap.put(prefix + "**", "anon");
        }

        // 需要登录才能访问的页面
        filterChainDefinitionMap.put("/upload", "authc");
        filterChainDefinitionMap.put("/apps/**", "authc");
        filterChainDefinitionMap.put("/admin/**", "authc");
        filterChainDefinitionMap.put("/webHook/**", "authc");
        // 登录页面
        shiroFilterFactoryBean.setLoginUrl("/account/signin");
        // 成功后跳转页面
        shiroFilterFactoryBean.setSuccessUrl("/apps");
        // 未授权页面
        shiroFilterFactoryBean.setUnauthorizedUrl("/error/unauthorized");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public SessionManager sessionManager() {
        UserWebSessionManager sessionManager = new UserWebSessionManager();
        // 会话超时（毫秒），默认 8 小时
        sessionManager.setGlobalSessionTimeout(SESSION_TIMEOUT_MS);
        // 删除无效会话
        sessionManager.setDeleteInvalidSessions(true);
        return sessionManager;
    }

    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm());
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =
                new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public static DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}