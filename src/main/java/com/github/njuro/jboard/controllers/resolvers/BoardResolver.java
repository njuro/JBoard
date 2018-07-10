package com.github.njuro.jboard.controllers.resolvers;

import com.github.njuro.jboard.models.Board;
import com.github.njuro.jboard.services.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver for mapping board label (path variable) to respective {@link Board} object
 *
 * @author njuro
 */
@Component
public class BoardResolver implements PathVariableArgumentResolver {

    private final BoardService boardService;

    @Autowired
    public BoardResolver(BoardService boardService) {
        this.boardService = boardService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Board.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String label = getPathVariable("board", webRequest);

        return boardService.resolveBoard(label);
    }
}
