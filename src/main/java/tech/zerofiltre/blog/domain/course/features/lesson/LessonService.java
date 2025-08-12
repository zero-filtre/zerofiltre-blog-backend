package tech.zerofiltre.blog.domain.course.features.lesson;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

import static tech.zerofiltre.blog.domain.article.features.FindArticle.DOTS;
import static tech.zerofiltre.blog.domain.course.model.Chapter.DOES_NOT_EXIST;
import static tech.zerofiltre.blog.domain.course.model.Chapter.USER_DOES_NOT_EXIST;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED;

@RequiredArgsConstructor
public class LessonService {

    public static final String THE_LESSON_OF_ID = "The lesson of id ";

    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final EnrollmentProvider enrollmentProvider;

    public Lesson init(String title, long chapterId, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setChapterId(chapterId);

        checkLessonAccessConditions(lesson, currentUserId, chapterId, false, false);

        Optional<Chapter> chapter = chapterProvider.chapterOfId(chapterId);
        Lesson lastLesson = chapter.get().getLessons().stream().reduce((first, second) -> second).orElse(null);

        int newPosition = (lastLesson != null) ? lastLesson.getNumber() + 1 : 1;
        lesson.setNumber(newPosition);

        return lessonProvider.save(lesson);
    }

    public Lesson update(Lesson lesson, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {

        Lesson existingLesson = lessonProvider.lessonOfId(lesson.getId())
                .orElseThrow(() -> new ResourceNotFoundException(THE_LESSON_OF_ID + lesson.getId() + DOES_NOT_EXIST, String.valueOf(lesson.getId())));

        checkLessonAccessConditions(lesson, currentUserId, lesson.getChapterId(), false, false);
        updateExistingLesson(existingLesson, lesson);

        return lessonProvider.save(existingLesson);
    }

    public Lesson getAsUser(long id, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        Optional<Lesson> lesson = lessonProvider.lessonOfId(id);

        if (lesson.isEmpty())
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id));

        // THE USER IS CONNECTED

        if (currentUserId != 0) {
            checkLessonAccessConditions(lesson.get(), currentUserId, lesson.get().getChapterId(), false, true);
            if (lesson.get().isNotEnrolledAccess()) lesson.get().setContent(return25PercentOfText(lesson.get().getContent()));
            return lesson.get();
        }

        // THE USER IS NOT CONNECTED

        Chapter chapter = chapterProvider.chapterOfId(lesson.get().getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("The chapter with id: " + lesson.get().getChapterId() + DOES_NOT_EXIST, String.valueOf(lesson.get().getChapterId())));

        long courseId = chapter.getCourseId();
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + courseId + DOES_NOT_EXIST, String.valueOf(courseId)));

        if (!Status.PUBLISHED.equals(existingCourse.getStatus())) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
        }

        if (!lesson.get().isFree()) {
            lesson.get().setNotEnrolledAccess(true);
            lesson.get().setContent(return25PercentOfText(lesson.get().getContent()));
        }

        return lesson.get();
    }

    public void delete(long lessonId, long currentUserId) throws ForbiddenActionException, ResourceNotFoundException {
        Optional<Lesson> lesson = lessonProvider.lessonOfId(lessonId);
        if (lesson.isEmpty()) {
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + lessonId + DOES_NOT_EXIST, String.valueOf(lessonId));
        }
        checkLessonAccessConditions(lesson.get(), currentUserId, lesson.get().getChapterId(), true, false);
        lessonProvider.delete(lesson.get());
    }

    private void checkLessonAccessConditions(Lesson lesson, long currentUserId, long chapterId, boolean isDeletion, boolean checkEnrollments) throws ResourceNotFoundException, ForbiddenActionException {
        User currentUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter chapter = chapterProvider.chapterOfId(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("The chapter with id: " + chapterId + DOES_NOT_EXIST, String.valueOf(chapterId)));

        long courseId = chapter.getCourseId();
        Course course = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + courseId + DOES_NOT_EXIST, String.valueOf(courseId)));

        if (!currentUser.isAdmin() && course.getAuthor().getId() != currentUser.getId() && !course.getStatus().equals(Status.PUBLISHED)) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
        }

        if (isDeletion && course.getStatus().equals(Status.PUBLISHED) && !currentUser.isAdmin())
            throw new ForbiddenActionException("You can not delete a lesson that is already published");

        if (!checkEnrollments && !currentUser.isAdmin() && course.getAuthor().getId() != currentUser.getId()) {
            throw new ForbiddenActionException("You are not allowed to do this action on this course");
        }

        if (checkEnrollments) {
            Optional<Enrollment> enrollment = enrollmentProvider.enrollmentOf(currentUserId, courseId, true);
            if (!currentUser.isAdmin() && course.getAuthor().getId() != currentUser.getId() && enrollment.isEmpty() && !lesson.isFree())
                lesson.setNotEnrolledAccess(true);
        }
    }

    private String return25PercentOfText(String text) {
        int halfLength = text.length() / 4;
        return text.substring(0, halfLength) + DOTS;
    }

    private void updateExistingLesson(Lesson existingLesson, Lesson lesson) {
        existingLesson.setId(lesson.getId());
        existingLesson.setTitle(lesson.getTitle());
        existingLesson.setContent(lesson.getContent());
        existingLesson.setSummary(lesson.getSummary());
        existingLesson.setThumbnail(lesson.getThumbnail());
        existingLesson.setVideo(lesson.getVideo());
        existingLesson.setFree(lesson.isFree());
        existingLesson.setType(lesson.getType());
        existingLesson.setChapterId(lesson.getChapterId());
    }
}
