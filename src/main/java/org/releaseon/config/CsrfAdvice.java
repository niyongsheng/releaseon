package org.releaseon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 向所有 Thymeleaf 模板注入 CSRF token
 * 如果 session 中不存在则自动生成
 */
@ControllerAdvice
public class CsrfAdvice {

    private static final SecureRandom RANDOM = new SecureRandom();

    @ModelAttribute
    public void addCsrfToken(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(true);
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null) {
            csrfToken = generateToken();
            session.setAttribute("csrfToken", csrfToken);
        }
        model.addAttribute("csrfToken", csrfToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
