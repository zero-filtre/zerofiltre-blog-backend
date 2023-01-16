package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class FoundSectionProviderSpy implements SectionProvider {
    public boolean findByIdCalled;
    public boolean saveCalled;
    public boolean deleteCalled;
    @Override
    public Optional<Section> findById(long id) {
        findByIdCalled = true;
        return Optional.ofNullable(new Section.SectionBuilder().id(1L).position(1).title("title").content("content").image("image").build());

    }

    @Override
    public Section save(Section section) {
        saveCalled = true;
        return section;
    }

    @Override
    public void delete(Section section) {
        deleteCalled = true;

    }
}
