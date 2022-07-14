package tech.zerofiltre.blog.infra.providers.database;

import tech.zerofiltre.blog.domain.*;

public class SpringPageMapper<T> {

    public Page<T> fromSpringPage(org.springframework.data.domain.Page<T> page){
        Page<T> result = new Page<>();

        result.setPageSize(page.getSize());
        result.setPageNumber(page.getNumber());
        result.setContent(page.getContent());
        result.setHasNext(page.hasNext());
        result.setHasPrevious(page.hasPrevious());
        result.setTotalNumberOfPages(page.getTotalPages());
        result.setNumberOfElements(page.getNumberOfElements());
        result.setTotalNumberOfElements(page.getTotalElements());
        return result;

    }
}
