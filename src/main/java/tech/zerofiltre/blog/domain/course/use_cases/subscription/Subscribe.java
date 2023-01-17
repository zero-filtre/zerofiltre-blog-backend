package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class Subscribe {

    private final SubscriptionProvider subscriptionProvider;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;

    public Subscribe(SubscriptionProvider subscriptionProvider, CourseProvider courseProvider, UserProvider userProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
    }

    public Subscription execute(long userId, long courseId) throws ResourceNotFoundException, ForbiddenActionException {

        User user = userProvider.userOfId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We could not find the user with id " + userId,
                        String.valueOf(userId),
                        Domains.USER.name()));

        Course course = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We couldn't find the course of id " + courseId + " you are trying to subscribe to",
                        String.valueOf(courseId),
                        Domains.COURSE.name()));

        if (course.getStatus().compareTo(Status.PUBLISHED) < 0)
            throw new ForbiddenActionException("You can not subscribe to an unpublished course", Domains.COURSE.name());

        Subscription subscription = new Subscription();
        subscription.setCourse(course);
        subscription.setSubscriber(user);
        return subscriptionProvider.save(subscription);
    }
}
