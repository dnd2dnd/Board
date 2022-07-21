package com.dnd.board.http.response;

import com.dnd.board.controller.CommentController;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
public class CommentPageResponse {
    private final PagedModel<CommentListResponse> comment;

    public CommentPageResponse(List<CommentListResponse> work, Pageable pageable, UUID uuid) {
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + 9), work.size());
        Page<CommentListResponse> workPage = new PageImpl<>(work.subList(start, end), pageable, work.size());

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(pageable.getPageSize(), workPage.getNumber(), workPage.getTotalElements());
        comment = PagedModel.of(workPage.getContent(), pageMetadata);
        comment.add(linkTo(methodOn(CommentController.class).getComments(pageable, uuid)).withSelfRel());
    }
}
