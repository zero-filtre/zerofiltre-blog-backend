package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.SaveLessonVM;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/lesson")
public class LessonController {

    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final EnrollmentProvider enrollmentProvider;
    private final SecurityContextManager securityContextManager;

    public LessonController(LessonProvider lessonProvider, ChapterProvider chapterProvider, UserProvider userProvider, CourseProvider courseProvider, EnrollmentProvider enrollmentProvider, SecurityContextManager securityContextManager) {
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
        this.userProvider = userProvider;
        this.courseProvider = courseProvider;
        this.enrollmentProvider = enrollmentProvider;
        this.securityContextManager = securityContextManager;
    }

    @PostMapping
    public Lesson init(@RequestParam @NotNull @NotEmpty String title, @RequestParam long chapterId) throws ResourceNotFoundException, ForbiddenActionException {
        Lesson lesson = Lesson.builder()
                .lessonProvider(lessonProvider)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();
        return lesson.init(title, chapterId, securityContextManager.getAuthenticatedUser().getId());
    }

    @PatchMapping
    public Lesson save(@RequestBody @Valid SaveLessonVM saveLessonVM) throws ZerofiltreException {
        Lesson lesson = Lesson.builder()
                .lessonProvider(lessonProvider)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .id(saveLessonVM.getId())
                .title(saveLessonVM.getTitle())
                .chapterId(saveLessonVM.getChapterId())
                .video(saveLessonVM.getVideo())
                .free(saveLessonVM.isFree())
                .summary(saveLessonVM.getSummary())
                .thumbnail(saveLessonVM.getThumbnail())
                .content(saveLessonVM.getContent())
                .build();
        return lesson.save(securityContextManager.getAuthenticatedUser().getId());
    }

    @GetMapping("/{id}")
    public Lesson get(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        Lesson lesson = Lesson.builder()
                .lessonProvider(lessonProvider)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .id(id)
                .build();
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return wanted lesson if it's free");
        }
        return lesson.get(user == null ? 0 : user.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        Lesson lesson = Lesson.builder()
                .lessonProvider(lessonProvider)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .id(id)
                .build();
        lesson.delete(securityContextManager.getAuthenticatedUser().getId());
    }
}
