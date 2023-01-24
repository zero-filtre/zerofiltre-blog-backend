package tech.zerofiltre.blog.doubles;

import org.springframework.data.domain.*;
import org.springframework.data.repository.query.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;
import java.util.function.*;

public class DummyLessonJPARepository implements LessonJPARepository {
    @Override
    public List<LessonJPA> findAll() {
        return null;
    }

    @Override
    public List<LessonJPA> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<LessonJPA> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<LessonJPA> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(LessonJPA entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends LessonJPA> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends LessonJPA> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends LessonJPA> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<LessonJPA> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends LessonJPA> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends LessonJPA> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<LessonJPA> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public LessonJPA getOne(Long aLong) {
        return null;
    }

    @Override
    public LessonJPA getById(Long aLong) {
        return null;
    }

    @Override
    public <S extends LessonJPA> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends LessonJPA> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends LessonJPA> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends LessonJPA> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends LessonJPA> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends LessonJPA> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends LessonJPA, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
