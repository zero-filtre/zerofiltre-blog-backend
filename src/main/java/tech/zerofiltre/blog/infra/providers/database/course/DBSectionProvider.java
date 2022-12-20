package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;

import java.util.*;
@Component
@Transactional
@RequiredArgsConstructor
public class DBSectionProvider implements SectionProvider {

    private final SectionJPARepository sectionJPARepository;

    private final SectionJPAMapper mapper = Mappers.getMapper(SectionJPAMapper.class);
    @Override
    public Optional<Section> findById(long id) {
        return sectionJPARepository.findById(id)
                .map(mapper::fromJPA);
    }

    @Override
    public Section save(Section section) {
        return mapper.fromJPA(sectionJPARepository.save(mapper.toJPA(section)));
    }

    @Override
    public void delete(Section section) {
        sectionJPARepository.delete(mapper.toJPA(section));
    }
}
