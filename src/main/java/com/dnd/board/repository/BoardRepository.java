package com.dnd.board.repository;

import com.dnd.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {
    Page<Board> findByTitleContaining(Pageable pageable, String title);
    Page<Board> findByTitleContainingOrContentsContaining(Pageable pageable, String title, String content);
}
