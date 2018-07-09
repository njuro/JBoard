package com.github.njuro.controllers;

import com.github.njuro.exceptions.ThreadNotFoundException;
import com.github.njuro.models.Board;
import com.github.njuro.models.Post;
import com.github.njuro.models.Thread;
import com.github.njuro.models.dto.PostForm;
import com.github.njuro.models.dto.ThreadForm;
import com.github.njuro.models.enums.UserRole;
import com.github.njuro.services.BoardService;
import com.github.njuro.services.PostService;
import com.github.njuro.services.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * Controller for threads
 *
 * @author njuro
 */

@Controller
@RequestMapping(value = "/board/{board}")
public class ThreadController {

    private final PostService postService;

    private final ThreadService threadService;

    private final BoardService boardService;

    @Autowired
    public ThreadController(PostService postService, ThreadService threadService, BoardService boardService) {
        this.postService = postService;
        this.threadService = threadService;
        this.boardService = boardService;
    }

    @PostMapping("/submit")
    public String submitThread(@PathVariable("board") String boardLabel,
                               @Valid @ModelAttribute(name = "threadForm") ThreadForm threadForm,
                               BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.threadForm", result);
            redirectAttributes.addFlashAttribute("threadForm", threadForm);
            return "redirect:/board/" + boardLabel;
        }

        Board board = boardService.getBoard(boardLabel);
        Thread thread = threadService.createThread(threadForm, board);
        threadService.saveThread(thread);

        return "redirect:/board/" + boardLabel + "/" + thread.getOriginalPost().getPostNumber();
    }

    @PostMapping("/{threadNo}/reply")
    public String replyToThread(@PathVariable("board") String boardLabel,
                                @PathVariable("threadNo") Long threadNumber,
                                @Valid @ModelAttribute(name = "postForm") PostForm postForm,
                                BindingResult result, RedirectAttributes redirectAttributes) {

        Board board = boardService.getBoard(boardLabel);
        Thread thread = threadService.getThread(board, threadNumber);

        if (thread.isLocked()) {
            result.reject("thread.locked", "Thread is locked");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.postForm", result);
            redirectAttributes.addFlashAttribute("postForm", postForm);
            redirectAttributes.addFlashAttribute("threadNo", threadNumber);
            return "redirect:/board/" + boardLabel + "/" + threadNumber;
        }



        Post post = postService.createPost(postForm, board);
        post.setThread(thread);
        postService.savePost(post, board);

        return "redirect:/board/" + boardLabel + "/" + threadNumber;
    }

    @Secured(UserRole.Roles.MODERATOR_ROLE)
    @PostMapping("/{threadNo}/sticky")
    public String toggleStickyThread(@PathVariable("board") String boardLabel,
                                     @PathVariable("threadNo") Long threadNumber) {

        Board board = boardService.getBoard(boardLabel);

        Thread thread = threadService.getThread(board, threadNumber);
        thread.setStickied(!thread.isStickied());
        threadService.updateThread(thread);

        return "redirect:/board/" + boardLabel;
    }

    @Secured(UserRole.Roles.JANITOR_ROLE)
    @PostMapping("/{threadNo}/lock")
    public String toggleLockThread(@PathVariable("board") String boardLabel,
                                   @PathVariable("threadNo") Long threadNumber) {

        Board board = boardService.getBoard(boardLabel);

        Thread thread = threadService.getThread(board, threadNumber);
        thread.setLocked(!thread.isLocked());
        threadService.updateThread(thread);

        return "redirect:/board/" + boardLabel;
    }

    @GetMapping("/{threadNo}")
    public String viewThread(@PathVariable("board") String boardLabel,
                             @PathVariable("threadNo") Long threadNumber, Model model) {
        Board board = boardService.getBoard(boardLabel);
        Thread thread = threadService.getThread(board, threadNumber);

        if (thread == null) {
            throw new ThreadNotFoundException();
        }

        model.addAttribute("label", boardLabel);
        model.addAttribute("thread", thread);
        model.addAttribute("title", String.format("/%s/", board.getLabel(), thread.getSubject()));

        return "fragments/thread";
    }


}
