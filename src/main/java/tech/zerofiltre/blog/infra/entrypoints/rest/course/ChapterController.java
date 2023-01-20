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

@Slf4j
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterProvider chapterProvider;
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
    public Chapter save(@RequestBody @Valid SaveChapterVM saveChapterVM) throws BlogException {
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
        User user = securityContextManager.getAuthenticatedUser();
        return chapter.get(user.getId());
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
