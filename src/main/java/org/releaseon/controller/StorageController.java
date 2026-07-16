package org.releaseon.controller;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.releaseon.domain.entity.Storage;
import org.releaseon.service.StorageService;
import org.releaseon.storage.StorageUtil;
import org.releaseon.utils.CharUtil;
import org.releaseon.utils.response.BaseResponse;
import org.releaseon.utils.response.ResponseUtil;

import java.io.IOException;

@Controller
public class StorageController {

    @Autowired
    private StorageUtil storageUtil;
    @Autowired
    private StorageService storageService;

    private String generateKey(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        String suffix = originalFilename.substring(index);

        String key = null;
        Storage storageInfo = null;

        do {
            key = CharUtil.generate(20) + suffix;
            storageInfo = storageService.findByKey(key);
        }
        while (storageInfo != null);

        return key;
    }

    @RequiresAuthentication
    @PostMapping("/upload")
    @ResponseBody
    public BaseResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        Storage storage = storageUtil.store(file.getInputStream(), file.getSize(), file.getContentType(), originalFilename);
        if (storage != null) {
            return ResponseUtil.ok(storage);
        } else {
            return ResponseUtil.fail(401, "不支持的文件类型");
        }
    }

    /**
     * 访问存储对象
     *
     * @param key 存储对象key
     * @return
     */
    @GetMapping("/fetch/{key:.+}")
    public ResponseEntity<Resource> fetch(@PathVariable String key) {
        Storage Storage = storageService.findByKey(key);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        if (key.contains("../")) {
            return ResponseEntity.badRequest().build();
        }
        String type = Storage.getType();
        MediaType mediaType = MediaType.parseMediaType(type);

        Resource file = storageUtil.loadAsResource(key);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(mediaType).body(file);
    }

    /**
     * 访问存储对象
     *
     * @param key 存储对象key
     * @return
     */
    @GetMapping("/download/{key:.+}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
        Storage Storage = storageService.findByKey(key);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        if (key.contains("../")) {
            return ResponseEntity.badRequest().build();
        }

        String type = Storage.getType();
        MediaType mediaType = MediaType.parseMediaType(type);

        Resource file = storageUtil.loadAsResource(key);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(mediaType).header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
