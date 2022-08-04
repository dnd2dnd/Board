package com.dnd.board.entity;

public enum SearchOption {

    title("제목"),
    title_contents("제목+내용");

    private String searchOption;

    SearchOption(String searchOption){
        this.searchOption=searchOption;
    }
    public String getValue(){
        return this.searchOption;
    }
}
