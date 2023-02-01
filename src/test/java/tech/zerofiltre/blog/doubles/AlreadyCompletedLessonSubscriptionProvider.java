package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class AlreadyCompletedLessonSubscriptionProvider implements SubscriptionProvider {


    public void delete(long userId, long courseId) {

    }

    public Page<Subscription> of(int pageNumber, int pageSize, long authorId, FinderRequest.Filter filter, String tag) {
        return null;
    }

    public Optional<Subscription> subscriptionOf(long userId, long courseId, boolean isActive) {
        Subscription subscription1 = new Subscription();
        subscription1.setId(224);
        Lesson lesson = new Lesson.LessonBuilder().id(3).build();
        subscription1.getCompletedLessons().add(lesson);
        return Optional.of(subscription1);
    }

    public Subscription save(Subscription subscription) {
        return null;
    }
}
