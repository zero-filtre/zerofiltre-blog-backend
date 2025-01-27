package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyCourseServiceTest {

    private static User adminUser;
    private static User userWithRoleUser;

    private CompanyCourseService companyCourseService;

    @Mock
    CompanyCourseProvider companyCourseProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @Mock
    DataChecker checker;

    @BeforeAll
    static void setup() {
        adminUser = new User();
        adminUser.getRoles().add("ROLE_ADMIN");

        userWithRoleUser = new User();
        userWithRoleUser.getRoles().add("ROLE_USER");
    }

    @BeforeEach
    void init() {
        companyCourseService = new CompanyCourseService(companyCourseProvider, enrollmentProvider, checker);
    }

    @Test
    @DisplayName("When I link a course to a company as a platform admin, then the link is created")
    void shouldCreatesLink_whenLinkCourseToCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.link(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When I link a course to a company and the link already exists as a platform admin, then there is nothing")
    void shouldDoNothing_whenLinkCourseToCompany_IfLinkExists_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a link between a course and a company is suspended and I link them again as a platform admin, then the link is activated")
    void shouldActivatesLink_whenSuspendLinkBetweenCourseAndCompany_LinkAgain_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.getCompanyId()).isEqualTo(linkCompanyCourse.getCompanyId());
        assertThat(linkCompanyCourseCaptured.getCourseId()).isEqualTo(linkCompanyCourse.getCourseId());
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When I link a course to a company as a non-platform admin, then it is forbidden")
    void shouldForbidden_whenLinkCourseToCompany_asNonPlatformAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.link(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I link a course to a company that does not exist as a platform admin, then the course and the company are not linked")
    void shouldCourseAndCompanyNotLinked_whenLinkCourseToNotExistingCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I link a course that does not exist to a company as a platform admin, then the course and the company are not linked")
    void shouldCourseAndCompanyNotLinked_whenLinkNotExistingCourseToCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I activate all links between courses and a company as a platform admin, then all the links are activated")
    void shouldActivatesAllLinks_whenActivateAllLinksBetweenCoursesAndCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));
        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 1L, 2L, false, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusWeeks(2));
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(linkCompanyCourse1);
        list.add(linkCompanyCourse2);

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 1L);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).save(captor.capture());
        List<LinkCompanyCourse> listCaptured = captor.getAllValues();
        assertThat(listCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(listCaptured.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(listCaptured.get(0).isActive()).isTrue();
        assertThat(listCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(listCaptured.get(0).getSuspendedAt()).isNull();

        assertThat(listCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(listCaptured.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(listCaptured.get(1).isActive()).isTrue();
        assertThat(listCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(listCaptured.get(1).getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When I activate all links between courses and a company as a non-platform admin, then it is forbidden.")
    void shouldForbidden_whenActivateAllLinksBetweenCoursesAndCompany_asNonPlatformAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(userWithRoleUser, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I activate all links between courses and a company that does not exist as a platform admin, then the links are not activated")
    void shouldNotActivatedLinks_whenActivateAllLinksBetweenCoursesAndNotExistingCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(adminUser, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I activate all links between courses and a company and the links do not exist as a platform admin, then there is nothing")
    void shouldDoNothing_whenActivateAllLinksBetweenCoursesAndCompany_IfLinksDoNotExist_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(new ArrayList<>());

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 2L);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I search for a link between a course and a company as a platform admin or a company user, then I find the link")
    void shouldFindsLink_whenSearchForLinkBetweenCourseAndCompany_asAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.find(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I search for a link between a course and a company as a user who is neither a platform admin nor part of the company, then it is forbidden")
    void shouldForbidden_whenSearchForLinkBetweenCourseAndCompany_asNonPlatformAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I search for a link between a course and a company that does not exist as a platform admin or a company user, then I find nothing")
    void shouldFindNothing_whenSearchForLinkBetweenCourseAndNotExistingCompany_asAdminOrCompanyUser() throws
    ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I search for a link between a course that does not exist and a company as a platform admin or a company user, then I find nothing")
    void shouldFindNothing_whenSearchForLinkBetweenNotExistingCourseAndCompany_asAdminOrCompanyUser() throws
    ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I search for all the links between courses and a company as a platform admin or a company user, then I get part of the list of links")
    void shouldReturnPartOfLinkList_whenSearchingForAllLinksBetweenCoursesAndCompany_asAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.findAllByCompanyId(adminUser, 0, 0, 1L);

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When I search for all the links between courses and a company as a user who is neither a platform admin nor part of the company, then it is forbidden")
    void shouldForbidden_whenSearchingForAllLinksBetweenCoursesAndCompany_asNonPlatformAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When I search for all the links between courses and a company that does not exist as a platform admin or a company user, then I find nothing")
    void shouldFindNothing_whenSearchingForAllLinksBetweenCoursesAndNotExistingCompany_asAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When I search for all the courses of a company as a platform admin or a company user, I get part of the list of courses")
    void shouldReturnPartOfCourseList_whenSearchingForAllCoursesOfACompany_asAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.findAllCoursesByCompanyId(adminUser, 0, 0, 1L);

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllCoursesByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When I search for all the courses of a company as a user who is neither a platform admin nor part of the company, then it is forbidden")
    void shouldForbidden_whenSearchingForAllCoursesOfACompany_asNonPlatformAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenThrow(ForbiddenActionException.class);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findAllCoursesByCompanyId(adminUser, 0, 0, 1L));

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker, never()).companyExists(anyLong());
        verify(companyCourseProvider, never()).findAllCoursesByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When I search for the identification number of the link between a course and a company, I find the identification number")
    void shouldFindIdentificationNumber_whenSearchingForLinkBetweenCourseAndCompany() throws ResourceNotFoundException {
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1, 2L, 3L, true, LocalDateTime.now(), null);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        long response = companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(1, 1);

        //THEN
        assertThat(response).isEqualTo(linkCompanyCourse.getId());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When I search for the identification number of the non-existent link between a course and a company, then I find nothing")
    void shouldFindNothing_whenNotExistingLinkBetweenCourseAndCompanyIsSearched() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyCourseExists(anyLong(), anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(1, 1));

        verify(checker).companyCourseExists(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I delete the link between a course and a company as a platform or company admin, the link is deleted and the enrollments related to this link are suspended")
    void shouldDeleteLinkAndSuspendEnrollments_whenLinkBetweenCourseAndCompanyIsDeleted_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captorLink = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).delete(captorLink.capture());
        LinkCompanyCourse linkCaptured = captorLink.getValue();
        assertThat(linkCaptured).isNotNull();
        assertThat(linkCaptured.getId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(linkCaptured.getCompanyId()).isEqualTo(linkCompanyCourse.getCompanyId());
        assertThat(linkCaptured.getCourseId()).isEqualTo(linkCompanyCourse.getCourseId());
        assertThat(linkCaptured.isActive()).isEqualTo(linkCompanyCourse.isActive());
        assertThat(linkCaptured.getLinkedAt()).isEqualTo(linkCompanyCourse.getLinkedAt());
        assertThat(linkCaptured.getSuspendedAt()).isEqualTo(linkCompanyCourse.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I delete a non-existent link between a course and a company as a platform or company admin, then there is nothing")
    void shouldDoNothing_whenDeleteNotExistingLink_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).delete(any(LinkCompanyCourse.class));
        verify(enrollmentProvider, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("When I suspend the link between a course and a company as a platform or company admin, the link is suspended and the enrollments related to this link are suspended")
    void shouldSuspendLinkAndEnrollments_whenLinkBetweenCourseAndCompanyIsSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));

        //WHEN
        companyCourseService.unlink(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isFalse();
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I suspend a non-existent link between a course and a company as a platform or company admin, then there is nothing")
    void shouldDoNothing_whenSuspendNotExistingLinkBetweenCourseAndCompany_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
        verify(enrollmentProvider, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("When I suspend a link between a course and a company as a platform or company non-admin, then it is forbidden")
    void shouldForbidden_whenSuspendLink_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlink(userWithRoleUser, 2L, 2L, false));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).delete(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When I delete all links between courses and a company as a platform or company admin, then the links are deleted and the enrollments related to these links are suspended")
    void shouldDeleteAllLinksAndSuspendEnrollments_whenAllLinksBetweenCoursesAndCompanyAreDeleted_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 1L, 2L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCompanyId(adminUser, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyId(anyLong());

        ArgumentCaptor<LinkCompanyCourse> captorLink = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).delete(captorLink.capture());
        List<LinkCompanyCourse> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyCourse1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyCourse1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyCourse2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyCourse2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I suspend all links between courses and a company as a platform or company admin, then the links are suspended and the enrollments related to these links are suspended")
    void shouldSuspendAllLinksAndSuspendEnrollments_whenAllLinksBetweenCoursesAndCompanyAreSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 2L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCompanyId(adminUser, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyId(anyLong());
        verify(enrollmentProvider, times(2)).findAll(anyLong(), anyBoolean());

        ArgumentCaptor<LinkCompanyCourse> captorLink = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).save(captorLink.capture());
        List<LinkCompanyCourse> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyCourse1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyCourse1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyCourse2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyCourse2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I delete all links between courses and a company as a platform or company non-admin, then it is forbidden")
    void shouldForbidden_whenDeleteAllLinksBetweenCoursesAndCompany_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCompanyId(userWithRoleUser, 2L, true));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When I delete all links between courses and a company that does not exist as a platform or company admin, then all links are not deleted")
    void shouldLinksNotDeleted_whenDeleteAllLinksOfNotExistingCompany_asPlatformOrCompanyAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCompanyId(adminUser, 2L, true));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When I delete all links between a course and companies as a platform admin, then the links are deleted and the enrollments related to these links are suspended")
    void shouldDeleteAllLinksAndSuspendEnrollments_whenAllLinksBetweenCourseAndCompaniesAreDeleted_asPlatformAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 3L, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 2L, 3L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCourseId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCourseId(adminUser, 1L, true);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findAllByCourseId(anyLong());

        ArgumentCaptor<LinkCompanyCourse> captorLink = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).delete(captorLink.capture());
        List<LinkCompanyCourse> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyCourse1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyCourse1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyCourse2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyCourse2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I suspend all links between a course and companies as a platform or company admin, then the links are suspended and the enrollments related to these links are suspended")
    void shouldSuspendAllLinksAndSuspendEnrollments_whenAllLinksBetweenCourseAndCompaniesAreSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 3L, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 2L, 3L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCourseId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCourseId(adminUser, 1L, false);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findAllByCourseId(anyLong());
        verify(enrollmentProvider, times(2)).findAll(anyLong(), anyBoolean());

        ArgumentCaptor<LinkCompanyCourse> captorLink = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).save(captorLink.capture());
        List<LinkCompanyCourse> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyCourse1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyCourse1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyCourse2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyCourse2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyCourseId()).isEqualTo(linkCompanyCourse1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyCourseId()).isEqualTo(linkCompanyCourse2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I delete all links between a course and companies as a platform or company non-admin, then it is forbidden")
    void shouldForbidden_whenDeleteAllLinksBetweenCourseAndCompanies_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(userWithRoleUser, 2L, true));

        verify(checker).isAdminUser(any(User.class));
        verify(companyCourseProvider, never()).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When I delete all links between a course that does not exist and companies as a platform or company admin, then all links are not deleted")
    void shouldLinksNotDeleted_whenDeleteAllLinksBetweenNotExistingCourseAndCompanies_asPlatformOrCompanyAdmin() throws ResourceNotFoundException {
        //GIVEN
        when(checker.courseExists(anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(adminUser, 2L, true));

        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider, never()).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When I suspend the link between a course and a company, then the link is suspended and all enrollments related to this link are also suspended")
    void shouldSuspendLinkAndRelatedEnrollments_whenSuspendCourseCompanyLink() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment = new Enrollment();
        enrollment.setCompanyCourseId(linkCompanyCourse.getId());

        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment));

        //WHEN
        companyCourseService.suspendLink(linkCompanyCourse);

        //THEN
        ArgumentCaptor<LinkCompanyCourse> captorLink = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captorLink.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captorLink.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isFalse();
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider).save(captorEnrollment.capture());
        Enrollment enrollmentCaptured = captorEnrollment.getValue();
        assertThat(enrollmentCaptured.isActive()).isFalse();
        assertThat(enrollmentCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When I suspend the link between a course and a company, if the link is already suspended, then all enrollments are still suspended")
    void shouldAllEnrollmentsAreStillSuspended_whenSuspendLinkBetweenCourseAndCompany_ifLinkIsAlreadySuspended() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L,1L, 1L, false, LocalDateTime.now(), LocalDateTime.now());

        Enrollment enrollment = new Enrollment();

        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment));

        //WHEN
        companyCourseService.suspendLink(linkCompanyCourse);

        //THEN
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
        verify(enrollmentProvider).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("When I suspend all enrollments related to a link company-course, then all enrollments are suspended")
    void shouldAllEnrollmentsAreSuspended_whenSuspendEnrollmentsRelatedToLinkCompanyCourse() throws ZerofiltreException {
        //GIVEN
        long companyCourseId = 1;

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(companyCourseId);
        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(companyCourseId);

        List<Enrollment> enrollmentList = List.of(enrollment1, enrollment2);

        when(enrollmentProvider.findAll(companyCourseId, true)).thenReturn(enrollmentList);

        //WHEN
        companyCourseService.suspendEnrollments(1L);

        //THEN
        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captor.capture());
        List<Enrollment> enrollmentsCaptured = captor.getAllValues();
        assertThat(enrollmentsCaptured.size()).isEqualTo(2);
        assertThat(enrollmentsCaptured.get(0).isActive()).isFalse();
        assertThat(enrollmentsCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(enrollmentsCaptured.get(1).isActive()).isFalse();
        assertThat(enrollmentsCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

}