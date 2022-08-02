package com.dnd.board.controller;

import com.dnd.board.entity.Board;
import com.dnd.board.http.request.BoardRequest;
import com.dnd.board.http.response.BoardResponse;
import com.dnd.board.http.response.GeneralResponse;
import com.dnd.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/{board_id}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable(name="board_id") UUID uuid){
        return new ResponseEntity<>(boardService.getBoard(uuid), HttpStatus.OK);
    }

    @PostMapping("") // 게시판 생성
    public ResponseEntity<GeneralResponse> createBoard(@Valid @RequestBody BoardRequest boardRequest){
        try{
            boardService.setBoard(boardRequest);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK, "성공"), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.BAD_REQUEST, "실패"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{board_id}")
    public ResponseEntity<GeneralResponse> deleteBoard(@PathVariable(name="board_id") UUID uuid){
        try{
            boardService.deleteBoard(uuid);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK,"게시글이 삭제되었습니다."), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.NOT_FOUND,"삭제할 게시글이 없습니다."), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("isAuthenticated() and ((#writer == principal.username) or hasRole('ROLE_ADMIN'))")
    @PatchMapping("/update/{board_id}") // 게시판 수정
    public ResponseEntity<GeneralResponse> boardUpdate(@PathVariable(name="board_id") UUID uuid,
                                                       @RequestPart(name = "boardRequest") Board boardRequest, @RequestPart(name = "writer") String writer ){
        try{
            boardService.updateBoard(uuid, boardRequest);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK, "게시글이 수정되었습니다"), HttpStatus.OK);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.NOT_FOUND,"수정할 게시글이 없습니다."), HttpStatus.NOT_FOUND);
        }
    }
}
