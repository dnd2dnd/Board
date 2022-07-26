package com.dnd.board.http.request;

import lombok.*;

import javax.persistence.Column;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardRequest {
    @Column
    private String title;

    @Column
    private String contents;
}
