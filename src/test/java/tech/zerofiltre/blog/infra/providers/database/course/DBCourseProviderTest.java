package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class DBCourseProviderTest {

    @Mock
    CourseJPARepository courseJPARepository;

    @Test
    void givenAuthorIdIsZeroAndTagIsNotNull_whenCourseOf_ThenCall_findByStatusAndTagsName() {

        Mockito.when(courseJPARepository.findByStatusAndTagsName(any(), any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 0, FinderRequest.Filter.MOST_VIEWED, "tag");

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByStatusAndTagsName(any(), any(), any());

    }

    @Test
    void givenAuthorIdIsZero_TagIsNull_AndFilterPopular_whenCourseOf_ThenCall_findByReactionsDesc() {

        Mockito.when(courseJPARepository.findByReactionsDesc(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 0, FinderRequest.Filter.POPULAR, null);

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByReactionsDesc(any(), any());

    }

    @Test
    void givenAuthorIdIsZero_TagIsNull_AndFilterMostViewed_whenCourseOf_ThenCall_findByEnrolledDesc() {

        Mockito.when(courseJPARepository.findByEnrolledDesc(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 0, FinderRequest.Filter.MOST_VIEWED, null);

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByEnrolledDesc(any(), any());

    }

    @Test
    void givenAuthorIdIsZero_TagIsNull_AndFilterNotKnown_thenCall_findByStatus() {

        Mockito.when(courseJPARepository.findByStatus(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 0, null, null);

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByStatus(any(), any());

    }



//    @Test
//    void givenAuthorIdIsNotZero_TagNotNull_whenCourseOf_ThenCall_findByStatusAndAuthorIdAndTagsName() {
//
//        Mockito.when(courseJPARepository.findByStatusAndAuthorIdAndTagsName(any(), any(), any(),any())).thenReturn(new PageImpl<>(new ArrayList<>()));
//        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
//        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 1, FinderRequest.Filter.MOST_VIEWED, "tag");
//
//        Mockito.verify(courseJPARepository, Mockito.times(1)).findByStatusAndAuthorIdAndTagsName(any(), any(), any(),any());
//
//    }

    @Test
    void givenAuthorIdIsNotZero_TagIsNull_AndFilterMostViewed_whenCourseOf_ThenCall_findByEnrolledAndAuthorIdDesc() {

        Mockito.when(courseJPARepository.findByEnrolledAndAuthorIdDesc(any(), any(), anyLong())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 1, FinderRequest.Filter.MOST_VIEWED, null);

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByEnrolledAndAuthorIdDesc(any(), any(), anyLong());

    }

    @Test
    void givenAuthorIdIsNotZero_TagIsNull_AndFilterPopular_whenCourseOf_ThenCall_findByReactionsAndAuthorIdDesc() {

        Mockito.when(courseJPARepository.findByReactionsAndAuthorIdDesc(any(), any(), anyLong())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 1, FinderRequest.Filter.POPULAR, null);

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByReactionsAndAuthorIdDesc(any(), any(), anyLong());

    }

    @Test
    void givenAuthorIdIsNotZero_TagIsNull_AndFilterNotKnown_thenCall_findByStatusAndAuthorId() {

        Mockito.when(courseJPARepository.findByStatusAndAuthorId(any(), any(), anyLong())).thenReturn(new PageImpl<>(new ArrayList<>()));
        DBCourseProvider dbCourseProvider = new DBCourseProvider(courseJPARepository);
        dbCourseProvider.courseOf(0, 2, Status.PUBLISHED, 1, null, null);

        Mockito.verify(courseJPARepository, Mockito.times(1)).findByStatusAndAuthorId(any(), any(), anyLong());

    }

}