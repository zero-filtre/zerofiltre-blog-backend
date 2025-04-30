package tech.zerofiltre.blog.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataCheckerTest {

    private static User adminUser;
    private static User userWithUserRole;
    private static LinkCompanyUser linkCompanyUserRoleAdmin;
    private static LinkCompanyUser linkCompanyUserRoleEditor;
    private static LinkCompanyUser linkCompanyUserRoleViewer;

    DataChecker checker;

    @Mock
    UserProvider userProvider;

    @Mock
    CourseProvider courseProvider;

    @Mock
    CompanyProvider companyProvider;

    @Mock
    CompanyUserProvider companyUserProvider;

    @BeforeAll
    static void setup() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.getRoles().add("ROLE_ADMIN");

        userWithUserRole = new User();
        userWithUserRole.setId(1L);
        userWithUserRole.getRoles().add("ROLE_USER");

        linkCompanyUserRoleAdmin = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        linkCompanyUserRoleEditor = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null);
        linkCompanyUserRoleViewer = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null);
    }

    @BeforeEach
    void init() {
        checker = new DataChecker(userProvider, courseProvider, companyProvider, companyUserProvider);
    }

    @Test
    @DisplayName("When I check the existence of an existing user, no exception is thrown.")
    void shouldNotThrowException_whenCheckUserExistence_ofExistingUser() throws ResourceNotFoundException {
        //GIVEN
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(new User()));

        //WHEN
        checker.checkUserExistence(2L);

        //THEN
        verify(userProvider).userOfId(anyLong());
    }

    @Test
    @DisplayName("When I check the existence of a non-existent user, a resource not found exception is thrown.")
    void shouldThrowException_whenCheckUserExistence_ofNonExistingUser() {
        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.checkUserExistence(2L));
    }

    @Test
    @DisplayName("When I check the existence of an existing course, no exception is thrown.")
    void shouldNotThrowException_whenCheckCourseExistence_ofExistingCourse() throws ResourceNotFoundException {
        //GIVEN
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //WHEN
        checker.checkCourseExistence(course.getId());

        //THEN
        verify(courseProvider).courseOfId(anyLong());
    }

    @Test
    @DisplayName("When I check the existence of a non-existent course, a resource not found exception is thrown.")
    void shouldThrowException_whenCheckCourseExistence_ofNotExistingCourse() {
        //GIVEN
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.checkCourseExistence(2L));
    }

    @Test
    @DisplayName("When I check the existence of an existing company, no exception is thrown.")
    void shouldNotThrowException_whenCheckCompanyExistence_ofExistingCompany() throws ResourceNotFoundException {
        //GIVEN
        when(companyProvider.findById(anyLong())).thenReturn(Optional.of(new Company()));

        //WHEN
        checker.checkCompanyExistence(1L);

        //THEN
        verify(companyProvider).findById(anyLong());
    }

    @Test
    @DisplayName("When I check the existence of a non-existent company, a resource not found exception is thrown.")
    void shouldThrowException_whenCheckCompanyExistence_ofNotExistingCompany() {
        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.checkCompanyExistence(1L));
    }

    @Test
    @DisplayName("When I check whether a user with the role of user is an admin platform, a forbidden action exception is thrown.")
    void shouldThrowException_whenCheckAdminUserExistence_ofNonIfAdminUser() {
        //GIVEN
        User user = new User();
        user.getRoles().add("ROLE_USER");

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.checkIfAdminUser(user));
    }

    @Test
    @DisplayName("When an user is an admin platform and I check that he is an admin or a company admin, no exception is thrown.")
    void shouldNotThrowException_whenCheckAdminOrCompanyIfAdmin_asPlatformAdmin() throws ForbiddenActionException {
        //WHEN
        checker.checkIfAdminOrCompanyAdmin(adminUser, 1L);

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When an company admin is a non-admin platform and I check that he is an admin or a company admin, no exception is thrown.")
    void shouldNotThrowException_whenCheckAdminOrCompanyIfAdmin_asCompanyAdmin() throws ForbiddenActionException {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(true).when(spy).isCompanyAdmin(anyLong(), anyLong());

        //WHEN
        spy.checkIfAdminOrCompanyAdmin(userWithUserRole, 1L);

        //THEN
        verify(spy).isCompanyAdmin(anyLong(), anyLong());
    }

    @DisplayName("When a user is a company editor or viewer or non-exitent and I check that he is an admin or a company admin, then a forbidden action exception is thrown.")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("companyUserWithBadRoleList")
    void shouldThrowException_whenCheckAdminOrCompanyIfAdmin_asCompanyEditorOrViewerOrNotExistingUser(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(companyUser);

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.checkIfAdminOrCompanyAdmin(user, 1L));
    }

    @Test
    @DisplayName("When a user is a platform admin and I check that he is a platform admin or a company user, no exception is thrown.")
    void shouldNotThrowException_whenCheckAdminOrCompanyUser_asPlatformIfAdmin() throws ForbiddenActionException {
        //WHEN
        checker.checkIfAdminOrCompanyUser(adminUser, 1L);

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When an user is a company user and I check that he is a platform admin or a company user, no exception is thrown.")
    void shouldNotThrowException_whenCheckIfAdminOrCompanyUser_asCompanyUser() throws ForbiddenActionException {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(true).when(spy).isCompanyUser(anyLong(), anyLong());

        //WHEN
        spy.checkIfAdminOrCompanyUser(userWithUserRole, 1L);

        //THEN
        verify(spy).isCompanyUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a user is not a platform admin and is not part of the company, and I check whether the user is a platform admin or part of the company, a forbidden action exception is thrown.")
    void shouldThrownException_whenCheckAdminOrCompanyUser_asNotPlatformIfAdminOrCompanyUser() {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(false).when(spy).isCompanyUser(anyLong(), anyLong());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> spy.checkIfAdminOrCompanyUser(new User(), 1L));
        verify(spy).isCompanyUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a user is an admin platform and I check that he is an admin or a company admin or editor, no exception is thrown.")
    void shouldNotThrowException_whenCheckAdminOrCompanyIfAdminOrEditor_asAdminPlatform() throws ForbiddenActionException {
        //GIVEN
        //WHEN
        checker.checkIfAdminOrCompanyAdminOrEditor(ZerofiltreUtilsTest.createMockUser(true), 1L);

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When a user is a company admin or editor and I check that he is an admin or a company admin or editor, no exception is thrown.")
    void shouldNotThrowException_whenCheckAdminOrCompanyIfAdminOrEditor_asCompanyIfAdminOrEditor() throws ForbiddenActionException {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(true).when(spy).isCompanyAdminOrEditor(anyLong(), anyLong());

        //WHEN
        spy.checkIfAdminOrCompanyAdminOrEditor(new User(), 1L);

        //THEN
        verify(spy).isCompanyAdminOrEditor(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a user is a company viewer and I check that he is an admin or a company admin or editor, a forbidden action exception is thrown.")
    void shouldThrowException_whenCheckAdminOrCompanyIfAdminOrEditor_asCompanyViewer() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyUserRoleViewer));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.checkIfAdminOrCompanyAdminOrEditor(new User(), 1L));
    }

    @Test
    @DisplayName("When a user is a non platform admin or company user and I check that he is an admin or a company admin or editor, a forbidden action exception is thrown.")
    void shouldThrowException_whenCheckAdminOrCompanyAdminOrEditor_asNonPlatformIfIfAdminOrCompanyUser() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.checkIfAdminOrCompanyAdminOrEditor(new User(), 1L));
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @DisplayName("When a user is a company admin or editor and I verify that a user is a company admin or editor, true is returned.")
    @ParameterizedTest(name = "[{index}] - role {1} is a company admin or editor: {2}")
    @MethodSource("companyUserList")
    void shouldReturnTrue_whenCheckCompanyAdminOrEditor_asCompanyAdminOrEditor(Optional<LinkCompanyUser> link, String role, boolean result) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(link);

        //WHEN
        boolean response = checker.isCompanyAdminOrEditor(1, 1);

        //THEN
        assertThat(response).isEqualTo(result);
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When a user is an active company admin and I verify that a user is a company admin, true is returned.")
    void shouldReturnTrue_whenIsCompanyAdmin_asActiveCompanyAdmin() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, userWithUserRole.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null)));

        //WHEN
        boolean response = checker.isCompanyAdmin(userWithUserRole.getId(), 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("When a user is an inactive company admin and I verify that a user is a company admin, false is returned.")
    void shouldReturnFalse_whenIsCompanyAdmin_asInactiveCompanyAdmin() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //WHEN
        boolean response = checker.isCompanyAdmin(userWithUserRole.getId(), 1L);

        //THEN
        assertThat(response).isFalse();
    }

    @Test
    @DisplayName("When a user is a company editor or viewer and I verify that a user is a company admin, false is returned.")
    void shouldReturnFalse_whenIsCompanyAdmin_asCompanyEditorOrViewer() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, userWithUserRole.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null)));

        //WHEN
        boolean response = checker.isCompanyAdmin(userWithUserRole.getId(), 1L);

        //THEN
        assertThat(response).isFalse();
    }

    @DisplayName("I verify that a user is a company user.")
    @ParameterizedTest(name = "[{index}] is a company user: {1}")
    @MethodSource("linkCompanyUserList")
    void shouldIsCompanyUser(Optional<LinkCompanyUser> link, boolean result) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(link);

        //WHEN
        boolean response = checker.isCompanyUser(1, 1);

        //THEN
        assertThat(response).isEqualTo(result);
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    static Stream<Arguments> companyUserWithBadRoleList() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER),
                arguments(userWithUserRole, "user with user role", Optional.empty(), LinkCompanyUser.Role.ADMIN)
        );
    }

    static Stream<Arguments> companyUserList() {
        return Stream.of(
                arguments(Optional.of(linkCompanyUserRoleAdmin), LinkCompanyUser.Role.ADMIN.toString(), true),
                arguments(Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR.toString(), true),
                arguments(Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER.toString(), false),
                arguments(Optional.empty(), "", false)
        );
    }

    static Stream<Arguments> linkCompanyUserList() {
        return Stream.of(
                arguments(Optional.of(new LinkCompanyUser()), true),
                arguments(Optional.empty(), false)
        );
    }

}