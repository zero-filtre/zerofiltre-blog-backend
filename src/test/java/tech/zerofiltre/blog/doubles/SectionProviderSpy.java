package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class SectionProviderSpy implements SectionProvider {
    public boolean findByIdCalled;
    public boolean saveCalled;

    public boolean deleteCalled;
    @Override
    public Optional<Section> findById(long id) {
        findByIdCalled = true;
        return Optional.empty();
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
