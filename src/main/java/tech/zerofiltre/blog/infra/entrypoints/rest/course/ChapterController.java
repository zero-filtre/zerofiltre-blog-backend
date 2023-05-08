package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterProvider chapterProvider;
    private final LessonProvider lessonProvider;
    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final SecurityContextManager securityContextManager;


    @PostMapping
    public Chapter init(@RequestParam @NotNull @NotEmpty String title, @RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();
        User user = securityContextManager.getAuthenticatedUser();
        return chapter.init(title, courseId, user.getId());
    }

    @PatchMapping
    public Chapter save(@RequestBody @Valid SaveChapterVM saveChapterVM) throws ZerofiltreException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .title(saveChapterVM.getTitle())
                .id(saveChapterVM.getId())
                .courseId(saveChapterVM.getCourseId())
                .build();
        User user = securityContextManager.getAuthenticatedUser();
        return chapter.save(user.getId());
    }

    @GetMapping("/{id}")
    public Chapter get(@PathVariable long id) throws ResourceNotFoundException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .id(id)
                .build();
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted chapter", e);
        }
        return chapter.get();
    }

    @PatchMapping("/{id}/lesson/{lessonId}")
    public Chapter moveLesson(@PathVariable long id, @PathVariable long lessonId, @RequestParam int toNumber) throws ResourceNotFoundException, ForbiddenActionException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .lessonProvider(lessonProvider)
                .id(id)
                .build();
        User user = securityContextManager.getAuthenticatedUser();
        return chapter.moveLesson(user.getId(), lessonId, toNumber);

    }

    @PatchMapping("/{id}")
    public Chapter moveChapter(@PathVariable long id, @RequestParam int toNumber) throws ZerofiltreException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .lessonProvider(lessonProvider)
                .id(id)
                .build();
        User user = securityContextManager.getAuthenticatedUser();
        return chapter.move(user.getId(), toNumber);

    }

    @GetMapping("/course/{id}")
    public List<Chapter> getByCourseId(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .courseId(id)
                .build();
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted chapters", e);
        }
        return chapter.getByCourseId(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .id(id)
                .build();
        User user = securityContextManager.getAuthenticatedUser();
        chapter.delete(user.getId());
    }


}
