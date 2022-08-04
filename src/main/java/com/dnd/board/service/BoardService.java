package com.dnd.board.service;

import com.dnd.board.entity.Board;
import com.dnd.board.entity.SearchOption;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.BoardRequest;
import com.dnd.board.http.response.Board.BoardListResponse;
import com.dnd.board.http.response.Board.BoardPageResponse;
import com.dnd.board.http.response.Board.BoardResponse;
import com.dnd.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    private final UserService userService;

    public void setBoard(BoardRequest boardRequest){
        Board board = Board.builder()
                .userId(new User(userService.getMyUserWithAuthorities().get().getUserId()))
                .title(boardRequest.getTitle())
                .contents(boardRequest.getContents())
                .build();
        boardRepository.save(board);
    }

    public BoardResponse getBoard(UUID uuid){
        Board board = boardRepository.findById(uuid).orElseThrow(IllegalArgumentException::new);
        BoardResponse boardResponse = BoardResponse.builder()
                .nickname(board.getUserId().getNickname())
                .title(board.getTitle())
                .contents(board.getContents())
                .build();
        return boardResponse;
    }

    @Transactional
    public void deleteBoard(UUID uuid){
        boardRepository.deleteById(uuid);
    }

    @Transactional
    public void updateBoard(UUID uuid, Board boardRequest){
        Board board = boardRepository.findById(uuid).orElseThrow(IllegalArgumentException::new);
        board.setTitle(boardRequest.getTitle());
        board.setContents(boardRequest.getContents());
    }

    public void userCheck(Board board, Board boardRequest){
        System.out.println(board.getUserId().getUsername());
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

    }

    public BoardPageResponse getAllBoardList(Pageable pageable, List<BoardListResponse> boardListResponseList, SearchOption searchOption) {
        Page<Board> boardPage = boardRepository.findAll(pageable);
        addBoardListResponse(boardPage, boardListResponseList);
        return new BoardPageResponse(pageable, boardPage, searchOption, null, boardListResponseList);
    }

    public BoardPageResponse getBoardListByTitleOrContents(Pageable pageable, List<BoardListResponse> boardListResponseList, SearchOption searchOption, String keyword) {
        Page<Board> boardPage = boardRepository.findByTitleContainingOrContentsContaining(pageable, keyword, keyword);
        if (boardPage.getContent().isEmpty()){
            addNullBoardListResponse(boardListResponseList);
        } else {
            addBoardListResponse(boardPage, boardListResponseList);
        }
        return new BoardPageResponse(pageable, boardPage, searchOption, keyword, boardListResponseList);
    }

    public BoardPageResponse getBoardListByTitle(Pageable pageable, List<BoardListResponse> boardListResponseList, SearchOption searchOption, String keyword) {
        Page<Board> boardPage = boardRepository.findByTitleContaining(pageable, keyword);
        if (boardPage.getContent().isEmpty()){
            addNullBoardListResponse(boardListResponseList);
        } else {
            addBoardListResponse(boardPage, boardListResponseList);
        }
        return new BoardPageResponse(pageable, boardPage, searchOption, keyword, boardListResponseList);
    }

    public void addBoardListResponse(Page<Board> boards, List<BoardListResponse> boardListResponseList){
        if(boards.getSize()>=boards.getTotalElements()){
            for(int i=0; i<boards.getTotalElements(); i++){
                BoardListResponse boardListResponse = BoardListResponse.builder()
                        .idx(boards.getContent().get(i).getIdx())
                        .title(boards.getContent().get(i).getTitle())
                        .createdDate(boards.getContent().get(i).getCreatedDate())
                        .build();
                boardListResponseList.add(boardListResponse);
            }
        }else {
            int contentSize = boards.getSize()*boards.getNumber();
            for(int i=0; i<boards.getSize();i++){
                if(contentSize>=boards.getTotalElements())
                    break;
                System.out.println(contentSize +" " + boards.getSize() +" " + boards.getNumber());
                BoardListResponse boardListResponse = BoardListResponse.builder()
                        .idx(boards.getContent().get(i).getIdx())
                        .title(boards.getContent().get(i).getTitle())
                        .createdDate(boards.getContent().get(i).getCreatedDate())
                        .build();
                boardListResponseList.add(boardListResponse);
                contentSize++;
            }
        }
    }

    public static void addNullBoardListResponse(List<BoardListResponse> boardListResponseList){
        BoardListResponse boardListResponse = BoardListResponse.builder()
                .idx(null)
                .title(null)
                .createdDate(null)
                .build();
        boardListResponseList.add(boardListResponse);
    }
}
