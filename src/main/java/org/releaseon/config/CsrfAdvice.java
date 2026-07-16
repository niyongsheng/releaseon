package org.releaseon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 向所有 Thymeleaf 模板注入 CSRF token
 * 如果 session 中不存在则自动生成
 */
@ControllerAdvice
public class CsrfAdvice {

    @ModelAttribute
    public void addCsrfToken(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(true);
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null) {
            csrfToken = CsrfTokenManager.generateToken();
            session.setAttribute("csrfToken", csrfToken);
        }
        model.addAttribute("csrfToken", csrfToken);
    }
}
