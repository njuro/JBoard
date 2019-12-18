package com.github.njuro.jboard.attachment;

import static com.github.njuro.jboard.common.Constants.IMAGE_MAX_THUMB_HEIGHT;
import static com.github.njuro.jboard.common.Constants.IMAGE_MAX_THUMB_WIDTH;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;

/**
 * Utility methods for working with image attachments
 *
 * @author njuro
 */
@UtilityClass
public class ImageUtils {

  /**
   * Sets dimension of a image attachment and also calculates thumbnail dimensions
   *
   * @param att attachment
   */
  public void setDimensions(Attachment att) {
    BufferedImage img = getImageFromAttachment(att);
    if (img == null) {
      return;
    }

    att.setWidth(img.getWidth());
    att.setHeight(img.getHeight());

    setThumbnailDimensions(att);
  }

  /**
   * Attempts to retrieve image data from image attachment
   *
   * @param att image attachment
   * @return image, or null if attachment does not have file associated with it
   * @throws IllegalArgumentException when reading image data fails
   */
  private BufferedImage getImageFromAttachment(Attachment att) {
    if (att.getFile() == null) {
      return null;
    }

    try {
      return ImageIO.read(att.getFile());
    } catch (IOException e) {
      throw new IllegalArgumentException("Error while reading image", e);
    }
  }

  /**
   * Calculates thumbnail dimensions for image attachments.
   *
   * @att image attachment
   */
  private void setThumbnailDimensions(Attachment att) {
    Objects.requireNonNull(att);

    if (att.getWidth() == 0 || att.getHeight() == 0) {
      // set real dimensions first
      setDimensions(att);
    }

    if (att.getWidth() > IMAGE_MAX_THUMB_WIDTH || att.getHeight() > IMAGE_MAX_THUMB_HEIGHT) {
      double factor =
          Math.min(
              IMAGE_MAX_THUMB_WIDTH / att.getWidth(), IMAGE_MAX_THUMB_HEIGHT / att.getHeight());
      att.setThumbWidth(((int) Math.ceil(att.getWidth() * factor)) + 1);
      att.setThumbHeight(((int) Math.ceil(att.getHeight() * factor)) + 1);
    } else {
      // thumbnail dimensions are the same as real dimensions
      att.setThumbHeight(att.getHeight());
      att.setThumbWidth(att.getWidth());
    }
  }
}