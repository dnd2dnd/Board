package com.dnd.board.service;

import com.dnd.board.entity.Board;
import com.dnd.board.entity.Comment;
import com.dnd.board.entity.User;
import com.dnd.board.http.response.CommentListResponse;
import com.dnd.board.http.response.CommentPageResponse;
import com.dnd.board.http.response.CommentResponse;
import com.dnd.board.repository.BoardRepository;
import com.dnd.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;


@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;

    public void setComment(UUID uuid, Comment commentRequest){
        Comment comment = Comment.builder()
                .boardId(new Board(uuid))
                .userId(new User(UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName())))
                .comment(commentRequest.getComment())
                .build();
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID uuid){
        commentRepository.deleteById(uuid);
    }

    @Transactional
    public void updateComment(UUID uuid, Comment commentRequest){
        Comment comment = commentRepository.findById(uuid).orElseThrow(IllegalArgumentException::new);
        comment.setComment(commentRequest.getComment());
    }

    public CommentPageResponse getComment(Pageable pageable, UUID uuid){
        List<Comment> comments = commentRepository.findOptionalByBoardId_Idx(uuid);
        List<CommentListResponse> data = new ArrayList<>();
        for(int i=0; i<comments.size(); i++){
            CommentListResponse commentListResponse = CommentListResponse.builder()
                    .userId(comments.get(i).getUserId().getUserId())
                    .boardId(comments.get(i).getBoardId().getIdx())
                    .comment(comments.get(i).getComment())
                    .createdDate(comments.get(i).getCreatedDate())
                    .updatedDate(comments.get(i).getUpdatedDate())
                    .build();
            data.add(commentListResponse);
        }
        return new CommentPageResponse(data, pageable, uuid);
    }
}
