package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

import java.time.*;
import java.util.*;

import static tech.zerofiltre.blog.domain.article.model.Status.*;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "loggerProvider", "tagProvider", "loggerProvider", "sectionProvider"})
public class Course {
    public static final String AUTHOR_DOES_NOT_EXIST = "The author does not exist";
    public static final String USER_DOES_NOT_EXIST = "The user does not exist";
    public static final String DOES_NOT_EXIST = " does not exist";


    private long id;
    private String subTitle;
    private String summary;
    private String thumbnail;
    private String firstLessonId;
    private List<Tag> tags;
    private long enrolledCount;
    private String title;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastSavedAt;
    private User author;
    private double price;
    private String video;
    private List<Section> sections;

    private CourseProvider courseProvider;
    private UserProvider userProvider;
    private TagProvider tagProvider;
    private LoggerProvider loggerProvider;
    private SectionProvider sectionProvider;

    private Course(CourseBuilder courseBuilder) {
        this.id = courseBuilder.id;
        this.title = courseBuilder.title;
        this.subTitle = courseBuilder.subTitle;
        this.summary = courseBuilder.summary;
        this.thumbnail = courseBuilder.thumbnail;
        this.firstLessonId = courseBuilder.firstLessonId;
        this.tags = courseBuilder.tags;
        this.enrolledCount = courseBuilder.enrolledCount;
        this.author = courseBuilder.author;
        this.price = courseBuilder.price;
        this.video = courseBuilder.video;
        this.sections = courseBuilder.sections;
        this.status = courseBuilder.status;
        this.lastPublishedAt = courseBuilder.lastPublishedAt;
        this.publishedAt = courseBuilder.publishedAt;
        this.createdAt = courseBuilder.createdAt;
        this.lastSavedAt = courseBuilder.lastSavedAt;
        this.courseProvider = courseBuilder.courseProvider;
        this.userProvider = courseBuilder.userProvider;
        this.tagProvider = courseBuilder.tagProvider;
        this.sectionProvider = courseBuilder.sectionProvider;
        this.loggerProvider = courseBuilder.loggerProvider;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getSummary() {
        return summary;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getFirstLessonId() {
        return firstLessonId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public User getAuthor() {
        return author;
    }

    public double getPrice() {
        return price;
    }

    public String getVideo() {
        return video;
    }

    public List<Section> getSections() {
        return sections;
    }

    public CourseProvider getCourseProvider() {
        return courseProvider;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getLastPublishedAt() {
        return lastPublishedAt;
    }

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public TagProvider getTagProvider() {
        return tagProvider;
    }

    public SectionProvider getSectionProvider() {
        return sectionProvider;
    }

    public LoggerProvider getLoggerProvider() {
        return loggerProvider;
    }

    public static CourseBuilder builder() {
        return new CourseBuilder();
    }

    public Course init(String title, User author) throws UserNotFoundException {
        User existingUser = userProvider.userOfId(author.getId())
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, author.toString()));
        this.title = title;
        this.author = existingUser;
        this.createdAt = LocalDateTime.now();
        this.lastSavedAt = createdAt;
        return setProviders(courseProvider.save(this));
    }

    public Course findById(long id, User viewer) throws ResourceNotFoundException, ForbiddenActionException {
        Course foundCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (viewer == null) {
            setAttributes(foundCourse);
            if (status != Status.PUBLISHED)
                throw new ForbiddenActionException("You are not allowed to access this course as it is not yet published", Domains.COURSE.name());
            return setProviders(foundCourse);
        }

        Optional<User> existing = userProvider.userOfId(viewer.getId());
        if (existing.isEmpty())
            throw new UserNotFoundException(USER_DOES_NOT_EXIST, viewer.toString());
        setAttributes(foundCourse);
        if (!isAdmin(existing.get()) && !isAuthor(existing.get()) && status != Status.PUBLISHED)
            throw new ForbiddenActionException("You are not allowed to access this course as it is not yet published", Domains.COURSE.name());


        return setProviders(foundCourse);
    }

    public Course save(long currentEditorId) throws ResourceNotFoundException, ForbiddenActionException {
        String titleToSave = this.title;
        String subTitleToSave = this.subTitle;
        String summaryToSave = this.summary;
        String thumbnailToSave = this.thumbnail;
        String videoToSave = this.video;
        List<Section> sectionsToSave = this.sections;
        List<Tag> tagsToSave = this.tags;
        Status statusToSave = this.status;
        LocalDateTime now = LocalDateTime.now();

        User currentEditor = userProvider.userOfId(currentEditorId)
                .orElseThrow(() -> new UserNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(currentEditorId)));

        Course existingCourse = courseProvider.courseOfId(this.id)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + this.id + DOES_NOT_EXIST, String.valueOf(this.id), Domains.COURSE.name()));
        setAttributes(existingCourse);
        setAttributes(titleToSave, subTitleToSave, summaryToSave, videoToSave, thumbnailToSave, sectionsToSave, tagsToSave);
        if (!isAuthor(currentEditor) && !isAdmin(currentEditor))
            throw new ForbiddenActionException("You are not allowed to edit this course", Domains.COURSE.name());

