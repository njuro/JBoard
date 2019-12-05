package com.github.njuro.jboard.controllers;

import com.github.njuro.jboard.config.security.SensitiveDataFilter;
import com.github.njuro.jboard.config.security.methods.HasAuthorities;
import com.github.njuro.jboard.facades.BoardFacade;
import com.github.njuro.jboard.helpers.Mappings;
import com.github.njuro.jboard.models.Board;
import com.github.njuro.jboard.models.dto.BoardAttachmentTypeDto;
import com.github.njuro.jboard.models.dto.forms.BoardForm;
import com.github.njuro.jboard.models.enums.UserAuthority;
import com.jfilter.filter.DynamicFilter;
import com.jfilter.filter.FieldFilterSetting;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Mappings.API_ROOT_BOARDS)
public class BoardRestController {

  private final BoardFacade boardFacade;

  @Autowired
  public BoardRestController(final BoardFacade boardFacade) {
    this.boardFacade = boardFacade;
  }

  @PostMapping
  @HasAuthorities(UserAuthority.MANAGE_BOARDS)
  public Board createBoard(@RequestBody @Valid final BoardForm boardForm) {
    return this.boardFacade.createBoard(boardForm);
  }

  @GetMapping("/types")
  @HasAuthorities(UserAuthority.MANAGE_BOARDS)
  public Set<BoardAttachmentTypeDto> getBoardTypes() {
    return BoardFacade.getBoardTypes();
  }

  @GetMapping
  @FieldFilterSetting(className = Board.class, fields = "threads")
  public List<Board> showAllBoards() {
    return this.boardFacade.getAllBoards();
  }

  @GetMapping(Mappings.PATH_VARIABLE_BOARD)
  @DynamicFilter(SensitiveDataFilter.class)
  public Board showBoard(final Board board) {
    return board;
  }
}