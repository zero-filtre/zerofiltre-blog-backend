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
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @Mock
    CompanyCourseProvider companyCourseProvider;

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
        checker = new DataChecker(userProvider, courseProvider, companyProvider, companyUserProvider, companyCourseProvider);
    }

    @Test
    @DisplayName("given existing user execute then return true")
    void givenExistingUser_userExists_thenReturnTrue() throws ResourceNotFoundException {
        //GIVEN
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(new User()));

        //WHEN
        boolean response = checker.userExists(2L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given not existing user execute then throw resource not found exception")
    void givenNotExistingUser_userExists_thenThrowResourceNotFoundException() {
        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.userExists(2L));
    }

    @Test
    @DisplayName("given admin user when check if user is admin then return true")
    void givenAdminUser_whenCheckIfUserIsAdmin_thenReturnTrue() throws ForbiddenActionException {
        //GIVEN
        User user = new User();
        user.getRoles().add("ROLE_ADMIN");

        //WHEN
        boolean response = checker.isAdminUser(user);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given user with role user when check if user is admin then throw forbidden action exception")
    void givenUserWithRoleUser_whenCheckIfUserIsAdmin_thenThrowForbiddenActionException() {
        //GIVEN
        User user = new User();
        user.getRoles().add("ROLE_USER");

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.isAdminUser(user));
    }

    @Test
    @DisplayName("given an existing course when execute then return true")
    void givenExistingCompanyCourse_courseExists_thenNotThrowException() throws ResourceNotFoundException {
        //GIVEN
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //WHEN
        boolean response = checker.courseExists(course.getId());

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given not existing course when execute then throw ResourceNotFoundException")
    void givenUnpublishedCourse_courseExists_thenThrowException() {
        //GIVEN
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.courseExists(2L));
    }

    @Test
    @DisplayName("given an existing published course when execute then not throw exception")
    void givenExistingPublishedCourse_isPublishedCourse_thenNotThrowException() throws ForbiddenActionException {
        //GIVEN
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //WHEN
        boolean response = checker.isPublishedCourse(course.getId());

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given not existing course when execute then not throw exception")
    void givenNotExistingCourse_isPublishedCourse_thenNotThrowException() {
        //GIVEN
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        //THEN
        assertDoesNotThrow(() -> checker.isPublishedCourse(1L));
    }

    @Test
    @DisplayName("given draft course when execute then throw ForbiddenActionException")
    void givenUnpublishedCourse_isPublishedCourse_thenThrowException() {
        //GIVEN
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.isPublishedCourse(2L));
    }

    @Test
    @DisplayName("given existing company when execute if company exists then return true")
    void givenExistingCompany_whenCompanyExists_thenReturnTrue() throws ResourceNotFoundException {
        //GIVEN
        when(companyProvider.findById(anyLong())).thenReturn(Optional.of(new Company()));

        //WHEN
        boolean response = checker.companyExists(1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given non existent company when execute if company exists then throw ResourceNotFoundException")
    void givenNonExistentCompany_whenCompanyExists_thenThrowException() {
        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.companyExists(1L));
    }

    static Stream<Arguments> companyUser() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleAdmin), LinkCompanyUser.Role.ADMIN),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER)
        );
    }

    static Stream<Arguments> isCompanyUser() {
        return Stream.of(
                arguments(Optional.of(new LinkCompanyUser()), true),
                arguments(Optional.empty(), false)
        );
    }

    static Stream<Arguments> userWithGoodRoleProvider() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleAdmin), LinkCompanyUser.Role.ADMIN),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR)
        );
    }

    @Test
    @DisplayName("given existing company when execute if company exists then return true")
    void givenExistingCompany_whenExecute_thenReturnTrue() throws ResourceNotFoundException {
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyCourse()));

        //WHEN
        boolean response = checker.companyCourseExists(1, 1);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given non existent company course when execute then throw ResourceNotFoundException")
    void givenNotExistingCompanyCourse_whenExecute_thenThrowException() {
        //GIVEN
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.companyCourseExists(1, 1));
    }

    @Test
    @DisplayName("given inactive company course when execute then throw ResourceNotFoundException")
    void givenInactiveCompanyCourse_whenExecute_thenThrowException() {
        //GIVEN
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyCourse(1, 1, 1, false, false, LocalDateTime.now(), null)));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.companyCourseExists(1, 1));
    }

    @Test
    @DisplayName("given admin connected user when execute then return true")
    void givenAdminConnectedUser_whenIsAdminOrCompanyAdmin_thenReturnTrue() throws ForbiddenActionException {
        //WHEN
        boolean response = checker.isAdminOrCompanyAdmin(adminUser, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @DisplayName("given company user when execute then return true")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("companyUser")
    void givenCompanyUser_whenExecute_thenReturnTrue(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) throws ResourceNotFoundException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(companyUser);

        //WHEN
        boolean response = checker.companyUserExists(1L, user.getId());

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given non company user when execute then throw ResourceNotFoundException")
    void givenNonCompanyUser_whenExecute_thenThrowException() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> checker.companyUserExists(1L, 1L));
    }

    static Stream<Arguments> userWithBadRoleProvider1() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER),
                arguments(userWithUserRole, "user with user role", Optional.empty(), LinkCompanyUser.Role.ADMIN)
        );
    }

    @Test
    @DisplayName("given admin connected user when execute then return true")
    void givenAdminConnectedUser_whenExecute_thenReturnTrue() throws ForbiddenActionException {
        //WHEN
        boolean response = checker.isAdminOrCompanyUser(adminUser, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given user with role company admin when execute then return true")
    void givenUserWithRoleCompanyAdmin_whenIsAdminOrCompanyAdmin_thenReturnTrue() throws ForbiddenActionException {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(true).when(spy).isCompanyAdmin(anyLong(), anyLong());

        //WHEN
        boolean response = spy.isAdminOrCompanyAdmin(userWithUserRole, 1L);

        //THEN
        assertThat(response).isTrue();
        verify(spy).isCompanyAdmin(anyLong(), anyLong());
    }

    @DisplayName("given user with user role and bad company role when execute then throw ForbiddenActionException")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("userWithBadRoleProvider1")
    void givenUserWithUserRoleAndBadCompanyRole_whenIsAdminOrCompanyAdmin_thenThrowForbiddenActionException(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(companyUser);

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.isAdminOrCompanyAdmin(user, 1L));
    }

    @Test
    @DisplayName("When a user is an admin and I check that he is an admin or a company admin or editor, then true is returned")
    void shouldReturnTrue_whenVerifyUserIsAdmin() throws ForbiddenActionException {
        //WHEN
        boolean response = checker.isAdminOrCompanyAdminOrEditor(adminUser, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("When a user is a company user then return true")
    void shouldReturnTrue_whenUserIsCompanyUser() throws ForbiddenActionException {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(true).when(spy).isCompanyUser(anyLong(), anyLong());

        //WHEN
        boolean response = spy.isAdminOrCompanyUser(userWithUserRole, 1L);

        //THEN
        assertThat(response).isTrue();
        verify(spy).isCompanyUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a user is not a company user then a forbidden action exception is thrown")
    void shouldThrownException_whenUserIsNotCompanyUser() {
        //GIVEN
        DataChecker spy = spy(checker);
        doReturn(false).when(spy).isCompanyUser(anyLong(), anyLong());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> spy.isAdminOrCompanyUser(new User(), 1L));
        verify(spy).isCompanyUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a user is a company viewer and I check that he is an admin or a company admin or editor, then throw ForbiddenActionException")
    void shouldThrowException_whenIsCompanyViewer() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyUserRoleViewer));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.isAdminOrCompanyAdminOrEditor(new User(), 1L));
    }

    @Test
    @DisplayName("When a user is admin of another company and I check that he is an admin or a company admin or editor, then throw ForbiddenActionException")
    void shouldThrowException_whenIsAdminOfAnotherCompany() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> checker.isAdminOrCompanyAdminOrEditor(new User(), 1L));
    }

    @DisplayName("When a user is a company admin or editor and I check that he is an admin or a company admin or editor, then true is returned")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("userWithGoodRoleProvider")
    void shouldReturnTrue_whenVerifyUserIsCompanyAdminOrEditor(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(companyUser);

        //WHEN
        boolean response = checker.isAdminOrCompanyAdminOrEditor(user, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("When I verify that a user is a company ADMIN, then he is")
    void shouldTrue_whenIsCompanyAdmin_asActiveCompanyAdmin() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, userWithUserRole.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null)));

        //WHEN
        boolean response = checker.isCompanyAdmin(userWithUserRole.getId(), 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("When I verify that a user with inactive link is a company ADMIN, then he is not")
    void shouldFalse_whenIsCompanyAdmin_asActiveCompanyUserWithInactiveLink() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //WHEN
        boolean response = checker.isCompanyAdmin(userWithUserRole.getId(), 1L);

        //THEN
        assertThat(response).isFalse();
    }

    @Test
    @DisplayName("When I verify that a user with EDITOR role is a company ADMIN, then he is not")
    void shouldFalse_whenIsCompanyAdmin_asUserWithEditorRole() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, userWithUserRole.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null)));

        //WHEN
        boolean response = checker.isCompanyAdmin(userWithUserRole.getId(), 1L);

        //THEN
        assertThat(response).isFalse();
    }

    @DisplayName("I verify that a user is a company user.")
    @ParameterizedTest(name = "[{index}] is a company user: {1}")
    @MethodSource("isCompanyUser")
    void shouldIsCompanyUser(Optional<LinkCompanyUser> link, boolean result) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(link);

        //WHEN
        boolean response = checker.isCompanyUser(1, 1);

        //THEN
        assertThat(response).isEqualTo(result);
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @DisplayName("I verify that a user is a company admin or editor.")
    @ParameterizedTest(name = "[{index}] - role {1} is a company admin or editor: {2}")
    @MethodSource("companyAdminOrEditor")
    void shouldIsCompanyAdminOrEditor(Optional<LinkCompanyUser> link, String role, boolean result) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(link);

        //WHEN
        boolean response = checker.isCompanyAdminOrEditor(1, 1);

        //THEN
        assertThat(response).isEqualTo(result);
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    static Stream<Arguments> companyAdminOrEditor() {
        return Stream.of(
                arguments(Optional.of(linkCompanyUserRoleAdmin), LinkCompanyUser.Role.ADMIN.toString(), true),
                arguments(Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR.toString(), true),
                arguments(Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER.toString(), false),
                arguments(Optional.empty(), "", false)
        );
    }

}