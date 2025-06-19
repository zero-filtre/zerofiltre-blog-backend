package tech.zerofiltre.blog.domain.course.features.course;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;

public class CourseService {

    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_COURSE_WITH_ID = "The course with id: ";

    private final CourseProvider courseProvider;

    private final TagProvider tagProvider;
    private final LoggerProvider loggerProvider;

    private final DataChecker checker;
    private final CompanyCourseService companyCourseService;
    private final CompanyCourseProvider companyCourseProvider;

    public CourseService(CourseProvider courseProvider, TagProvider tagProvider, LoggerProvider loggerProvider, DataChecker checker, CompanyCourseProvider companyCourseProvider, EnrollmentProvider enrollmentProvider) {
        this.courseProvider = courseProvider;
        this.tagProvider = tagProvider;
        this.loggerProvider = loggerProvider;
        this.checker = checker;
        this.companyCourseProvider = companyCourseProvider;
        this.companyCourseService = new CompanyCourseService(companyCourseProvider, enrollmentProvider, checker);
    }


    public Course init(String title, User author, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        boolean isCompanyCourse = false;

        if (companyId > 0) {
            checker.checkCompanyExistence(companyId);
            checker.checkIfAdminOrCompanyAdminOrEditor(author, companyId);
            isCompanyCourse = true;
        }

        Course course = new Course();
        course.setTitle(title);
        course.setAuthor(author);
        course.setCreatedAt(LocalDateTime.now());
        course.setLastSavedAt(course.getCreatedAt());
        course = courseProvider.save(course);

        if (isCompanyCourse)
            companyCourseProvider.save(new LinkCompanyCourse(0, companyId, course.getId(), true, true, LocalDateTime.now(), null));

        return course;
    }

    public Course findById(long id, User viewer) throws ResourceNotFoundException, ForbiddenActionException {
        Course foundCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        if ((viewer == null && Status.PUBLISHED != foundCourse.getStatus())
                || (viewer != null && !viewer.isAdmin() && isNotAuthor(viewer, foundCourse) && foundCourse.getStatus() != Status.PUBLISHED)) {
            throw new ForbiddenActionException("You are not allowed to access this course (that you do not own) as it is not yet published");
        }
        return foundCourse;
    }

