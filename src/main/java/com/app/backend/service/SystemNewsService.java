package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.SystemNewsCreateRequest;
import com.app.backend.dto.SystemNewsDto;
import com.app.backend.dto.SystemNewsUpdateRequest;
import com.app.backend.entity.SystemNews;
import com.app.backend.entity.User;
import com.app.backend.repository.SystemNewsRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SystemNewsService {

    private final SystemNewsRepository systemNewsRepository;
    private final UserService userService;

    public SystemNewsService(SystemNewsRepository systemNewsRepository, UserService userService) {
        this.systemNewsRepository = systemNewsRepository;
        this.userService = userService;
    }

    @Transactional
    public SystemNewsDto create(Long operatorUserId, SystemNewsCreateRequest req) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null || operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }

        SystemNews news = new SystemNews();
        news.setTitle(req.getTitle());
        news.setCoverImage(req.getCoverImage());
        news.setContent(req.getContent());
        news.setIsTop(req.getIsTop() != null ? req.getIsTop() : 0);
        news.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        news.setViewCount(0);
        news.setIsDeleted(0);

        String author = operator.getRealName();
        if (author == null || author.trim().isEmpty()) {
            author = operator.getUsername();
        }
        news.setAuthor(author);

        if (news.getStatus() != null && news.getStatus() == 1) {
            news.setPublishTime(LocalDateTime.now());
        }

        systemNewsRepository.insert(news);
        return toDto(news);
    }

    @Transactional
    public SystemNewsDto update(Long operatorUserId, Long newsId, SystemNewsUpdateRequest req) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null || operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }

        SystemNews news = getActiveAnyStatus(newsId);

        if (req.getTitle() != null) {
            news.setTitle(req.getTitle());
        }
        if (req.getCoverImage() != null) {
            news.setCoverImage(req.getCoverImage());
        }
        if (req.getContent() != null) {
            news.setContent(req.getContent());
        }
        if (req.getIsTop() != null) {
            news.setIsTop(req.getIsTop());
        }

        if (req.getStatus() != null) {
            if (req.getStatus() == 1 && (news.getStatus() == null || news.getStatus() != 1)) {
                news.setPublishTime(LocalDateTime.now());
            }
            news.setStatus(req.getStatus());
        }

        systemNewsRepository.updateById(news);
        return toDto(news);
    }

    @Transactional
    public SystemNewsDto publish(Long operatorUserId, Long newsId) {
        SystemNewsUpdateRequest req = new SystemNewsUpdateRequest();
        req.setStatus(1);
        return update(operatorUserId, newsId, req);
    }

    @Transactional
    public SystemNewsDto unpublish(Long operatorUserId, Long newsId) {
        SystemNewsUpdateRequest req = new SystemNewsUpdateRequest();
        req.setStatus(2);
        return update(operatorUserId, newsId, req);
    }

    @Transactional
    public SystemNewsDto top(Long operatorUserId, Long newsId) {
        SystemNewsUpdateRequest req = new SystemNewsUpdateRequest();
        req.setIsTop(1);
        return update(operatorUserId, newsId, req);
    }

    @Transactional
    public SystemNewsDto untop(Long operatorUserId, Long newsId) {
        SystemNewsUpdateRequest req = new SystemNewsUpdateRequest();
        req.setIsTop(0);
        return update(operatorUserId, newsId, req);
    }

    @Transactional
    public void delete(Long operatorUserId, Long newsId) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null || operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }

        SystemNews news = getActiveAnyStatus(newsId);
        news.setIsDeleted(1);
        systemNewsRepository.updateById(news);
    }

    @Transactional
    public SystemNewsDto detail(Long operatorUserId, Long newsId) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        SystemNews news = systemNewsRepository.selectById(newsId);
        if (news == null || (news.getIsDeleted() != null && news.getIsDeleted() != 0)) {
            throw new BizException(404, "资讯不存在");
        }

        boolean admin = operator.getRoleType() == 1;
        if (!admin) {
            if (news.getStatus() == null || news.getStatus() != 1) {
                throw new BizException(403, "无权限");
            }

            UpdateWrapper<SystemNews> uw = new UpdateWrapper<>();
            uw.eq("news_id", newsId);
            uw.eq("is_deleted", 0);
            uw.setSql("view_count = view_count + 1");
            systemNewsRepository.update(null, uw);

            Integer vc = news.getViewCount() != null ? news.getViewCount() : 0;
            news.setViewCount(vc + 1);
        }

        return toDto(news);
    }

    @Transactional(readOnly = true)
    public IPage<SystemNewsDto> page(Long operatorUserId,
                                    int page,
                                    int size,
                                    String title,
                                    Integer status,
                                    Integer isTop,
                                    boolean includeContent) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        boolean admin = operator.getRoleType() == 1;

        Page<SystemNews> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<SystemNews> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);

        if (title != null && !title.trim().isEmpty()) {
            qw.like("title", title.trim());
        }

        if (admin) {
            if (status != null) {
                qw.eq("status", status);
            }
            if (isTop != null) {
                qw.eq("is_top", isTop);
            }
        } else {
            qw.eq("status", 1);
        }

        qw.orderByDesc("is_top").orderByDesc("publish_time").orderByDesc("news_id");

        IPage<SystemNews> newsPage = systemNewsRepository.selectPage(p, qw);
        return newsPage.convert(n -> {
            SystemNewsDto dto = toDto(n);
            if (!includeContent) {
                dto.setContent(null);
            }
            return dto;
        });
    }

    private SystemNews getActiveAnyStatus(Long newsId) {
        SystemNews news = systemNewsRepository.selectById(newsId);
        if (news == null || (news.getIsDeleted() != null && news.getIsDeleted() != 0)) {
            throw new BizException(404, "资讯不存在");
        }
        return news;
    }

    private SystemNewsDto toDto(SystemNews news) {
        SystemNewsDto dto = new SystemNewsDto();
        dto.setNewsId(news.getNewsId());
        dto.setTitle(news.getTitle());
        dto.setAuthor(news.getAuthor());
        dto.setCoverImage(news.getCoverImage());
        dto.setContent(news.getContent());
        dto.setViewCount(news.getViewCount());
        dto.setIsTop(news.getIsTop());
        dto.setStatus(news.getStatus());
        dto.setPublishTime(news.getPublishTime());
        dto.setCreateTime(news.getCreateTime());
        dto.setUpdateTime(news.getUpdateTime());
        return dto;
    }
}
