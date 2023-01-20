package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.validation.*;
import javax.validation.constraints.*;

@RestController
@RequestMapping("/lesson")
public class LessonController {

    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final SecurityContextManager securityContextManager;

    public LessonController(LessonProvider lessonProvider, ChapterProvider chapterProvider, UserProvider userProvider, CourseProvider courseProvider, SecurityContextManager securityContextManager) {
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
        this.userProvider = userProvider;
        this.courseProvider = courseProvider;
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
    public Lesson save(@RequestBody @Valid SaveLessonVM saveLessonVM) throws BlogException {
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
                .type(saveLessonVM.getType())
                .thumbnail(saveLessonVM.getThumbnail())
                .content(saveLessonVM.getContent())
                .build();
        return lesson.save(securityContextManager.getAuthenticatedUser().getId());
    }

    @GetMapping("/{id}")
    public Lesson get(@PathVariable long id) throws ResourceNotFoundException {
        Lesson lesson = Lesson.builder()
                .lessonProvider(lessonProvider)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .id(id)
                .build();
        return lesson.get(securityContextManager.getAuthenticatedUser().getId());
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
