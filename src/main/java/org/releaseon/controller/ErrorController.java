package org.releaseon.controller;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ErrorController extends BasicErrorController {

    public ErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    @Override
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        HttpStatus status = getStatus(request);
        Object msgObj = body.get("message");
        String message = msgObj != null ? msgObj.toString() : "请求错误";
        if (message.startsWith("Subject does not have permission")) {
            message = "无操作权限";
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Object statusCode = body.get("status");
        map.put("code", statusCode != null ? statusCode : status.value());
        map.put("msg", message);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
}
