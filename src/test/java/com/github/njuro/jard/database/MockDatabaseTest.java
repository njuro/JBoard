package com.github.njuro.jard.database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@UseMockDatabase
public class MockDatabaseTest {

  @Test
  public void testMockDatabase() {
    // empty
  }
}