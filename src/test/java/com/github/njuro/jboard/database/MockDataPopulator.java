package com.github.njuro.jboard.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.njuro.jboard.board.Board;
import com.github.njuro.jboard.board.BoardFacade;
import com.github.njuro.jboard.post.PostForm;
import com.github.njuro.jboard.thread.Thread;
import com.github.njuro.jboard.thread.ThreadFacade;
import com.github.njuro.jboard.thread.ThreadForm;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.multipart.MultipartFile;

@DisabledIf(
    expression = "#{ systemProperties['populateMock'] == null}",
    reason = "Database populator must be enabled with -DpopulateMock flag")
@SpringBootTest
@ActiveProfiles(profiles = "dev", inheritProfiles = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class MockDataPopulator {

  @Autowired private ThreadFacade threadFacade;

  @Autowired private BoardFacade boardFacade;

  @Autowired private ObjectMapper objectMapper;

  private static final Path DATA_PATH = Paths.get("src", "test", "resources", "mockdata");
  private static final Path IMAGES_PATH = DATA_PATH.resolve("images");

  @Test
  @Commit
  @Transactional
  public void populateMockData() throws IOException {
    Board board = boardFacade.resolveBoard("fit"); // TODO create board if missing
    List<File> files =
        Files.list(DATA_PATH)
            .map(Path::toFile)
            .filter(file -> !file.isDirectory())
            .collect(Collectors.toList());

    int counter = 1;
    for (File file : files) {
      JsonNode thread = objectMapper.readTree(file);
      JsonNode posts = thread.get("posts");
      JsonNode originalPost = posts.get(0);
      ThreadForm threadForm = createThreadForm(thread, originalPost);
      Thread createdThread = threadFacade.createThread(threadForm, board);
      for (int i = 1; i < posts.size() - 1; i++) {
        PostForm postForm = createPostForm(posts.get(i));
        threadFacade.replyToThread(postForm, createdThread);
      }

      log.info(String.format("Populated thread %d of %d", counter++, files.size()));
    }
  }

  private ThreadForm createThreadForm(JsonNode thread, JsonNode originalPost) throws IOException {
    return ThreadForm.builder()
        .stickied(thread.has("stickied"))
        .subject(thread.has("subject") ? thread.get("subject").asText() : null)
        .postForm(createPostForm(originalPost))
        .build();
  }

  private PostForm createPostForm(JsonNode post) throws IOException {
    PostForm postForm =
        PostForm.builder()
            .body(post.has("body") ? post.get("body").asText() : null)
            .ip("127.0.0.1")
            .name(post.has("name") ? post.get("name").asText() : null)
            .build();

    if (post.has("attachment")) {
      JsonNode attachment = post.get("attachment");
      postForm.setAttachment(
          createAttachment(
              attachment.get("filename").asText(), attachment.get("originalFilename").asText()));
    }

    return postForm;
  }

  private MultipartFile createAttachment(String filename, String originalFilename)
      throws IOException {
    Path imagePath = IMAGES_PATH.resolve(filename);
    String contentType = Files.probeContentType(imagePath);
    if (contentType.contains("gif")) {
      imagePath =
          IMAGES_PATH.resolve(
              "giffix.gif"); // TODO temporary fix see ImageUtils##getImageFromAttachment
    }

    return new MockMultipartFile(
        originalFilename,
        originalFilename,
        contentType,
        IOUtils.toByteArray(new FileInputStream(imagePath.toFile())));
  }
}
