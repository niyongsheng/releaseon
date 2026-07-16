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
import org.releaseon.utils.response.BaseResponse;
import org.releaseon.utils.response.ResponseUtil;

import java.io.IOException;

@Controller
public class StorageController {

    @Autowired
    private StorageUtil storageUtil;
    @Autowired
    private StorageService storageService;

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
     * 访问存储对象（内联预览）
     */
    @GetMapping("/fetch/{key:.+}")
    public ResponseEntity<Resource> fetch(@PathVariable String key) {
        return loadResource(key, false);
    }

    /**
     * 下载存储对象（强制下载）
     */
    @GetMapping("/download/{key:.+}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
        return loadResource(key, true);
    }

    /**
     * 加载存储资源
     *
     * @param key        存储对象 key
     * @param asDownload 是否以附件形式下载（true 则添加 Content-Disposition 头）
     */
    private ResponseEntity<Resource> loadResource(String key, boolean asDownload) {
        if (key == null || key.contains("../")) {
            return ResponseEntity.badRequest().build();
        }
        Storage storage = storageService.findByKey(key);
        if (storage == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.parseMediaType(storage.getType());
        Resource file = storageUtil.loadAsResource(key);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok().contentType(mediaType);
        if (asDownload) {
            builder.header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"");
        }
        return builder.body(file);
    }
}
