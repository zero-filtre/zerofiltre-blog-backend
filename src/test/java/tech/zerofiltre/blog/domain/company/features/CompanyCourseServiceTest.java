package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
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
    @DisplayName("When a platform admin links a course to a company, then the link is created")
    void shouldCreatesLink_whenLinkCourseToCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCompanyExistence(anyLong());
        doNothing().when(checker).checkCourseExistence(anyLong());
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.link(adminUser, 1L, 1L);

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkCourseExistence(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When a platform admin links a course to a company and the link already exists, then there is nothing")
    void shouldDoNothing_whenLinkCourseToCompany_IfLinkExists_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCompanyExistence(anyLong());
        doNothing().when(checker).checkCourseExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkCourseExistence(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a link between a course and a company is suspended and a platform admin links them again, then the link is activated")
    void shouldActivatesLink_whenSuspendLinkBetweenCourseAndCompany_LinkAgain_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCompanyExistence(anyLong());
        doNothing().when(checker).checkCourseExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkCourseExistence(anyLong());
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
    @DisplayName("When a non-platform admin links a course to a company, then it is forbidden")
    void shouldForbidden_whenLinkCourseToCompany_asNonPlatformAdmin() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminUser(any(User.class));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.link(userWithRoleUser, 2L, 2L));

        verify(checker).checkIfAdminUser(any(User.class));
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin links a course to a company that does not exist, then the course and the company are not linked")
    void shouldCourseAndCompanyNotLinked_whenLinkCourseToNotExistingCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doThrow(ResourceNotFoundException.class).when(checker).checkCompanyExistence(anyLong());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin links a course that does not exist to a company, then the course and the company are not linked")
    void shouldCourseAndCompanyNotLinked_whenLinkNotExistingCourseToCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCompanyExistence(anyLong());
        doThrow(ResourceNotFoundException.class).when(checker).checkCourseExistence(anyLong());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkCourseExistence(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin activates all links between courses and a company, then all the links are activated")
    void shouldActivatesAllLinks_whenActivateAllLinksBetweenCoursesAndCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCompanyExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, false, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));
        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 1L, 2L, false, false, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusWeeks(2));
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(linkCompanyCourse1);
        list.add(linkCompanyCourse2);

        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 1L);

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());

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
    @DisplayName("When a non-platform admin activates all links between courses and a company, then it is forbidden.")
    void shouldForbidden_whenActivateAllLinksBetweenCoursesAndCompany_asNonPlatformAdmin() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminUser(any(User.class));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(userWithRoleUser, 2L));

        verify(checker).checkIfAdminUser(any(User.class));
        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin activates all links between courses and a company that does not exist, then the links are not activated")
    void shouldNotActivatedLinks_whenActivateAllLinksBetweenCoursesAndNotExistingCompany_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doThrow(ResourceNotFoundException.class).when(checker).checkCompanyExistence(anyLong());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(adminUser, 2L));

        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin activates all links between courses and a company and the links do not exist, then there is nothing")
    void shouldDoNothing_whenActivateAllLinksBetweenCoursesAndCompany_IfLinksDoNotExist_asPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCompanyExistence(anyLong());
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(new ArrayList<>());

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 2L);

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCompanyExistence(anyLong());
        verify(companyCourseProvider).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin or a company user searches for a link between a course and a company, then he finds the link")
    void shouldFindsLink_whenSearchForLinkBetweenCourseAndCompany_asAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        LinkCompanyCourse expectedLink = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now(), null);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(expectedLink));

        //WHEN
        Optional<LinkCompanyCourse> result = companyCourseService.find(adminUser, 1L, 1L);

        //THEN
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        assertThat(result).contains(expectedLink);
    }

    @Test
    @DisplayName("When a user who is neither a platform admin nor part of the company searches for a link between a course and a company, then it is forbidden")
    void shouldForbidden_whenSearchForLinkBetweenCourseAndCompany_asNonPlatformAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a platform admin or a company user searches for a link between a course and a company that does not exist, then he finds nothing")
    void shouldFindNothing_whenSearchForLinkBetweenNotExistingCourseAndCompany_asAdminOrCompanyUser() throws
            ForbiddenActionException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //THEN
        Optional<LinkCompanyCourse> result = companyCourseService.find(userWithRoleUser, 2L, 2L);

        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("When a platform admin or a company user searches for all the links between courses and a company, then he finds a part of the list of links")
    void shouldFindPartOfLinkList_whenSearchingForAllLinksBetweenCoursesAndCompany_asAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        //WHEN
        companyCourseService.findAllByCompanyId(adminUser, 0, 0, 1L);

        //THEN
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyId(0, 0, 1);
    }

    @Test
    @DisplayName("When a user who is neither a platform admin nor part of the company searches for all the links between courses and a company, then it is forbidden")
    void shouldForbidden_whenSearchingForAllLinksBetweenCoursesAndCompany_asNonPlatformAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider, never()).findByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When a platform admin or a company user search for all the links between courses and a non-existent company, then he finds nothing")
    void shouldFindNothing_whenSearchingForAllLinksBetweenCoursesAndNotExistingCompany_asAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        when(companyCourseProvider.findByCompanyId(anyInt(), anyInt(), anyLong())).thenReturn(Page.emptyPage());

        //WHEN
        Page<LinkCompanyCourse> result = companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L);

        //THEN
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("When a platform admin or a company user searches for all the courses of a company, he finds a part of the list of courses")
    void shouldReturnPartOfCourseList_whenSearchingForAllCoursesOfACompany_asAdminOrCompanyUser() throws ForbiddenActionException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        //WHEN
        companyCourseService.findCoursesByCompanyId(new FinderRequest(0, 0, Status.PUBLISHED, adminUser), 1L);

        //THEN
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider).findCoursesByCompanyId(0, 0, 1L, Status.PUBLISHED);
    }

    @Test
    @DisplayName("When a user who is neither a platform admin nor part of the company searches for all the courses of a company, then it is forbidden")
    void shouldForbidden_whenSearchingForAllCoursesOfACompany_asNonPlatformAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findCoursesByCompanyId(new FinderRequest(0, 0, Status.PUBLISHED, adminUser), 1L));

        //THEN
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker, never()).checkCompanyExistence(anyLong());
        verify(companyCourseProvider, never()).findCoursesByCompanyId(anyInt(), anyInt(), anyLong(), any(Status.class));
    }

    @Test
    @DisplayName("When I search for the identification number of the link between a course and a company, I find the identification number")
    void shouldFindIdentificationNumber_whenSearchingForLinkBetweenCourseAndCompany() throws ResourceNotFoundException {
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1, 2L, 3L, false, true, LocalDateTime.now(), null);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        long response = companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(1, 1);

        //THEN
        assertThat(response).isEqualTo(linkCompanyCourse.getId());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When I search for the identification number of the non-existent link between a course and a company, then I find nothing")
    void shouldFindNothing_whenNotExistingLinkBetweenCourseAndCompanyIsSearched() {
        //GIVEN
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(1, 1));
    }

    @Test
    @DisplayName("When a platform or company admin deletes the link between a platform course and a company, the link is deleted and the enrollments related to this link are suspended")
    void shouldDeleteLinkAndSuspendEnrollments_whenLinkBetweenCourseAndCompanyIsDeleted_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse.getId());

        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
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
    @DisplayName("When a platform or company admin deletes a non-existent link between a course and a company, then there is nothing")
    void shouldDoNothing_whenDeleteNotExistingLink_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).delete(any(LinkCompanyCourse.class));
        verify(enrollmentProvider, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("When a platform or company admin suspend the link between a course and a company, the link is suspended and the enrollments related to this link are suspended")
    void shouldSuspendLinkAndEnrollments_whenLinkBetweenCourseAndCompanyIsSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse.getId());

        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));

        //WHEN
        companyCourseService.unlink(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), false);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
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
    @DisplayName("When a platform or company admin suspend a non-existent link between a course and a company, then there is nothing")
    void shouldDoNothing_whenSuspendNotExistingLinkBetweenCourseAndCompany_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, false);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
        verify(enrollmentProvider, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("When a user non-admin of the platform or the company suspends a link between a course and a company, then it is forbidden")
    void shouldForbidden_whenSuspendLink_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlink(userWithRoleUser, 2L, 7L, false));

        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).delete(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform or company admin deletes the link between an exclusive company course and a company, then the link is deleted")
    void shouldDeleteLink_whenLinkBetweenCompanyCourseAndCompanyIsDeleted_asPlatformOrCompanyAdminOrEditor() throws ZerofiltreException {
        //GIVEN
        Course course = new Course();
        course.setId(1L);

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, course.getId(), true, true, LocalDateTime.now().minusMonths(1), null);

        doNothing().when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));
        doNothing().when(companyCourseProvider).delete(any(LinkCompanyCourse.class));

        //WHEN
        companyCourseService.unlink(adminUser, 1L, course.getId(), true);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider).delete(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform or company admin deletes all links between courses and a company, then the links are deleted and the enrollments related to these links are suspended")
    void shouldDeleteAllLinksAndSuspendEnrollments_whenAllLinksBetweenCoursesAndCompanyAreDeleted_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        doNothing().when(checker).checkCompanyExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 1L, 2L, false, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCompanyId(adminUser, 1L, true);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).checkCompanyExistence(anyLong());
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
    @DisplayName("When a platform or company admin suspends all links between courses and a company, then the links are suspended and the enrollments related to these links are suspended")
    void shouldSuspendAllLinksAndSuspendEnrollments_whenAllLinksBetweenCoursesAndCompanyAreSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        doNothing().when(checker).checkCompanyExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 2L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCompanyId(adminUser, 1L, false);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).checkCompanyExistence(anyLong());
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
    @DisplayName("When a non-admin user of the platform or company deletes all links between courses and a company, then it is forbidden")
    void shouldForbidden_whenDeleteAllLinksBetweenCoursesAndCompany_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCompanyId(userWithRoleUser, 2L, true));

        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin deletes all links between courses and a company that does not exist, then all links are not deleted")
    void shouldLinksNotDeleted_whenDeleteAllLinksOfNotExistingCompany_asPlatformOrCompanyAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        doNothing().when(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        doThrow(ResourceNotFoundException.class).when(checker).checkCompanyExistence(anyLong());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCompanyId(adminUser, 2L, true));

        verify(checker).checkIfAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).checkCompanyExistence(anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When I delete all links between a platform course and companies as a platform admin, then the links are deleted and the enrollments related to these links are suspended")
    void shouldDeleteAllLinksAndSuspendEnrollments_whenAllLinksBetweenPlatformCourseAndCompaniesAreDeleted_asPlatformAdmin() throws ZerofiltreException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCourseExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 3L, false, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 2L, 3L, false, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(companyCourseProvider.findAllByCourseId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCourseId(adminUser, 1L, true);

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCourseExistence(anyLong());
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
    @DisplayName("When a platform or company admin suspends all links between a course and companies, then the links are suspended and the enrollments related to these links are suspended")
    void shouldSuspendAllLinksAndSuspendEnrollments_whenAllLinksBetweenCourseAndCompaniesAreSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        doNothing().when(checker).checkIfAdminUser(any(User.class));
        doNothing().when(checker).checkCourseExistence(anyLong());

        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 3L, false, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 2L, 3L, false, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyCourseId(linkCompanyCourse1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyCourseId(linkCompanyCourse2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyCourseId(linkCompanyCourse2.getId());

        when(companyCourseProvider.findAllByCourseId(anyLong())).thenReturn(List.of(linkCompanyCourse1, linkCompanyCourse2));
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyCourseService.unlinkAllByCourseId(adminUser, 1L, false);

        //THEN
        verify(checker).checkIfAdminUser(any(User.class));
        verify(checker).checkCourseExistence(anyLong());
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
    @DisplayName("When a non-admin user of the platform or company deletes all links between a course and companies, then it is forbidden")
    void shouldForbidden_whenDeleteAllLinksBetweenCourseAndCompanies_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminUser(any(User.class));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(userWithRoleUser, 2L, true));

        verify(checker).checkIfAdminUser(any(User.class));
        verify(companyCourseProvider, never()).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin deletes all links between a course that does not exist and companies, then all links are not deleted")
    void shouldLinksNotDeleted_whenDeleteAllLinksBetweenNotExistingCourseAndCompanies_asPlatformOrCompanyAdmin() throws ResourceNotFoundException {
        //GIVEN
        doThrow(ResourceNotFoundException.class).when(checker).checkCourseExistence(anyLong());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(adminUser, 2L, true));

        verify(checker).checkCourseExistence(anyLong());
        verify(companyCourseProvider, never()).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When I suspend the link between a course and a company, then the link is suspended and all enrollments related to this link are also suspended")
    void shouldSuspendLinkAndRelatedEnrollments_whenSuspendCourseCompanyLink() throws ZerofiltreException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, true, LocalDateTime.now().minusMonths(1), null);

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
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, false, LocalDateTime.now(), LocalDateTime.now());

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
        assertThat(enrollmentsCaptured).hasSize(2);
        assertThat(enrollmentsCaptured.get(0).isActive()).isFalse();
        assertThat(enrollmentsCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(enrollmentsCaptured.get(1).isActive()).isFalse();
        assertThat(enrollmentsCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

}