package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tech.zerofiltre.blog.domain.Domains;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.SectionProvider;
import tech.zerofiltre.blog.domain.course.use_cases.course.CourseService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.io.Serializable;
import java.util.List;

import static tech.zerofiltre.blog.domain.Domains.COURSE;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.DOES_NOT_EXIST;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.THE_COURSE_WITH_ID;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "loggerProvider", "sectionProvider", "courseService"})
public class Section implements Serializable {

    private long id;
    private int position;
    private String title;
    private String content;
    private String image;
    private long courseId;
    private SectionProvider sectionProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;
    private CourseService courseService;
    private LoggerProvider loggerProvider;

    private Section(SectionBuilder sectionBuilder) {
        this.id = sectionBuilder.id;
        this.position = sectionBuilder.position;
        this.title = sectionBuilder.title;
        this.content = sectionBuilder.content;
        this.image = sectionBuilder.image;
        this.sectionProvider = sectionBuilder.sectionProvider;
        this.courseId = sectionBuilder.courseId;
        TagProvider tagProvider = sectionBuilder.tagProvider;
        ChapterProvider chapterProvider = sectionBuilder.chapterProvider;
        this.userProvider = sectionBuilder.userProvider;
        this.courseProvider = sectionBuilder.courseProvider;
        this.loggerProvider = sectionBuilder.loggerProvider;
        courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
    }

    public static SectionBuilder builder() {
        return new SectionBuilder();
    }

    public long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImage() {
        return image;
    }

    public long getCourseId() {
        return courseId;
    }

    public SectionProvider getSectionProvider() {
        return sectionProvider;
    }

    private static void shiftPositions(int newPosition, Section sectionToMove, List<Section> sections) {
        int currentPosition = sectionToMove.getPosition();
        for (Section aSection : sections) {
            if (aSection.getId() == sectionToMove.getId()) {
                // Move the section to the new position
                aSection.position = newPosition;
            } else {
                int aSectionPosition = aSection.getPosition();
                if (currentPosition < newPosition) {
                    // Shift sections down
                    if (aSectionPosition > currentPosition && aSectionPosition <= newPosition) {
                        aSection.position = aSectionPosition - 1;
                    }
                } else {
                    // Shift sections up
                    if (aSectionPosition < currentPosition && aSectionPosition >= newPosition) {
                        aSection.position = aSectionPosition + 1;
                    }
                }
            }
        }
    }

    private static void checkRoles(User currentUser, Course existingCourse) throws ForbiddenActionException {
        if (currentUser == null || (!currentUser.isAdmin() && existingCourse.getAuthor().getId() != currentUser.getId())) {
            throw new ForbiddenActionException("You are not allowed to edit a section for this course", COURSE.name());
        }
    }

    public Section findById(long id) throws ResourceNotFoundException {
        return setProviders(sectionProvider.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("The section with id: " + id + " does not exist", String.valueOf(id), Domains.COURSE.name())
        ));
    }

    public void delete(User deleter) throws ResourceNotFoundException, ForbiddenActionException {
        if (deleter == null)
            throw new ForbiddenActionException("You are not allowed to delete this section", Domains.COURSE.name());
        if (deleter.isAdmin()) {
            sectionProvider.delete(findById(id));
        } else {
            try {
                Section existingSection = findById(id);
                courseService.findById(existingSection.getCourseId(), deleter);
                sectionProvider.delete(existingSection);
            } catch (ForbiddenActionException e) {
                loggerProvider.log(new LogEntry(LogEntry.Level.DEBUG, "You can't delete a section belonging to a course you don't own", e, Section.class));
                throw e;
            }
        }
    }

    public Section init(User currentUser) throws ResourceNotFoundException, ForbiddenActionException {
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + courseId + DOES_NOT_EXIST, String.valueOf(courseId), Domains.COURSE.name()));

        checkRoles(currentUser, existingCourse);

        Section lastSection = existingCourse.getSections().stream().reduce((first, second) -> second).orElse(null);
        this.position = (lastSection != null) ? lastSection.getPosition() + 1 : 1;
        return setProviders(sectionProvider.save(this));
    }

    public Section update(long id, String title, String content, String image, int position, User currentUser) throws ZerofiltreException {
        Section existingSection = findById(id);

        Course existingCourse = courseProvider.courseOfId(existingSection.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + existingSection.getCourseId() + DOES_NOT_EXIST, String.valueOf(existingSection.getCourseId()), Domains.COURSE.name()));

        checkRoles(currentUser, existingCourse);

        setAttributes(existingSection);
        int currentPosition = existingSection.getPosition();
        if (currentPosition == position) return setAttributes(title, content, image, position).save();


        List<Section> sections = existingCourse.getSections();
        shiftPositions(position, existingSection, sections);
        sections.forEach(section -> {
            if (section.getId() == id) {
                setAttributes(title, content, image, position).save();
            } else {
                sectionProvider.save(section);
            }
        });
        return this;
    }

    private Section save() {
        return setProviders(sectionProvider.save(this));
    }

    private Section setAttributes(String title, String content, String image, int position) {
        this.title = title;
        this.content = content;
        this.image = image;
        this.position = position;
        return this;
    }

    private void setAttributes(Section section) {
        this.id = section.getId();
        this.position = section.getPosition();
        this.title = section.getTitle();
        this.content = section.getContent();
        this.image = section.getImage();
        this.courseId = section.getCourseId();
    }

    public CourseService getCourseService() {
        return courseService;
    }

    public static class SectionBuilder {
        private ChapterProvider chapterProvider;
        private TagProvider tagProvider;
        private long courseId;
        private UserProvider userProvider;
        private CourseProvider courseProvider;
        private LoggerProvider loggerProvider;
        private long id;
        private int position;
        private String title;
        private String content;
        private String image;
        private SectionProvider sectionProvider;

        public SectionBuilder id(long id) {
            this.id = id;
            return this;
        }

        public SectionBuilder position(int position) {
            this.position = position;
            return this;
        }

        public SectionBuilder title(String title) {
            this.title = title;
            return this;
        }

        public SectionBuilder content(String content) {
            this.content = content;
            return this;
        }

        public SectionBuilder image(String image) {
            this.image = image;
            return this;
        }

        public SectionBuilder courseId(long courseId) {
            this.courseId = courseId;
            return this;
        }

        public SectionBuilder sectionProvider(SectionProvider sectionProvider) {
            this.sectionProvider = sectionProvider;
            return this;
        }

        public SectionBuilder userProvider(UserProvider userProvider) {
            this.userProvider = userProvider;
            return this;
        }

        public SectionBuilder courseProvider(CourseProvider courseProvider) {
            this.courseProvider = courseProvider;
            return this;
        }

        public SectionBuilder loggerProvider(LoggerProvider loggerProvider) {
            this.loggerProvider = loggerProvider;
            return this;
        }

        public SectionBuilder tagProvider(TagProvider tagProvider) {
            this.tagProvider = tagProvider;
            return this;
        }

        public SectionBuilder chapterProvider(ChapterProvider chapterProvider) {
            this.chapterProvider = chapterProvider;
            return this;
        }

        public Section build() {
            return new Section(this);
        }
    }

    private Section setProviders(Section inNeedOfProviders) {
        inNeedOfProviders.sectionProvider = this.sectionProvider;
        inNeedOfProviders.courseProvider = this.courseProvider;
        inNeedOfProviders.loggerProvider = this.loggerProvider;
        inNeedOfProviders.userProvider = this.userProvider;
        inNeedOfProviders.courseService = this.courseService;
        return inNeedOfProviders;
    }
}
