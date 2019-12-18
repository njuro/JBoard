package com.github.njuro.jboard.board;

import com.github.njuro.jboard.common.Mappings;
import com.github.njuro.jboard.config.security.methods.HasAuthorities;
import com.github.njuro.jboard.user.UserAuthority;
import com.github.njuro.jboard.utils.SensitiveDataFilter;
import com.jfilter.filter.DynamicFilter;
import com.jfilter.filter.FieldFilterSetting;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Mappings.API_ROOT_BOARDS)
public class BoardRestController {

  private final BoardFacade boardFacade;

  @Autowired
  public BoardRestController(BoardFacade boardFacade) {
    this.boardFacade = boardFacade;
  }

  @PutMapping
  @HasAuthorities(UserAuthority.MANAGE_BOARDS)
  public Board createBoard(@RequestBody @Valid BoardForm boardForm) {
    return boardFacade.createBoard(boardForm);
  }

  @GetMapping("/types")
  @HasAuthorities(UserAuthority.MANAGE_BOARDS)
  public Set<BoardAttachmentTypeDto> getBoardTypes() {
    return BoardFacade.getBoardTypes();
  }

  @GetMapping
  @FieldFilterSetting(className = Board.class, fields = "threads")
  public List<Board> getAllBoards() {
    return boardFacade.getAllBoards();
  }

  @GetMapping(Mappings.PATH_VARIABLE_BOARD)
  @FieldFilterSetting(className = Thread.class, fields = "board")
  @DynamicFilter(SensitiveDataFilter.class)
  public Board getBoard(Board board, Pageable pageRequest) {
    return boardFacade.getBoard(board, pageRequest);
  }

  @GetMapping(Mappings.PATH_VARIABLE_BOARD + "/catalog")
  @FieldFilterSetting(className = Thread.class, fields = "board")
  @DynamicFilter(SensitiveDataFilter.class)
  public Board getBoardCatalog(Board board) {
    return boardFacade.getBoardCatalog(board);
  }

  @PostMapping(Mappings.PATH_VARIABLE_BOARD + "/edit")
  @HasAuthorities(UserAuthority.MANAGE_BOARDS)
  public Board editBoard(Board oldBoard, @RequestBody @Valid BoardForm boardForm) {
    return boardFacade.editBoard(oldBoard, boardForm);
  }

  @DeleteMapping(Mappings.PATH_VARIABLE_BOARD)
  @HasAuthorities(UserAuthority.MANAGE_BOARDS)
  public ResponseEntity<?> deleteBoard(Board board) {
    boardFacade.deleteBoard(board);
    return ResponseEntity.ok().build();
  }
}