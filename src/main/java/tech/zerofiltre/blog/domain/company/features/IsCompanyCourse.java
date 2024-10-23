package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
public class IsCompanyCourse {

    private final CompanyCourseProvider companyCourseProvider;

    public boolean execute(long companyId, long courseId) throws ResourceNotFoundException {
        getCompanyCourseIdIfCourseIsActive(companyId, courseId);
        return true;
    }

    public long getCompanyCourseIdIfCourseIsActive(long companyId, long courseId) throws ResourceNotFoundException {
        Optional<LinkCompanyCourse> companyCourse = companyCourseProvider.linkOf(companyId, courseId);

        if(companyCourse.isEmpty()) {
            throw new ResourceNotFoundException("We could not find the company course", "");
        }
        if(!companyCourse.get().isActive()) {
            throw new ResourceNotFoundException("This course is not active", "");
        }
        return companyCourse.get().getId();
    }
}
