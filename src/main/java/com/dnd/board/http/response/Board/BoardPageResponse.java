package com.dnd.board.http.response.Board;

import com.dnd.board.controller.BoardController;
import com.dnd.board.entity.Board;
import com.dnd.board.entity.SearchOption;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BoardPageResponse {
    private PagedModel<BoardListResponse> boards;

    public BoardPageResponse(Pageable pageable, Page<Board> boardPage, SearchOption searchOption, String keyword, List<BoardListResponse> boardListResponseList) {
        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(pageable.getPageSize(), boardPage.getNumber(), boardPage.getTotalElements());
        boards = PagedModel.of(boardListResponseList, pageMetadata);
        boards.add(linkTo(methodOn(BoardController.class).getBoards(pageable, searchOption, keyword)).withSelfRel());
    }
}
