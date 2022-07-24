package com.dnd.board.controller;

import com.dnd.board.entity.Board;
import com.dnd.board.entity.Comment;
import com.dnd.board.http.response.CommentPageResponse;
import com.dnd.board.http.response.GeneralResponse;
import com.dnd.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/comment")
@RestController
public class CommentController {

    private final CommentService commentService;


    @PostMapping("{board_id}")
    public ResponseEntity<GeneralResponse> setComment(@PathVariable(name = "board_id") UUID uuid, @RequestBody Comment comment){
        try{
            commentService.setComment(uuid, comment);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK, "성공"), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.BAD_REQUEST, "실패"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("{comment_id}")
    public ResponseEntity<GeneralResponse> deleteBoard(@PathVariable(name="comment_id") UUID uuid){
        try{
            commentService.deleteComment(uuid);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK,"댓글이 삭제되었습니다."), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.NOT_FOUND,"삭제할 댓글이 없습니다."), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/update/{comment_id}") // 게시판 수정
    public ResponseEntity<GeneralResponse> boardUpdate(@PathVariable(name="comment_id") UUID uuid,
                                                       @RequestBody Comment commentRequest){
        try{
            commentService.updateComment(uuid, commentRequest);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK, "댓글이 수정되었습니다"), HttpStatus.OK);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.NOT_FOUND,"수정할 댓글이 없습니다."), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("{board_id}")
    public CommentPageResponse getComments(@PageableDefault Pageable pageable, @PathVariable(name = "board_id") UUID uuid){
        return commentService.getComment(pageable, uuid);
    }

}
