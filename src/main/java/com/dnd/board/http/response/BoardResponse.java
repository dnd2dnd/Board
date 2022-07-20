package com.dnd.board.http.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BoardResponse {
    private String nickname;
    private String title;
    private String contents;
}
