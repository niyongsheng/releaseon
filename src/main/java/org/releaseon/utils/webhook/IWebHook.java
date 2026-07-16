package org.releaseon.utils.webhook;

import org.releaseon.domain.entity.App;
import org.releaseon.storage.StorageUtil;

public interface IWebHook {
    void sendMessage(App app, String baseURL, StorageUtil storageUtil);
}
