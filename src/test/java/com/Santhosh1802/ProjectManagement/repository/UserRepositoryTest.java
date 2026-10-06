package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User activeAdmin;
    private User activeUser;
    private User inactiveUser;
    private User unverifiedUser;

    private String testPrefix;

    @BeforeEach
    void setUp() {

        /*
         * We are using the existing PostgreSQL database.
         *
         * Do NOT call userRepository.deleteAll() here because
         * existing users may be referenced by project_members,
         * tasks, projects, etc.
         *
         * Instead, create completely unique test data for every test.
         */
        testPrefix = "repo-test-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8);

        activeAdmin = createUser(
                testPrefix + "-admin",
                "Admin",
                testPrefix + "-admin@test.com",
                UserRole.ADMIN,
                true,
                true
        );

        activeUser = createUser(
                testPrefix + "-user",
                "User",
                testPrefix + "-user@test.com",
                UserRole.USER,
                true,
                true
        );

        inactiveUser = createUser(
                testPrefix + "-inactive",
                "Inactive",
                testPrefix + "-inactive@test.com",
                UserRole.USER,
                false,
                true
        );

        unverifiedUser = createUser(
                testPrefix + "-manager",
                "Manager",
                testPrefix + "-manager@test.com",
                UserRole.MANAGER,
                true,
                false
        );

        userRepository.saveAll(List.of(
                activeAdmin,
                activeUser,
                inactiveUser,
                unverifiedUser
        ));
    }

    // ---------------------------------------------------------
    // findByEmail
    // ---------------------------------------------------------

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {

        Optional<User> result =
                userRepository.findByEmail(activeAdmin.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo(activeAdmin.getEmail());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        Optional<User> result =
                userRepository.findByEmail(
                        testPrefix + "-notfound@test.com"
                );

        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------
    // searchUsers - keyword
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldFindByFirstName() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                activeAdmin.getFirstName(),
                null,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(activeAdmin.getEmail());
    }

    @Test
    void searchUsers_shouldFindByLastName() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                "Admin",
                null,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(activeAdmin.getEmail());
    }

    @Test
    void searchUsers_shouldFindByEmail() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                testPrefix + "-manager@test",
                null,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(unverifiedUser.getEmail());
    }

    @Test
    void searchUsers_shouldBeCaseInsensitive() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                activeAdmin.getFirstName().toUpperCase(),
                null,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(activeAdmin.getEmail());
    }

    // ---------------------------------------------------------
    // searchUsers - keyword empty/null
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldIgnoreKeyword_whenKeywordIsNull() {

        Pageable pageable = PageRequest.of(0, 10);

        /*
         * We cannot assert that the entire database is returned because
         * this test runs against the existing PostgreSQL database.
         *
         * Instead, use a role filter that uniquely identifies our
         * test user. This proves that NULL keyword does not restrict
         * the result.
         */
        Page<User> result = userRepository.searchUsers(
                null,
                UserRole.ADMIN,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(activeAdmin.getEmail());
    }

    @Test
    void searchUsers_shouldIgnoreKeyword_whenKeywordIsEmpty() {

        Pageable pageable = PageRequest.of(0, 10);

        /*
         * Same idea as the NULL keyword test.
         * The role filter isolates our test data from existing users.
         */
        Page<User> result = userRepository.searchUsers(
                "",
                UserRole.MANAGER,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(unverifiedUser.getEmail());
    }

    // ---------------------------------------------------------
    // searchUsers - role filter
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldFilterByRole() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                testPrefix,
                UserRole.ADMIN,
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(activeAdmin.getEmail());
    }

    // ---------------------------------------------------------
    // searchUsers - active filter
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldFilterByActiveStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                testPrefix,
                null,
                false,
                null,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(inactiveUser.getEmail());
    }

    // ---------------------------------------------------------
    // searchUsers - email verified filter
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldFilterByEmailVerified() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                testPrefix,
                null,
                null,
                false,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(unverifiedUser.getEmail());
    }

    // ---------------------------------------------------------
    // searchUsers - multiple filters
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldApplyMultipleFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                activeUser.getFirstName(),
                UserRole.USER,
                true,
                true,
                pageable
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly(activeUser.getEmail());
    }

    @Test
    void searchUsers_shouldReturnEmpty_whenFiltersDoNotMatch() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = userRepository.searchUsers(
                activeUser.getFirstName(),
                UserRole.ADMIN,
                true,
                true,
                pageable
        );

        assertThat(result.getContent())
                .isEmpty();
    }

    // ---------------------------------------------------------
    // Pagination
    // ---------------------------------------------------------

    @Test
    void searchUsers_shouldSupportPagination() {

        /*
         * All four test users contain the same unique prefix.
         * Therefore, we can isolate exactly our four records even
         * when the real database contains other users.
         */
        Pageable pageable = PageRequest.of(0, 2);

        Page<User> result = userRepository.searchUsers(
                testPrefix,
                null,
                null,
                null,
                pageable
        );

        assertThat(result.getSize())
                .isEqualTo(2);

        assertThat(result.getNumberOfElements())
                .isEqualTo(2);

        assertThat(result.getTotalElements())
                .isEqualTo(4);

        assertThat(result.getTotalPages())
                .isEqualTo(2);
    }

    // ---------------------------------------------------------
    // Helper
    // ---------------------------------------------------------

    private User createUser(
            String firstName,
            String lastName,
            String email,
            UserRole role,
            boolean isActive,
            boolean emailVerified
    ) {

        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        /*
         * User.password is NOT NULL in the database.
         */
        user.setPassword("test-password");

        user.setUserRole(role);
        user.setIsActive(isActive);
        user.setEmailVerified(emailVerified);

        return user;
    }
}