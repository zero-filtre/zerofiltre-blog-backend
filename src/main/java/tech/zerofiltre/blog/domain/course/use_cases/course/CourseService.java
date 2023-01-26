package tech.zerofiltre.blog.domain.course.use_cases.course;

import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

import static tech.zerofiltre.blog.domain.article.model.Status.*;

@Component
public class CourseService {

    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_COURSE_WITH_ID = "The course with id: ";

    private final CourseProvider courseProvider;

    private final TagProvider tagProvider;
    private final LoggerProvider loggerProvider;
    private final ChapterProvider chapterProvider;


    public CourseService(CourseProvider courseProvider, TagProvider tagProvider, LoggerProvider loggerProvider, ChapterProvider chapterProvider) {
        this.courseProvider = courseProvider;
        this.tagProvider = tagProvider;
        this.loggerProvider = loggerProvider;
        this.chapterProvider = chapterProvider;
    }


    public Course init(String title, User author) {
        Course course = new Course();
        course.setTitle(title);
        course.setAuthor(author);
        course.setCreatedAt(LocalDateTime.now());
        course.setLastSavedAt(course.getCreatedAt());
        return courseProvider.save(course);
    }

    public Course findById(long id, User viewer) throws ResourceNotFoundException, ForbiddenActionException {
        Course foundCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));


        if ((viewer == null && Status.PUBLISHED != foundCourse.getStatus())
                || (viewer != null && !isAdmin(viewer) && isNotAuthor(viewer, foundCourse) && foundCourse.getStatus() != Status.PUBLISHED)) {
            throw new ForbiddenActionException("You are not allowed to access this course (that you do not own) as it is not yet published", Domains.COURSE.name());
        }
        foundCourse.setLessonsCount(getLessonsCount(foundCourse.getId()));
        return foundCourse;
    }

    public Course save(Course updatedCourse, User currentEditor) throws ResourceNotFoundException, ForbiddenActionException {

        Status statusToSave = updatedCourse.getStatus();
        LocalDateTime now = LocalDateTime.now();

        long updatedCourseId = updatedCourse.getId();
        Course existingCourse = courseProvider.courseOfId(updatedCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + updatedCourseId + DOES_NOT_EXIST, String.valueOf(updatedCourseId), Domains.COURSE.name()));
        if (isNotAuthor(currentEditor, existingCourse) && !isAdmin(currentEditor))
            throw new ForbiddenActionException("You are not allowed to edit this course", Domains.COURSE.name());

        if (!isAlreadyPublished(existingCourse.getStatus()) && isTryingToPublish(statusToSave) && !isAdmin(currentEditor))
            existingCourse.setStatus(Status.IN_REVIEW);

        if (!isAlreadyPublished(existingCourse.getStatus()) && (!isTryingToPublish(statusToSave) || isAdmin(currentEditor)))
            existingCourse.setStatus(updatedCourse.getStatus());

        if (isAlreadyPublished(existingCourse.getStatus())) {
            if (existingCourse.getPublishedAt() == null) existingCourse.setPublishedAt(now);
            existingCourse.setLastPublishedAt(now);
        }

        existingCourse.setLastSavedAt(now);
        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setSubTitle(updatedCourse.getSubTitle());
        existingCourse.setSummary(updatedCourse.getSummary());
        existingCourse.setThumbnail(updatedCourse.getThumbnail());
        existingCourse.setVideo(updatedCourse.getVideo());
        checkTags(updatedCourse.getTags());
        existingCourse.setTags(updatedCourse.getTags());
        Course result = courseProvider.save(existingCourse);
        result.setLessonsCount(getLessonsCount(result.getId()));
        return result;
    }

    public void delete(long id, User deleter) throws ResourceNotFoundException, ForbiddenActionException {

        Course existingCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (isNotAuthor(deleter, existingCourse) && !isAdmin(deleter))
            throw new ForbiddenActionException("You are not allowed to delete this course", Domains.COURSE.name());
        courseProvider.delete(existingCourse);

        LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting course " + id + " for done", null, Course.class);
        loggerProvider.log(logEntry);
    }

    public Page<Course> of(FinderRequest request) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = request.getUser();

        //UNAUTHENTICATED USER TRYING TO GET NON PUBLISHED COURSES
        if (!PUBLISHED.equals(request.getStatus())
                && user == null
                && !request.isYours()) {
            throw new UnAuthenticatedActionException("The user token might be expired, try to refresh it. ", Domains.ARTICLE.name());
        }

        //NON ADMIN USER TRYING TO GET NON PUBLISHED COURSES
        if (!PUBLISHED.equals(request.getStatus())
                && (user == null || !user.getRoles().contains("ROLE_ADMIN"))
                && !request.isYours()) {
            throw new ForbiddenActionException("You are not authorize to request courses other than the published ones with this API. " +
                    "Please request with status=published or try /user/courses/* API resources", Domains.COURSE.name());
        }

        long authorId = request.isYours() ? request.getUser().getId() : 0;
        Page<Course> courses = courseProvider.courseOf(request.getPageNumber(), request.getPageSize(), request.getStatus(), authorId, request.getFilter(), request.getTag());
        courses.getContent().forEach(course -> course.setLessonsCount(getLessonsCount(course.getId())));
        return courses;
    }


    private boolean isNotAuthor(User currentEditor, Course course) {
        return !course.getAuthor().getEmail().equals(currentEditor.getEmail());
    }

    private boolean isAdmin(User existingUser) {
        return existingUser.getRoles().contains("ROLE_ADMIN");
    }

    private boolean isAlreadyPublished(Status status) {
        return status.equals(Status.PUBLISHED);
    }


    private boolean isTryingToPublish(Status status) {
        return status == Status.PUBLISHED || status == Status.IN_REVIEW;
    }

    private void checkTags(List<Tag> tags) throws ResourceNotFoundException {
        for (Tag tag : tags) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new ResourceNotFoundException("We can not publish this course. Could not find the related tag with id: " + tag.getId(), String.valueOf(tag.getId()), Domains.COURSE.name());
        }
    }

    public int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


}
