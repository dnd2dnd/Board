package com.dnd.board.service;

import com.dnd.board.entity.Board;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.BoardRequest;
import com.dnd.board.http.response.BoardResponse;
import com.dnd.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

}
