package com.dnd.board.controller;

import com.dnd.board.entity.Board;
import com.dnd.board.entity.SearchOption;
import com.dnd.board.http.request.BoardRequest;
import com.dnd.board.http.response.Board.BoardListResponse;
import com.dnd.board.http.response.Board.BoardPageResponse;
import com.dnd.board.http.response.Board.BoardResponse;
import com.dnd.board.http.response.GeneralResponse;
import com.dnd.board.service.BoardService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
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

    @Operation(tags = "Board", summary = "제목, 제목+내용으로 글을 찾습니다.",
            responses={
                    @ApiResponse(responseCode = "200", description = "제목, 제목+내용으로 글 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = BoardPageResponse.class)))
            })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "페이지 번호", dataType = "integer", paramType = "query", defaultValue = "0"),
            @ApiImplicitParam(name = "size", value = "한 페이지당 불러올 콘텐츠 개수", dataType = "integer", paramType = "query", defaultValue = "10"),
            @ApiImplicitParam(name = "sort", value = "정렬방법(id,desc,asc)", dataType = "string", paramType = "query", defaultValue = "asc")
    })
    @GetMapping("")   //제목으로 글 찾기
    public ResponseEntity<BoardPageResponse> getBoards(@PageableDefault Pageable pageable,
                                                       @RequestParam(name="searchOption", required = false, defaultValue = "title") SearchOption option,
                                                       @RequestParam(name="keyword", required = false) String keyword){
        String searchOption=option.getValue();
        List<BoardListResponse> boardListResponseList = new ArrayList<>();
        BoardPageResponse boardPageResponse;
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());
        if(keyword==null){
            boardPageResponse = boardService.getAllBoardList(pageable, boardListResponseList, option);
        }else {
            if(searchOption=="제목+내용"){
                boardPageResponse = boardService.getBoardListByTitleOrContents(pageable, boardListResponseList, option, keyword);
            }else {
                boardPageResponse = boardService.getBoardListByTitle(pageable, boardListResponseList, option, keyword);
            }
        }
        return new ResponseEntity<>(boardPageResponse, HttpStatus.OK);
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

    @PreAuthorize("isAuthenticated() and ((#boardRequest.writer == principal.username) or hasRole('ROLE_ADMIN'))")
    @PatchMapping("/update/{board_id}") // 게시판 수정
    public ResponseEntity<GeneralResponse> boardUpdate(@PathVariable(name="board_id") UUID uuid, @RequestBody BoardRequest boardRequest ){
        try{
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getCredentials());
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getDetails());
            boardService.updateBoard(uuid, boardRequest);
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.OK, "게시글이 수정되었습니다."), HttpStatus.OK);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(GeneralResponse.of(HttpStatus.NOT_FOUND,"수정할 게시글이 없습니다."), HttpStatus.NOT_FOUND);
        }
    }
}
