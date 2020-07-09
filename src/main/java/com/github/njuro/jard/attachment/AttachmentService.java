package com.github.njuro.jard.attachment;

import static com.github.njuro.jard.common.Constants.DEFAULT_THUMBNAIL_EXTENSION;
import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR_STR;

import com.github.njuro.jard.attachment.helpers.AttachmentImageUtils;
import com.github.njuro.jard.attachment.helpers.AttachmentMetadataUtils;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@Slf4j
public class AttachmentService {

  /**
   * Active storage mode.
   *
   * @see UserContentStorageMode
   */
  @Value("${app.user.content.storage:LOCAL}")
  private UserContentStorageMode storageMode;

  private final AmazonS3FileService amazonS3FileService;
  private final AttachmentRepository attachmentRepository;

  @Autowired
  public AttachmentService(
      AmazonS3FileService amazonS3FileService, AttachmentRepository attachmentRepository) {
    this.amazonS3FileService = amazonS3FileService;
    this.attachmentRepository = attachmentRepository;
  }

  /**
   * Saves {@link Attachment} and its retrieved {@link AttachmentMetadata} to database and stores
   * its file (specific way of storing the file is determined by active {@link
   * UserContentStorageMode}).
   *
   * <p>Depending on category of the attachment also creates and stores its thumbnail.
   *
   * @param attachment attachment to be saved
   * @param source uploaded file
   * @return saved {@link Attachment}
   * @throws IOException if storing to local filesystem fails
   * @throws IllegalArgumentException if something goes wrong during setting of metadata or
   *     uploading to remote server
   * @throws NullPointerException if one of the parameters is {@code null}
   */
  public Attachment saveAttachment(Attachment attachment, MultipartFile source) throws IOException {
    Objects.requireNonNull(attachment, "Attachment cannot be null");
    Objects.requireNonNull(source, "Source file cannot be null");

    //noinspection ResultOfMethodCallIgnored
    attachment.getFile().getParentFile().mkdirs();
    source.transferTo(attachment.getFile());
    AttachmentMetadataUtils.setMetadata(attachment);

    if (storageMode == UserContentStorageMode.AMAZON_S3) {
      String url =
          amazonS3FileService.uploadFile(
              attachment.getFolder(), attachment.getFilename(), attachment.getFile());
      attachment.setAmazonS3Url(url);
    }

    if (attachment.getCategory().hasThumbnail()) {
      saveAttachmentThumbnail(attachment);
    }

    attachment.getMetadata().setAttachment(attachment);
    return attachmentRepository.save(attachment);
  }

  /**
   * Creates and stores thumbnail of given attachment (specific way of storing the file is
   * determined by active {@link UserContentStorageMode}).
   *
   * @param attachment attachment to create and store thumbnail for
   * @throws IOException if storing to local filesystem fails
   * @throws IllegalArgumentException if something goes wrong during setting of metadata or
   *     uploading to remote server
   */
  private void saveAttachmentThumbnail(Attachment attachment) throws IOException {
    String extension =
        attachment.getCategory() == AttachmentCategory.IMAGE
            ? FilenameUtils.getExtension(attachment.getFilename())
            : DEFAULT_THUMBNAIL_EXTENSION;
    attachment.setThumbnailFilename(
        FilenameUtils.removeExtension(attachment.getFilename())
            + EXTENSION_SEPARATOR_STR
            + extension);
    attachment.getThumbnailFile().getParentFile().mkdirs();
    RenderedImage thumbnail = AttachmentImageUtils.createThumbnail(attachment);

    ImageIO.write(thumbnail, extension, attachment.getThumbnailFile());

    if (storageMode == UserContentStorageMode.AMAZON_S3) {
      String url =
          amazonS3FileService.uploadFile(
              attachment.getThumbnailFolder(),
              attachment.getFilename(),
              attachment.getThumbnailFile());
      attachment.setAmazonS3ThumbnailUrl(url);
    }
  }

  /**
   * Deletes attachment's file.
   *
   * @param attachment attachment, which file to delete
   * @throws IOException if deleting from local filesystem fails
   * @throws IllegalArgumentException if deleting from remote server fails
   * @throws NullPointerException if attachment is {@code null}
   */
  public void deleteAttachmentFile(Attachment attachment) throws IOException {
    Objects.requireNonNull(attachment, "Attachment cannot be null");

    if (storageMode == UserContentStorageMode.AMAZON_S3) {
      amazonS3FileService.deleteFile(attachment.getFolder(), attachment.getFilename());
      amazonS3FileService.deleteFile(attachment.getThumbnailFolder(), attachment.getFilename());
    }

    if (!attachment.getFile().delete()) {
      throw new IOException("Failed to delete attachment file");
    }

    if (!attachment.getThumbnailFile().delete()) {
      throw new IOException("Failed to delete attachment thumbnail");
    }
  }

  /**
   * Deletes files of given attachments.
   *
   * @param attachments list of attachments which files to delete
   * @throws IOException if deleting from local filesystem fails
   * @throws IllegalArgumentException if deleting from remote server fails
   * @throws NullPointerException if attachment list is {@code null}
   */
  public void deleteAttachmentFiles(List<Attachment> attachments) throws IOException {
    Objects.requireNonNull(attachments, "Attachment list cannot be null");
    for (Attachment attachment : attachments) {
      deleteAttachmentFile(attachment);
    }
  }
}