package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.features.lesson.LessonService;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.mapper.UpdateLessonVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.UpdateLessonVM;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/lesson")
public class LessonController {

    private final LessonService lessonService;
    private final SecurityContextManager securityContextManager;
    private final UpdateLessonVMMapper mapper = Mappers.getMapper(UpdateLessonVMMapper.class);

    public LessonController(LessonProvider lessonProvider, ChapterProvider chapterProvider, UserProvider userProvider, CourseProvider courseProvider, EnrollmentProvider enrollmentProvider, SecurityContextManager securityContextManager) {
        this.securityContextManager = securityContextManager;
        this.lessonService = new LessonService(lessonProvider, chapterProvider, userProvider, courseProvider, enrollmentProvider);
    }

    @PostMapping
    public Lesson init(@RequestParam @NotNull @NotEmpty String title, @RequestParam long chapterId) throws ResourceNotFoundException, ForbiddenActionException {
        return lessonService.init(title, chapterId, securityContextManager.getAuthenticatedUser().getId());
    }

    @PatchMapping
    public Lesson update(@RequestBody @Valid UpdateLessonVM updateLessonVM) throws ZerofiltreException {
        return lessonService.update(mapper.fromVM(updateLessonVM), securityContextManager.getAuthenticatedUser().getId());
    }

    @GetMapping("/{id}")
    public Lesson get(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return wanted lesson if it's free");
        }
        return lessonService.getAsUser(id, user == null ? 0 : user.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        lessonService.delete(id, securityContextManager.getAuthenticatedUser().getId());
    }
}
