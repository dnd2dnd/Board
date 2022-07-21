package com.dnd.board.http.response;

import com.dnd.board.controller.CommentController;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@Setter
@Builder
public class CommentListResponse {
    private UUID boardId;
    private UUID userId;
    private String comment;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime updatedDate;
}
