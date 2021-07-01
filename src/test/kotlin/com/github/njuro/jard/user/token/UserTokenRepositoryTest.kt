package com.github.njuro.jard.user.token

import com.github.njuro.jard.WithContainerDatabase
import com.github.njuro.jard.user
import com.github.njuro.jard.user.User
import com.github.njuro.jard.user.UserRepository
import com.github.njuro.jard.userToken
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.optional.shouldNotBePresent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.OffsetDateTime

@DataJpaTest
@WithContainerDatabase
internal class UserTokenRepositoryTest {

    @Autowired
    private lateinit var userTokenRepository: UserTokenRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user1: User

    private lateinit var user2: User

    @BeforeEach
    fun setUp() {
        user1 = userRepository.save(user(username = "user1", email = "user1@mail.com"))
        user2 = userRepository.save(user(username = "user2", email = "user2@mail.com"))
    }

    @Test
    fun `find by user and type`() {
        val token1 = userTokenRepository.save(userToken(user1, "aaa", type = UserTokenType.EMAIL_VERIFICATION))
        val token2 = userTokenRepository.save(userToken(user2, "bbb", type = UserTokenType.PASSWORD_RECOVERY))
        val token3 = userTokenRepository.save(userToken(user1, "ccc", type = UserTokenType.PASSWORD_RECOVERY))

        userTokenRepository.findByUserAndType(user1, UserTokenType.PASSWORD_RECOVERY)
            .shouldBePresent { it.value shouldBe token3.value }
        userTokenRepository.findByUserAndType(user1, UserTokenType.EMAIL_VERIFICATION)
            .shouldBePresent { it.value shouldBe token1.value }
        userTokenRepository.findByUserAndType(user2, UserTokenType.PASSWORD_RECOVERY)
            .shouldBePresent { it.value shouldBe token2.value }
        userTokenRepository.findByUserAndType(user2, UserTokenType.EMAIL_VERIFICATION).shouldNotBePresent()
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `find by user and type and value`() {
        val token1 = userTokenRepository.save(userToken(user1, "aaa", type = UserTokenType.EMAIL_VERIFICATION))
        val token2 = userTokenRepository.save(userToken(user2, "bbb", type = UserTokenType.PASSWORD_RECOVERY))
        val token3 = userTokenRepository.save(userToken(user1, "ccc", type = UserTokenType.PASSWORD_RECOVERY))

        userTokenRepository.findByUserAndValueAndType(user2, token2.value, UserTokenType.PASSWORD_RECOVERY)
            .shouldBePresent()
        userTokenRepository.findByUserAndValueAndType(user2, "xxx", UserTokenType.PASSWORD_RECOVERY)
            .shouldNotBePresent()
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `delete expired tokens`() {
        val baseDate = OffsetDateTime.now()
        val token1 = userTokenRepository.save(userToken(user1, "aaa", expirationAt = baseDate.plusDays(1)))
        val token2 = userTokenRepository.save(userToken(user2, "bbb", expirationAt = baseDate.minusDays(1)))
        val token3 = userTokenRepository.save(userToken(user1, "ccc", expirationAt = baseDate.plusDays(2)))

        userTokenRepository.deleteByExpirationAtBefore(baseDate)
        userTokenRepository.findAll().map(UserToken::getValue)
            .shouldContainExactlyInAnyOrder(token1.value, token3.value)
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `delete tokens by user and type`() {
        val token1 = userTokenRepository.save(userToken(user1, "aaa", type = UserTokenType.EMAIL_VERIFICATION))
        val token2 = userTokenRepository.save(userToken(user2, "bbb", type = UserTokenType.PASSWORD_RECOVERY))
        val token3 = userTokenRepository.save(userToken(user1, "ccc", type = UserTokenType.PASSWORD_RECOVERY))

        userTokenRepository.deleteByUserAndType(user1, UserTokenType.PASSWORD_RECOVERY)
        userTokenRepository.findAll().map(UserToken::getValue)
            .shouldContainExactlyInAnyOrder(token1.value, token2.value)
    }
}