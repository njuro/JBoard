package com.github.njuro.jard.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.njuro.jard.attachment.AttachmentCategory.AttachmentCategorySerializer;
import com.github.njuro.jard.common.Constants;
import com.github.njuro.jard.post.Post;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Entity representing an attachment to {@link Post}. */
@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Attachment implements Serializable {

  private static final long serialVersionUID = -751675348099883626L;

  /** Unique identifier of this attachment. */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(updatable = false)
  @JsonIgnore
  private UUID id;

  /**
   * Category this attachment belongs to.
   *
   * @see AttachmentCategory
   */
  @Enumerated(EnumType.STRING)
  @JsonSerialize(using = AttachmentCategorySerializer.class)
  @ToString.Include
  private AttachmentCategory category;

  /** Parent folder(s) this attachment is stored in. Example: {@code /foo/pol/}. */
  @Basic @EqualsAndHashCode.Include @ToString.Include private String folder;

  /** Filename given to the attachment's file by the poster. */
  @Basic
  @Column(nullable = false)
  @ToString.Include
  private String originalFilename;

  /** Filename generated by the system. The file will be stored under this name. */
  @Column(unique = true, nullable = false)
  @EqualsAndHashCode.Include
  @ToString.Include
  private String filename;

  /** (Optional) name of stored thumbnail for this attachment's file. */
  @Column(unique = true)
  @EqualsAndHashCode.Include
  private String thumbnailFilename;

  /** (Optional) shareable url to this attachment's file in Amazon S3 bucket. */
  private String amazonS3Url;

  /** (Optional) shareable url to thumbnail for this attachment's file in Amazon S3 bucket. */
  private String amazonS3ThumbnailUrl;

  /**
   * Metadata for this attachment.
   *
   * @see AttachmentMetadata
   */
  @OneToOne(cascade = CascadeType.ALL, mappedBy = "attachment", optional = false)
  @Builder.Default
  private AttachmentMetadata metadata = new AttachmentMetadata();

  /** @return pointer to this attachment's file on local filesystem */
  public File getFile() {
    return Constants.USER_CONTENT_PATH.resolve(Paths.get(folder, filename)).toFile();
  }

  /**
   * @return pointer to thumbnail for this attachment's file on local filesystem. If thumbnail does
   *     not exist, returns {@code null}.
   */
  public File getThumbnailFile() {
    if (thumbnailFilename == null) {
      return null;
    }

    return Constants.USER_CONTENT_PATH
        .resolve(Paths.get(getThumbnailFolder(), thumbnailFilename))
        .toFile();
  }

  /**
   * @return parent folder(s) thumbnail for this attachment's file is stored in. If thumbnail does
   *     not exist, returns {@code null}.
   */
  @JsonProperty("thumbnailFolder")
  public String getThumbnailFolder() {
    if (thumbnailFilename == null) {
      return null;
    }

    return Paths.get(folder, "thumbs").toString();
  }
}
