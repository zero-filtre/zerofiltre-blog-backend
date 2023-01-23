package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "loggerProvider", "sectionProvider"})
public class Section {

    private long id;
    private int position;
    private String title;
    private String content;
    private String image;
    private long courseId;
    private SectionProvider sectionProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;
    private LoggerProvider loggerProvider;

    private Section(SectionBuilder sectionBuilder) {
        this.id = sectionBuilder.id;
        this.position = sectionBuilder.position;
        this.title = sectionBuilder.title;
        this.content = sectionBuilder.content;
        this.image = sectionBuilder.image;
        this.sectionProvider = sectionBuilder.sectionProvider;
        this.courseId = sectionBuilder.courseId;
        this.userProvider = sectionBuilder.userProvider;
        this.courseProvider = sectionBuilder.courseProvider;
        this.loggerProvider = sectionBuilder.loggerProvider;
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

    public Section save() {
        return setProviders(sectionProvider.save(this));
    }

    public Section update(long id, String title, String content, String image) throws ResourceNotFoundException {
        setAttributes(findById(id));
        return setAttributes(title, content, image).save();
    }

    private Section setAttributes(String title, String content, String image) {
        this.title = title;
        this.content = content;
        this.image = image;
        return this;
    }

    public Section findById(long id) throws ResourceNotFoundException {
        return setProviders(sectionProvider.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("The section with id: " + id + " does not exist", String.valueOf(id), Domains.COURSE.name())
        ));
    }

    public void delete(User deleter) throws ResourceNotFoundException, ForbiddenActionException {
        if (deleter == null)
            throw new ForbiddenActionException("You are not allowed to delete this section", Domains.COURSE.name());
        if (isAdmin(deleter)) {
            sectionProvider.delete(findById(id));
        } else {
            try {
                Course.builder()
                        .courseProvider(courseProvider)
                        .userProvider(userProvider).build().findById(courseId, deleter);
                sectionProvider.delete(findById(id));
            } catch (ForbiddenActionException e) {
                loggerProvider.log(new LogEntry(LogEntry.Level.DEBUG, "You can't delete a section belonging to a course you don't own", e, Section.class));
                throw e;
            }
        }
    }

    private boolean isAdmin(User deleter) {
        return deleter.getRoles().contains("ROLE_ADMIN");
    }

    private void setAttributes(Section section) {
        this.id = section.getId();
        this.position = section.getPosition();
        this.title = section.getTitle();
        this.content = section.getContent();
        this.image = section.getImage();
        this.courseId = section.getCourseId();
    }

    public static class SectionBuilder {
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

        public Section build() {
            return new Section(this);
        }
    }

    private Section setProviders(Section inNeedOfProviders) {
        inNeedOfProviders.sectionProvider = this.sectionProvider;
        inNeedOfProviders.courseProvider = this.courseProvider;
        inNeedOfProviders.loggerProvider = this.loggerProvider;
        inNeedOfProviders.userProvider = this.userProvider;
        return inNeedOfProviders;
    }
}
