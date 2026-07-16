package org.releaseon.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.releaseon.domain.entity.Storage;
import org.releaseon.utils.CharUtil;

import java.io.File;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 提供存储服务类，所有存储服务均由该类对外提供
 */
public class StorageUtil {
    private String active;
    private IStorage storage;
    @Autowired
    private org.releaseon.service.StorageService storageService;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    /** ZIP 文件魔数前 4 字节 */
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    /**
     * 检测是否是有效的 ZIP 文件（APK/IPA 都是 ZIP 格式），然后转存到临时目录。
     * 使用 PushbackInputStream 在不消耗输入流的情况下先检查文件头，
     * 避免无效文件写入磁盘后再删除的浪费。
     */
    public String checkAndTransfer(InputStream inputStream, String contentType, String fileName) {
        try {
            // 先读取前 4 字节检查 ZIP 魔数
            PushbackInputStream pushback = new PushbackInputStream(inputStream, 4);
            byte[] header = new byte[4];
            int bytesRead = pushback.read(header);
            if (bytesRead < 4 || header[0] != ZIP_MAGIC[0] || header[1] != ZIP_MAGIC[1]
                    || header[2] != ZIP_MAGIC[2] || header[3] != ZIP_MAGIC[3]) {
                return null;
            }
            // 把魔数字节推回流中，继续用于后续写入
            pushback.unread(header, 0, bytesRead);

            // 获取文件后缀
            String ext = FilenameUtils.getExtension(fileName);
            // 生成文件名
            String newFileName = UUID.randomUUID().toString() + "." + ext;
            // 转存到 tmp
            String destPath = FileUtils.getTempDirectoryPath() + File.separator + newFileName;
            destPath = destPath.replaceAll("//", "/");
            Files.copy(pushback, Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
            return destPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储一个文件对象
     */
    public Storage store(InputStream inputStream, long contentLength, String contentType, String fileName) {
        String key = generateKey(fileName);
        storage.store(inputStream, contentLength, contentType, key);

        String url = generateUrl(key);
        Storage storageInfo = new Storage();
        storageInfo.setName(fileName);
        storageInfo.setSize((int) contentLength);
        storageInfo.setType(contentType);
        storageInfo.setKey(key);
        storageInfo.setUrl(url);
        this.storageService.save(storageInfo);

        return storageInfo;
    }

    private String generateKey(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        String suffix = originalFilename.substring(index);

        String key = null;
        Storage storageInfo = null;

        do {
            key = CharUtil.generate(20) + suffix;
            storageInfo = this.storageService.findByKey(key);
        }
        while (storageInfo != null);

        return key;
    }

    public Stream<Path> loadAll() {
        return storage.loadAll();
    }

    public Path load(String keyName) {
        return storage.load(keyName);
    }

    public Resource loadAsResource(String keyName) {
        return storage.loadAsResource(keyName);
    }

    public void delete(String keyName) {
        storage.delete(keyName);
    }

    private String generateUrl(String keyName) {
        return storage.generateUrl(keyName);
    }
}
