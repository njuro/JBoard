package com.github.njuro.jard.user

import com.github.njuro.jard.MockMvcTest
import com.github.njuro.jard.TestDataRepository
import com.github.njuro.jard.WithContainerDatabase
import com.github.njuro.jard.WithMockJardUser
import com.github.njuro.jard.common.InputConstraints.MAX_USERNAME_LENGTH
import com.github.njuro.jard.common.Mappings
import com.github.njuro.jard.config.security.captcha.CaptchaProvider
import com.github.njuro.jard.forgotPasswordRequest
import com.github.njuro.jard.passwordEdit
import com.github.njuro.jard.randomString
import com.github.njuro.jard.resetPasswordRequest
import com.github.njuro.jard.security.captcha.MockCaptchaVerificationResult
import com.github.njuro.jard.toForm
import com.github.njuro.jard.user
import com.github.njuro.jard.user.dto.CurrentUserEditDto
import com.github.njuro.jard.user.dto.CurrentUserPasswordEditDto
import com.github.njuro.jard.user.dto.UserDto
import com.github.njuro.jard.user.dto.UserForm
import com.github.njuro.jard.user.token.UserTokenType
import com.github.njuro.jard.userEdit
import com.github.njuro.jard.userToken
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.optional.shouldNotBePresent
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@WithContainerDatabase
@Transactional
internal class UserIntegrationTest : MockMvcTest() {

    @MockkBean
    private lateinit var captchaProvider: CaptchaProvider

    @Autowired
    private lateinit var db: TestDataRepository

    @Nested
    @DisplayName("create user")
    @WithMockJardUser(UserAuthority.MANAGE_USERS)
    inner class CreateUser {
        private fun createUser(userForm: UserForm) = mockMvc.post(Mappings.API_ROOT_USERS) { body(userForm) }

        @Test
        fun `create valid user`() {
            createUser(user().toForm()).andExpect { status { isCreated() } }.andReturnConverted<UserDto>()
                .shouldNotBeNull()
        }

        @Test
        fun `don't create invalid user`() {
            createUser(user(username = randomString(MAX_USERNAME_LENGTH + 1)).toForm()).andExpect { status { isBadRequest() } }
        }
    }

    @Test
    @WithMockJardUser(UserAuthority.MANAGE_USERS)
    fun `get all users`() {
        (1..3).forEach { db.insert(user(username = "User $it", email = "user$it@mail.com")) }

        mockMvc.get(Mappings.API_ROOT_USERS) { setUp() }.andExpect { status { isOk() } }
            .andReturnConverted<List<UserDto>>() shouldHaveSize 3
    }

    @Nested
    @DisplayName("create user")
    inner class GetCurrentUser {
        private fun getCurrentUser() = mockMvc.get("${Mappings.API_ROOT_USERS}/current") { setUp() }

        @Test
        @WithMockJardUser
        fun `get current user when someone is logged in`() {
            getCurrentUser().andExpect { status { isOk() } }.andReturnConverted<UserDto>().shouldNotBeNull()
        }

        @Test
        fun `get current user when nobody is logged in`() {
            getCurrentUser().andExpect { status { isOk() } }.andReturn().response.contentLength shouldBe 0
        }
    }

    @Nested
    @DisplayName("edit user")
    @WithMockJardUser(UserAuthority.MANAGE_USERS)
    inner class EditUser {
        private fun editUser(username: String, editForm: UserForm) =
            mockMvc.put("${Mappings.API_ROOT_USERS}/$username") { body(editForm) }

        @Test
        fun `edit user`() {
            val user = db.insert(user(role = UserRole.MODERATOR))

            editUser(user.username, user.toForm().apply { role = UserRole.ADMIN }).andExpect { status { isOk() } }
                .andReturnConverted<UserDto>().role shouldBe UserRole.ADMIN
        }

        @Test
        fun `don't edit non-existing user`() {
            editUser("xxx", user().toForm()).andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("edit current user")
    inner class EditUserCurrentUser {
        private fun editCurrentUser(userEdit: CurrentUserEditDto) =
            mockMvc.patch("${Mappings.API_ROOT_USERS}/current") { body(userEdit) }

        @Test
        @WithMockJardUser(email = "old@mail.com")
        fun `edit current user when user is authenticated`() {
            editCurrentUser(userEdit("new@mail.com")).andExpect { status { isOk() } }.andReturnConverted<UserDto>()
                .shouldNotBeNull()
        }

        @Test
        fun `don't edit current user when user is not authenticated`() {
            editCurrentUser(userEdit("new@mail.com")).andExpect { status { isBadRequest() } }
        }
    }

    @Nested
    @DisplayName("edit current user password")
    inner class EditUserCurrentUserPassword {
        private fun editCurrentUserPassword(passwordEdit: CurrentUserPasswordEditDto) =
            mockMvc.patch("${Mappings.API_ROOT_USERS}/current/password") { body(passwordEdit) }

        @Test
        @WithMockJardUser(password = "\$2b\$31\$Pr0po9XrlgIzkeUMCeheFOXJiVd/K.ISm0ra4SGAHgpWxY6b4CZaS") // nasty hack
        fun `edit user password when user is authenticated`() {
            editCurrentUserPassword(passwordEdit("oldPassword", "newPassword")).andExpect { status { isOk() } }
        }

        @Test
        fun `don't edit user password when user is not authenticated`() {
            editCurrentUserPassword(
                passwordEdit(
                    "oldPassword",
                    "newPassword"
                )
            ).andExpect { status { isBadRequest() } }
        }
    }

    @Test
    fun `forgot password`() {
        val user = db.insert(user(username = "user", email = "user@email.com"))
        every { captchaProvider.verifyCaptchaToken(any()) } returns MockCaptchaVerificationResult.VALID

        mockMvc.post("${Mappings.API_ROOT_USERS}/forgot-password") { body(forgotPasswordRequest(user.username)) }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `reset password`() {
        val user = db.insert(user(username = "user", email = "user@email.com"))
        val token = db.insert(userToken(user, "abcde", UserTokenType.PASSWORD_RESET))

        mockMvc.post("${Mappings.API_ROOT_USERS}/reset-password") {
            body(resetPasswordRequest(token = token.value, password = "newPassword"))
        }.andExpect { status { isOk() } }
    }

    @Test
    @WithMockJardUser(UserAuthority.MANAGE_USERS)
    fun `delete user`() {
        val user = db.insert(user())

        mockMvc.delete("${Mappings.API_ROOT_USERS}/${user.username}") { setUp() }.andExpect { status { isOk() } }
        db.select(user).shouldNotBePresent()
    }
}
