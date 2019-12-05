package com.github.njuro.jboard.models.dto.forms;

import static com.github.njuro.jboard.helpers.Constants.MAX_SUBJECT_LENGTH;

import com.github.njuro.jboard.models.Board;
import com.github.njuro.jboard.models.Thread;
import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Data transfer object for "submit new thread" form
 *
 * @author njuro
 */
@Data
public class ThreadForm {

  @NotNull private Board board;

  @Size(
      max = MAX_SUBJECT_LENGTH,
      message = "Subject too long (allowed " + MAX_SUBJECT_LENGTH + " chars)")
  private String subject;

  private boolean stickied;
  private boolean locked;

  @Valid @NotNull private PostForm postForm;

  @AssertFalse(message = "First post must have non-empty subject or non-empty body")
  public boolean isEmptySubjectAndComment() {
    return (this.subject == null || this.subject.trim().isEmpty())
        && (this.postForm.getBody() == null || this.postForm.getBody().trim().isEmpty());
  }

  @AssertTrue(message = "First post must have an attachment")
  public boolean isUploadedAttachment() {
    return this.postForm.getAttachment().getSize() > 0;
  }

  public Thread toThread() {
    return Thread.builder()
        .subject(this.subject)
        .locked(this.locked)
        .stickied(this.stickied)
        .board(this.board)
        .build();
  }
}