        if (!isAlreadyPublished(existingCourse.getStatus()) && isTryingToPublish(statusToSave) && !isAdmin(currentEditor))
            this.status = Status.IN_REVIEW;

        if (!isAlreadyPublished(existingCourse.getStatus()) && (!isTryingToPublish(statusToSave) || isAdmin(currentEditor)))
            this.status = statusToSave;

        if (isAlreadyPublished(existingCourse.getStatus())) {
            if (this.publishedAt == null) publishedAt = now;
            this.lastPublishedAt = now;
        }
        this.lastSavedAt = now;
        return setProviders(courseProvider.save(this));
    }


    public void delete(long id, User deleter) throws ResourceNotFoundException, ForbiddenActionException {
        Optional<User> existing = userProvider.userOfId(deleter.getId());
        if (existing.isEmpty())
            throw new UserNotFoundException(USER_DOES_NOT_EXIST, deleter.toString());

        Course existingCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (!existingCourse.isAuthor(existing.get()) && !isAdmin(existing.get()))
            throw new ForbiddenActionException("You are not allowed to delete this course", Domains.COURSE.name());
        courseProvider.delete(existingCourse);

        LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting course " + id + " for done", null, Course.class);
        loggerProvider.log(logEntry);
    }

    private boolean isTryingToPublish(Status status) {
        return status == Status.PUBLISHED || status == Status.IN_REVIEW;
    }


    private Course setProviders(Course inNeedOfProviders) {
        inNeedOfProviders.sectionProvider = this.sectionProvider;
        inNeedOfProviders.courseProvider = this.courseProvider;
        inNeedOfProviders.userProvider = this.userProvider;
        inNeedOfProviders.tagProvider = this.tagProvider;
        inNeedOfProviders.loggerProvider = this.loggerProvider;
        return inNeedOfProviders;
    }

    private boolean isAuthor(User currentEditor) {
        return author.getEmail().equals(currentEditor.getEmail());
    }

    private boolean isAdmin(User existingUser) {
        return existingUser.getRoles().contains("ROLE_ADMIN");
    }

    private boolean isAlreadyPublished(Status status) {
        return status.equals(Status.PUBLISHED);
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    private void setAttributes(String title, String subTitle, String summary, String video, String thumbnail, List<Section> sections, List<Tag> tags) throws ResourceNotFoundException {
        checkTags(tags);
        checkSections(sections);
        this.title = title;
        this.subTitle = subTitle;
        this.summary = summary;
        this.video = video;
        this.thumbnail = thumbnail;
        this.sections = sections;
        this.tags = tags;
    }

    private void checkSections(List<Section> sections) throws ResourceNotFoundException {
        for (Section section : sections) {
            Optional<Section> existingSection = sectionProvider.findById(section.getId());
            if (existingSection.isEmpty())
                throw new ResourceNotFoundException("The section with id: " + section.getId() + DOES_NOT_EXIST, String.valueOf(section.getId()), Domains.COURSE.name());
        }
    }

    private void setAttributes(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.subTitle = course.getSubTitle();
        this.summary = course.getSummary();
        this.thumbnail = course.getThumbnail();
        this.firstLessonId = course.getFirstLessonId();
        this.tags = course.getTags();
        this.enrolledCount = course.getEnrolledCount();
        this.author = course.getAuthor();
        this.price = course.getPrice();
        this.video = course.getVideo();
        this.status = course.getStatus();
        this.createdAt = course.getCreatedAt();
        this.publishedAt = course.getPublishedAt();
        this.lastPublishedAt = course.getLastPublishedAt();
        this.lastSavedAt = course.getLastSavedAt();
    }

    private void checkTags(List<Tag> tags) throws ResourceNotFoundException {
        for (Tag tag : tags) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new ResourceNotFoundException("We can not publish this course. Could not find the related tag with id: " + tag.getId(), String.valueOf(tag.getId()), Domains.COURSE.name());
        }
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
        return courseProvider.courseOf(request.getPageNumber(), request.getPageSize(), request.getStatus(), authorId, request.getFilter(), request.getTag());
    }

    public static class CourseBuilder {
        private SectionProvider sectionProvider;
        private LoggerProvider loggerProvider;
        private Status status = Status.DRAFT;
        private UserProvider userProvider;
        private TagProvider tagProvider;
        private long id;
        private String title = "Donnez-moi un titre!";
        private String subTitle = "Donnez-moi un sous-titre!";
        private String summary;
        private String thumbnail;
        private String firstLessonId;
        private List<Tag> tags = new ArrayList<>();
        private long enrolledCount;
        private User author;
        private double price;
        private String video;
        private LocalDateTime createdAt;
        private LocalDateTime publishedAt;
        private LocalDateTime lastPublishedAt;
        private LocalDateTime lastSavedAt;
        private List<Section> sections = new ArrayList<>();
        private CourseProvider courseProvider;

        public CourseBuilder id(long id) {
            this.id = id;
            return this;
        }

        public CourseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CourseBuilder subTitle(String subTitle) {
            this.subTitle = subTitle;
            return this;
        }

        public CourseBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public CourseBuilder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public CourseBuilder firstLessonId(String firstLessonId) {
            this.firstLessonId = firstLessonId;
            return this;
        }

        public CourseBuilder tags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public CourseBuilder enrolledCount(long enrolledCount) {
            this.enrolledCount = enrolledCount;
            return this;
        }

        public CourseBuilder author(User author) {
            this.author = author;
            return this;
        }

        public CourseBuilder price(double price) {
            this.price = price;
            return this;
        }

        public CourseBuilder video(String video) {
            this.video = video;
            return this;
        }

        public CourseBuilder sections(List<Section> sections) {
            this.sections = sections;
            return this;
        }

        public CourseBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public CourseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CourseBuilder courseProvider(CourseProvider courseProvider) {
            this.courseProvider = courseProvider;
            return this;
        }

        public CourseBuilder publishedAt(LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public CourseBuilder lastPublishedAt(LocalDateTime lastPublishedAt) {
            this.lastPublishedAt = lastPublishedAt;
            return this;
        }

        public CourseBuilder lastSavedAt(LocalDateTime lastSavedAt) {
            this.lastSavedAt = lastSavedAt;
            return this;
        }

        public CourseBuilder userProvider(UserProvider userProvider) {
            this.userProvider = userProvider;
            return this;
        }

        public CourseBuilder tagProvider(TagProvider tagProvider) {
            this.tagProvider = tagProvider;
            return this;
        }

        public CourseBuilder sectionProvider(SectionProvider sectionProvider) {
            this.sectionProvider = sectionProvider;
            return this;
        }

        public CourseBuilder loggerProvider(LoggerProvider loggerProvider) {
            this.loggerProvider = loggerProvider;
            return this;
        }

        public Course build() {
            return new Course(this);
        }
    }
}
