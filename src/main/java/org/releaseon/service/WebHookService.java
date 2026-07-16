package org.releaseon.service;


import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.releaseon.domain.repository.AppRepository;
import org.releaseon.domain.repository.WebHookRepository;
import org.releaseon.domain.entity.App;
import org.releaseon.domain.entity.WebHook;
import org.releaseon.vo.WebHookViewModel;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebHookService {

    @Resource
    private WebHookRepository webHookDao;
    @Resource
    private AppRepository appDao;

    @Transactional
    public WebHook save(WebHookViewModel viewModel) {
        App app = appDao.findById(viewModel.getAppId()).get();
        if (app != null) {
            WebHook webHook = new WebHook();
            BeanUtils.copyProperties(viewModel, webHook);
            webHook.setApp(app);
            webHook.setType(WebHook.WEB_HOOK_TYPE_DING_DING);
            return webHookDao.save(webHook);
        }
        return null;
    }

    @Transactional
    public WebHook get(String id) {
        WebHook webHook = this.webHookDao.findById(id).get();
        return webHook;
    }

    @Transactional
    public void deleteById(String id) {
        WebHook webHook = this.webHookDao.findById(id).get();
        if (webHook != null) {
            this.webHookDao.deleteById(id);
        }

    }

    @Transactional
    public List<WebHook> findByAppId(String appId) {
        App app = appDao.findById(appId).orElse(null);
        if (app != null) {
            return app.getWebHookList();
        }
        return new ArrayList<>();
    }

    public void update(WebHookViewModel viewModel) {
        WebHook webHook = webHookDao.findById(viewModel.getId()).get();
        if (webHook != null) {
            webHook.setName(viewModel.getName());
            webHook.setUrl(viewModel.getUrl());
            webHookDao.save(webHook);
        }
    }
}