    public Course findByIdAndCompanyId(long id, User viewer, long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        LinkCompanyCourse link = companyCourseService.find(viewer, companyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("No link between the course: " + id + " and the company: " + companyId, String.valueOf(id), String.valueOf(companyId)));

        return courseProvider.courseOfId(link.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));
    }

    public Course save(Course updatedCourse, User currentEditor) throws ResourceNotFoundException, ForbiddenActionException {

        Status statusToSave = updatedCourse.getStatus();
        LocalDateTime now = LocalDateTime.now();

        long updatedCourseId = updatedCourse.getId();
        Course existingCourse = courseProvider.courseOfId(updatedCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + updatedCourseId + DOES_NOT_EXIST, String.valueOf(updatedCourseId)));

        Optional<Long> companyId = courseProvider.idOfCompanyOwningCourse(existingCourse.getId());

        if(companyId.isEmpty()) {
            prepareExistingPlatformCourseForSaving(existingCourse, currentEditor, updatedCourse, statusToSave);
        } else {
            prepareExistingCompanyCourseForSaving(companyId.get(), existingCourse, currentEditor, updatedCourse, statusToSave);
        }

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
        return courseProvider.save(existingCourse);
    }

    public void delete(long id, User deleter) throws ResourceNotFoundException, ForbiddenActionException {
        Course existingCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        Optional<Long> companyId = courseProvider.idOfCompanyOwningCourse(existingCourse.getId());

        if(companyId.isEmpty()) {
            checkConditionsForDeletingPlatformCourse(existingCourse, deleter);
        } else {
            checkConditionsForDeletingCompanyCourse(companyId.get(), existingCourse, deleter);
        }

        courseProvider.delete(existingCourse);

        LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting course " + id + " for done", null, Course.class);
        loggerProvider.log(logEntry);
    }

    public Page<Course> of(FinderRequest request) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = request.getUser();

        boolean unAuthenticatedUserGettingNonPublishedCourses = !PUBLISHED.equals(request.getStatus())
                && user == null
                && !request.isYours();

        if (unAuthenticatedUserGettingNonPublishedCourses) {
            throw new UnAuthenticatedActionException("The user token might be expired, try to refresh it. ");
        }

        boolean nonAdminGettingNonPublishedCourses = !PUBLISHED.equals(request.getStatus())
                && (user == null || !user.getRoles().contains("ROLE_ADMIN"))
                && !request.isYours();

        if (nonAdminGettingNonPublishedCourses) {
            throw new ForbiddenActionException("You are not authorize to request courses other than the published ones with this API. " +
                    "Please request with status=published or try /user/courses/* API resources");
        }

        long authorId = request.isYours() ? request.getUser().getId() : 0;
        return courseProvider.courseOf(request.getPageNumber(), request.getPageSize(), request.getStatus(), authorId, request.getFilter(), request.getTag());
    }


    private boolean isNotAuthor(User currentEditor, Course course) {
        return !course.getAuthor().getEmail().equals(currentEditor.getEmail());
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
                throw new ResourceNotFoundException("We can not publish this course. Could not find the related tag with id: " + tag.getId(), String.valueOf(tag.getId()));
        }
    }

    private void prepareExistingPlatformCourseForSaving(Course existingCourse, User currentEditor, Course updatedCourse, Status statusToSave) throws ForbiddenActionException {
        if (isNotAuthor(currentEditor, existingCourse) && !currentEditor.isAdmin())
            throw new ForbiddenActionException("You are not allowed to edit this course");

        if (!isAlreadyPublished(existingCourse.getStatus()) && isTryingToPublish(statusToSave) && !currentEditor.isAdmin())
            existingCourse.setStatus(Status.IN_REVIEW);

        if (!isAlreadyPublished(existingCourse.getStatus()) && (!isTryingToPublish(statusToSave) || currentEditor.isAdmin()))
            existingCourse.setStatus(updatedCourse.getStatus());
    }

    private void prepareExistingCompanyCourseForSaving(long companyId, Course existingCourse, User currentEditor, Course updatedCourse, Status statusToSave) throws ForbiddenActionException {
        checker.checkIfAdminOrCompanyAdminOrEditor(currentEditor, companyId);

        boolean isAdminOrCompanyAdmin = checker.isAdminOrCompanyAdmin(currentEditor, companyId);

        if (!isAlreadyPublished(existingCourse.getStatus()) && isTryingToPublish(statusToSave) && !isAdminOrCompanyAdmin)
            existingCourse.setStatus(Status.IN_REVIEW);

        if (!isAlreadyPublished(existingCourse.getStatus()) && (!isTryingToPublish(statusToSave) || isAdminOrCompanyAdmin))
            existingCourse.setStatus(updatedCourse.getStatus());

        if (isAlreadyPublished(existingCourse.getStatus()) && !isAdminOrCompanyAdmin)
            throw new ForbiddenActionException("You do not have permission to modify a published course.");
    }

    private void checkConditionsForDeletingPlatformCourse(Course existingCourse, User deleter) throws ForbiddenActionException {
        if (existingCourse.getStatus().equals(Status.PUBLISHED))
            throw new ForbiddenActionException("You are not allowed to delete this course as it is published");

        if (existingCourse.getEnrolledCount() > 0)
            throw new ForbiddenActionException("You are not allowed to delete this course as it has enrolled users");

        if (isNotAuthor(deleter, existingCourse) && !deleter.isAdmin())
            throw new ForbiddenActionException("You are not allowed to delete this course");
    }

    private void checkConditionsForDeletingCompanyCourse(long companyId, Course existingCourse, User deleter) throws ForbiddenActionException {
        checker.checkIfAdminOrCompanyAdminOrEditor(deleter, companyId);

        if (existingCourse.getStatus().equals(Status.PUBLISHED) && !checker.isAdminOrCompanyAdmin(deleter, companyId))
            throw new ForbiddenActionException("You are not allowed to delete this company course as it is published");
    }

}
