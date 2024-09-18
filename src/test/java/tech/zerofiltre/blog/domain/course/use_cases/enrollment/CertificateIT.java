package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.storage.InMemoryCertificatesStorageProvider;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class CertificateIT {

    Certificate certificate;
    CompleteLesson completeLesson;
    InMemoryCertificatesStorageProvider inMemoryCertificatesStorageProvider;

    @Autowired
    SpringTemplateEngine templateEngine; // SpringTemplateEngine replace ITemplateEngine for test

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    DBChapterProvider dbChapterProvider;

    @Autowired
    DBLessonProvider dbLessonProvider;

    @Autowired
    DBEnrollmentProvider dbEnrollmentProvider;

    @BeforeEach
    void init() {
        inMemoryCertificatesStorageProvider = new InMemoryCertificatesStorageProvider();
        certificate = new Certificate(dbEnrollmentProvider, inMemoryCertificatesStorageProvider, dbCourseProvider, templateEngine);
        completeLesson = new CompleteLesson(dbEnrollmentProvider, dbLessonProvider, dbChapterProvider, dbCourseProvider);
    }

    @Test
    void givesCertificate_whenCertificateIsNotExistAndIsCreated() throws IOException, ZerofiltreException {
        //given
        User user = new User();
        user.setFullName("Testeur Humain");

        user = dbUserProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setStatus(Status.PUBLISHED);
        course.setAuthor(user);

        course = dbCourseProvider.save(course);

        Chapter chapter = new Chapter.ChapterBuilder()
                .courseId(course.getId())
                .build();

        chapter = dbChapterProvider.save(chapter);

        Lesson lesson1 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        Lesson lesson2 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        lesson1 = dbLessonProvider.save(lesson1);
        lesson2 = dbLessonProvider.save(lesson2);

        chapter.getLessons().add(lesson1);
        chapter.getLessons().add(lesson2);

        dbChapterProvider.save(chapter);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);

        dbEnrollmentProvider.save(enrollment);
        completeLesson.execute(course.getId(), lesson1.getId(), user.getId(), true);
        enrollment = completeLesson.execute(course.getId(), lesson2.getId(), user.getId(), true);
        CompletedLesson completedLesson1 = enrollment.getCompletedLessons().get(0);
        CompletedLesson completedLesson2 = enrollment.getCompletedLessons().get(1);

        assertThat(enrollment.getCompletedLessons()).isEqualTo(Arrays.asList(completedLesson1, completedLesson2));
        assertThat(enrollment.isCompleted()).isTrue();

        //when
        File response = certificate.giveCertificate(user, course.getId());

        //then
        assertThat(response.getName()).isEqualTo("Testeur_Humain_Cours_sur_les_tests.pdf");
    }

    @Test
    void givesCertificate_whenCertificateIsAlreadyCreated() throws IOException, ZerofiltreException {
        //given
        User user = new User();
        user.setFullName("Testeur Humain");

        user = dbUserProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setStatus(Status.PUBLISHED);
        course.setAuthor(user);

        course = dbCourseProvider.save(course);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setCompleted(true);

        dbEnrollmentProvider.save(enrollment);

        File file = new File("Testeur_Humain_Cours_sur_les_tests.pdf");
        inMemoryCertificatesStorageProvider.store(file);

        //when
        File response = certificate.giveCertificate(user, course.getId());

        //then
        assertThat(response.getName()).isEqualTo("Testeur_Humain_Cours_sur_les_tests.pdf");
    }
}