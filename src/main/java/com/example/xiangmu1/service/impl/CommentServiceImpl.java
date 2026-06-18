package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.entity.Comment;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.CommentMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.CommentService;
import com.example.xiangmu1.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, ArticleMapper articleMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Long add(Long articleId, String content, Long userId, Long parentId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("父评论不存在");
            }
            if (!parent.getArticleId().equals(articleId)) {
                throw new IllegalArgumentException("不能回复其他文章下的评论");
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public List<CommentVO> list(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .orderByAsc(Comment::getCreateTime)
        );

        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, String> usernameMap = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return comments.stream().map(comment -> toVO(comment, usernameMap)).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }

        commentMapper.deleteById(id);
    }

    private CommentVO toVO(Comment comment, Map<Long, String> usernameMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setUsername(usernameMap.get(comment.getUserId()));
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }
}
