package tech.zerofiltre.blog.doubles;

import org.springframework.data.domain.*;
import org.springframework.data.repository.query.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;
import java.util.function.*;

public class CreatorLessonJPANumberRepository implements LessonJPANumberRepository {
    @Override
    public List<LessonJPANumber> findAll() {
        return null;
    }

    @Override
    public List<LessonJPANumber> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<LessonJPANumber> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<LessonJPANumber> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void delete(LessonJPANumber entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {

    }

    @Override
    public void deleteAll(Iterable<? extends LessonJPANumber> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends LessonJPANumber> S save(S entity) {
        entity.setNumber(1);
        return entity;
    }

    @Override
    public <S extends LessonJPANumber> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<LessonJPANumber> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends LessonJPANumber> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends LessonJPANumber> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<LessonJPANumber> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public LessonJPANumber getOne(Integer integer) {
        return null;
    }

    @Override
    public LessonJPANumber getById(Integer integer) {
        return null;
    }

    @Override
    public <S extends LessonJPANumber> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends LessonJPANumber> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends LessonJPANumber> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends LessonJPANumber> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends LessonJPANumber> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends LessonJPANumber> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends LessonJPANumber, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
