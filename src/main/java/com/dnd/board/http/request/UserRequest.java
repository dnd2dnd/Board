package com.dnd.board.http.request;

import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
public class UserRequest {
    private String username;
    private String password;
    private String nickname;
}
