package com.github.njuro.jboard.facades;

import com.github.njuro.jboard.controllers.validation.FormValidationException;
import com.github.njuro.jboard.models.Post;
import com.github.njuro.jboard.models.Thread;
import com.github.njuro.jboard.models.dto.forms.PostForm;
import com.github.njuro.jboard.models.dto.forms.ThreadForm;
import com.github.njuro.jboard.services.BanService;
import com.github.njuro.jboard.services.PostService;
import com.github.njuro.jboard.services.ThreadService;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThreadFacade {

  private final ThreadService threadService;
  private final PostService postService;
  private final BanService banService;

  private final PostFacade postFacade;

  @Autowired
  public ThreadFacade(
      final ThreadService threadService,
      final PostService postService,
      final BanService banService,
      final PostFacade postFacade) {
    this.threadService = threadService;
    this.postService = postService;
    this.banService = banService;
    this.postFacade = postFacade;
  }

  public Thread submitNewThread(@NotNull final ThreadForm threadForm) {
    if (this.banService.hasActiveBan(threadForm.getPostForm().getIp())) {
      throw new FormValidationException("Your IP address is banned");
    }

    final Thread thread = threadForm.toThread();
    thread.setOriginalPost(this.postFacade.createPost(threadForm.getPostForm(), thread));
    thread.setLastReplyAt(LocalDateTime.now());

    return this.threadService.saveThread(thread);
  }

  public Post replyToThread(@NotNull final PostForm postForm, final Thread thread) {
    if (this.banService.hasActiveBan(postForm.getIp())) {
      throw new FormValidationException("Your IP address is banned");
    }

    if (thread.isLocked()) {
      throw new FormValidationException("Thread is locked");
    }

    Post post = this.postFacade.createPost(postForm, thread);
    post = this.postService.savePost(post);
    this.threadService.updateLastReplyTimestamp(thread); // TODO move to PostService##savePost

    return post;
  }

  public List<Post> findNewPosts(final Thread thread, final Long lastPostNumber) {
    return this.postService.getNewRepliesForThreadSince(thread, lastPostNumber);
  }

  public Thread toggleSticky(final Thread thread) {
    thread.toggleSticky();
    return this.threadService.updateThread(thread);
  }

  public Thread toggleLock(final Thread thread) {
    thread.toggleLock();
    return this.threadService.updateThread(thread);
  }

  public void deletePost(final Thread thread, final Post post) {
    if (thread.getOriginalPost().equals(post)) {
      // delete whole thread
      this.threadService.deleteThread(thread);
    } else {
      // delete post
      this.postService.deletePost(post);
    }
  }

  public Thread getFullThread(final Thread thread) {
    final List<Post> replies = this.postService.getAllRepliesForThread(thread);
    thread.setReplies(replies);
    return thread;
  }
}
