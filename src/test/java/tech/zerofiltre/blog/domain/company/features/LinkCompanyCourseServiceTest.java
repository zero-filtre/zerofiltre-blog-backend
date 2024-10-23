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
import tech.zerofiltre.blog.domain.course.features.course.IsCourseExists;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.features.IsAdminUser;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkCompanyCourseServiceTest {

    private static User adminUser;
    private static User userWithRoleUser;

    private CompanyCourseService companyCourseService;

    @Mock
    CompanyCourseProvider companyCourseProvider;

    @Mock
    IsAdminUser isAdminUser;

    @Mock
    IsAdminOrCompanyAdmin isAdminOrCompanyAdmin;

    @Mock
    IsAdminOrCompanyUser isAdminOrCompanyUser;

    @Mock
    IsCourseExists isCourseExists;

    @Mock
    IsCompanyExists isCompanyExists;

    @BeforeAll
    static void setup() {
        adminUser = new User();
        adminUser.getRoles().add("ROLE_ADMIN");

        userWithRoleUser = new User();
        userWithRoleUser.getRoles().add("ROLE_USER");
    }

    @BeforeEach
    void init() {
        companyCourseService = new CompanyCourseService(companyCourseProvider, isAdminUser, isAdminOrCompanyAdmin, isAdminOrCompanyUser, isCompanyExists, isCourseExists);
    }

    @Test
    @DisplayName("given admin user and existing company and existing course and non existent companyCourse when link then verify call companyCourseProvider link")
    void whenLink_thenVerifyCallCompanyCourseProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenReturn(true);
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.link(adminUser, 1L, 1L);

        //THEN
        verify(companyCourseProvider).linkOf(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).link(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("given same existent companyCourse when link then verify not call companyCourseProvider link")
    void givenSameCompanyCourse_whenLink_thenVerifyNotCallCompanyCourseProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenReturn(true);
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(companyCourseProvider).linkOf(anyLong(), anyLong());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given suspended companyCourse when link then verify call companyCourseProvider link")
    void givenSuspendedCompanyCourse_whenLink_thenVerifyCallCompanyCourseProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));

        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenReturn(true);
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(companyCourseProvider).linkOf(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).link(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.getCompanyId()).isEqualTo(linkCompanyCourse.getCompanyId());
        assertThat(linkCompanyCourseCaptured.getCourseId()).isEqualTo(linkCompanyCourse.getCourseId());
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("given user with role user when link then throw ForbiddenActionException")
    void userWithRoleUser_whenLink_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.link(userWithRoleUser, 2L, 2L));

        verify(companyCourseProvider, never()).linkOf(anyLong(), anyLong());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user and not existing company when link then throw ResourceNotFoundException")
    void givenNotExistingCompany_whenLink_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(companyCourseProvider, never()).linkOf(anyLong(), anyLong());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user and existing company and not existing course when link then throw ResourceNotFoundException")
    void notExistingCourse_whenLink_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(companyCourseProvider, never()).linkOf(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user and existing company when activeAllByCompanyId then verify call companyCourseProvider link")
    void whenActiveAllByCompanyId_thenVerifyCallCompanyCourseProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));
        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 1L, 2L, false, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusWeeks(2));
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(linkCompanyCourse1);
        list.add(linkCompanyCourse2);

        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 1L);

        //THEN
        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).link(captor.capture());
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
    @DisplayName("given not admin user when activeAllByCompanyId then throw ForbiddenActionException")
    void givenNotAdminUser_whenActiveAllByCompanyId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(userWithRoleUser, 2L));

        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user and not existing company when activeAllByCompanyId then throw ResourceNotFoundException")
    void givenNotExitingCompany_whenActiveAllByCompanyId_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(adminUser, 2L));

        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user and existing company and not companyCourse when activeAllByCompanyId then verify not call companyCourseProvider link")
    void emptyListCompanyCourse_whenActiveAllByCompanyId_thenVerifyNotCallCompanyCourseProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(new ArrayList<>());

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 2L);

        //THEN
        verify(companyCourseProvider).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given good user and existing company and existing course when find then verify call companyCourseProvider find")
    void whenFind_thenVerifyCallCompanyCourseProviderFind() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.find(adminUser, 1L, 1L);

        //THEN
        verify(companyCourseProvider).linkOf(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given bad user when find then throw ForbiddenActionException")
    void givenBadUser_whenFind_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(companyCourseProvider, never()).linkOf(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given non existent company when find then throw ForbiddenActionException")
    void givenNonExistentCompany_whenFind_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(companyCourseProvider, never()).linkOf(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given non existent course when find then throw ForbiddenActionException")
    void givenNonExistentCourse_whenFind_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(companyCourseProvider, never()).linkOf(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given good user and existing company and existing course when findAllByCompanyId then verify call companyCourseProvider findAllByCompanyIdByPage")
    void whenFindAllByCompanyId_thenVerifyCallCompanyCourseProviderFindAllByCompanyIdByPage() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.findAllByCompanyId(adminUser, 0, 0, 1L);

        //THEN
        verify(companyCourseProvider).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("given bad user when findAllByCompanyId then throw ForbiddenActionException")
    void givenBadUser_whenFindAllByCompanyId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(companyCourseProvider, never()).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("given company user and bad company id when findAllByCompanyId then throw ForbiddenActionException")
    void givenCompanyUserAndBadCompanyId_whenFindAllByCompanyId_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyUser.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(companyCourseProvider, never()).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("given admin user when unlink and delete action then verify call companyCourseProvider unlink")
    void whenUnlink_thenVerifyCallCompanyCourseProviderUnLink() throws ForbiddenActionException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);
        Enrollment enrollment = new Enrollment();
        enrollment.setCompanyCourseId(linkCompanyCourse.getId());

        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.unLink(adminUser, 1L, 1L, true);

        //THEN
        verify(companyCourseProvider).unlink(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user when unlink and suspend action then verify call companyCourseProvider link")
    void whenUnlink_thenVerifyCallCompanyCourseProviderLink() throws ForbiddenActionException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.linkOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.unLink(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), false);

        //THEN
        verify(companyCourseProvider).linkOf(anyLong(), anyLong(), anyBoolean());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).link(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isFalse();
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("given admin user and existing company and existing course when unlink and suspend action then verify not call companyCourseProvider link")
    void whenUnlinkNotExitingCompanyCourse_thenVerifyNotCallCompanyCourseProviderLink() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.linkOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.unLink(adminUser, 1L, 1L, false);

        //THEN
        verify(companyCourseProvider).linkOf(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given user with role user when unlink then throw ForbiddenActionException")
    void userWithRoleUser_whenUnLink_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unLink(userWithRoleUser, 2L, 2L, true));

        verify(companyCourseProvider, never()).unlink(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given admin user and existing company and existing course when unLinkAllByCompanyId and delete action then verify call companyCourseProvider unLinkAllByCompanyId")
    void whenUnlinkAllByCompanyId_thenVerifyCallCompanyCourseProviderUnLinkAllByCompanyId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.unLinkAllByCompanyId(adminUser, 1L, true);

        //THEN
        verify(companyCourseProvider).unlinkAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("given admin user and existing company and 2 existing courses when unLinkAllByCompanyId and suspend action then verify call companyCourseProvider link")
    void whenUnLinkAllByCompanyId_thenVerifyCallCompanyCourseProviderUnLinkAllByCompanyId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(new LinkCompanyCourse(1L, 1L, 1L, true, null, null));
        list.add(new LinkCompanyCourse(1L, 1L, 2L, true, null, null));

        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.unLinkAllByCompanyId(adminUser, 1L, false);

        //THEN
        verify(companyCourseProvider, times(2)).link(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("given user with role user when unLinkAllByCompanyId then throw ForbiddenActionException")
    void userWithRoleUser_whenUnLinkAllByCompanyId_ByCompanyId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unLinkAllByCompanyId(userWithRoleUser, 2L, true));

        verify(companyCourseProvider, never()).unlinkAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("given admin user and not existing company when unLinkAllByCompanyId then throw ResourceNotFoundException")
    void notExistingCompany_whenUnLinkAllByCompanyId_ByCompanyId_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unLinkAllByCompanyId(adminUser, 2L, true));

        verify(companyCourseProvider, never()).unlinkAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("given admin user and existing course when unlinkAllByCourseId then verify call companyCourseProvider unlinkAllByCourseId")
    void whenUnlinkAllByCourseId_thenVerifyCallCompanyCourseProviderUnlinkAllByCourseId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isCourseExists.execute(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.unlinkAllByCourseId(userWithRoleUser, 1L);

        //THEN
        verify(companyCourseProvider).unlinkAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("given user with role user when unlinkAllByCourseId then throw ForbiddenActionException")
    void givenUserWithRoleUser_whenUnlinkAllByCourseId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(userWithRoleUser, 2L));

        verify(companyCourseProvider, never()).unlinkAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("given admin user and not existing company when unlinkAllByCourseId then throw ResourceNotFoundException")
    void givenNotExistingCompany_whenUnlinkAllByCourseId_ByCompanyId_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(isCourseExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(adminUser, 2L));

        verify(companyCourseProvider, never()).unlinkAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("given admin user when suspendLink then verify call companyCourseProvider link")
    void userAdminUser_whenSuspendLink_thenVerifyCallLink() {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        //WHEN
        companyCourseService.suspendLink(linkCompanyCourse);

        //THEN
        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).link(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isFalse();
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("given admin user when suspendLink then nothing")
    void userAdminUser_whenSuspendLink_thenNothing() {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L,1L, 1L, false, LocalDateTime.now(), LocalDateTime.now());

        //WHEN
        companyCourseService.suspendLink(linkCompanyCourse);

        //THEN
        verify(companyCourseProvider, never()).link(any(LinkCompanyCourse.class));
    }

}