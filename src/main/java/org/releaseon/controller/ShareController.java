package org.releaseon.controller;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.releaseon.domain.entity.Storage;
import org.releaseon.service.AppService;
import org.releaseon.service.PackageService;
import org.releaseon.service.StorageService;
import org.releaseon.storage.StorageUtil;
import org.releaseon.utils.file.PathManager;
import org.releaseon.utils.image.QRCodeUtil;
import org.releaseon.utils.ipa.PlistGenerator;
import org.releaseon.vo.AppViewModel;
import org.releaseon.vo.PackageViewModel;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * 对外分享 / 公开访问相关页面
 * 预览页、下载、manifest、二维码、设备列表、安装教程
 */
@Controller
public class ShareController {

    @Resource
    private AppService appService;
    @Resource
    private PackageService packageService;
    @Resource
    private StorageUtil storageUtil;
    @Resource
    private StorageService storageService;

    /**
     * 预览页
     */
    @GetMapping("/s/{code}")
    public String get(@PathVariable("code") String code, HttpServletRequest request) {
        String id = request.getParameter("id");
        AppViewModel viewModel = appService.findByCode(code, id, request);
        request.setAttribute("app", viewModel);
        request.setAttribute("basePath", PathManager.request(request).getBaseURL() + "/");
        return "install";
    }

    /**
     * 设备列表
     */
    @GetMapping("/devices/{id}")
    public String devices(@PathVariable("id") String id, HttpServletRequest request) {
        PackageViewModel viewModel = packageService.findById(id, request);
        request.setAttribute("app", viewModel);
        return "devices";
    }

    /**
     * 下载文件源文件（ipa 或 apk）
     */
    @RequestMapping("/p/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable("id") String id) {
        try {
            org.releaseon.domain.entity.Package aPackage = packageService.get(id);
            String key = aPackage.getSourceFile().getKey();
            if (key == null || key.contains("../")) {
                return ResponseEntity.notFound().build();
            }
            Storage storage = storageService.findByKey(key);
            org.springframework.core.io.Resource file = storageUtil.loadAsResource(key);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            String fileName = aPackage.getName() + "_" + aPackage.getVersion();
            String ext = "." + FilenameUtils.getExtension(aPackage.getSourceFile().getKey());
            String appName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            MediaType mediaType = MediaType.parseMediaType(storage.getType());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + appName + ext + "\"")
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取 manifest（iOS OTA 安装必需）
     */
    @RequestMapping("/m/{id}")
    public void getManifest(@PathVariable("id") String id, HttpServletRequest request,
                            HttpServletResponse response) {
        try {
            PackageViewModel viewModel = packageService.findById(id, request);
            if (viewModel != null && viewModel.isiOS()) {
                response.setContentType("application/force-download");
                response.setHeader("Content-Disposition", "attachment;fileName=manifest.plist");
                Writer writer = new OutputStreamWriter(response.getOutputStream());
                PlistGenerator.generate(viewModel, writer);
            }
        } catch (Exception e) {
            // 静默处理，客户端会收到空白响应
        }
    }

    /**
     * 获取包二维码
     */
    @RequestMapping("/p/code/{id}")
    public void getQrCode(@PathVariable("id") String id, HttpServletRequest request,
                          HttpServletResponse response) {
        try {
            PackageViewModel viewModel = packageService.findById(id, request);
            if (viewModel != null) {
                response.setContentType("image/png");
                InputStream inputStream = storageUtil.loadAsResource(viewModel.getIconKey()).getInputStream();
                QRCodeUtil.encode(viewModel.getPreviewURL())
                        .withSize(250, 250)
                        .withIcon(inputStream)
                        .writeTo(response.getOutputStream());
            }
        } catch (Exception e) {
            // 静默处理
        }
    }
}
