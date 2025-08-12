package tech.zerofiltre.blog.domain.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private long id;
    private String chapterExplanations;
    private int chapterSatisfactionScore;
    private int chapterUnderstandingScore;
    private boolean recommendCourse;
    private int overallChapterSatisfaction;
    private String chapterImpressions;
    private String whyRecommendingThisCourse;
    private List<String> favoriteLearningToolOfTheChapter = new ArrayList<>();
    private String reasonFavoriteLearningToolOfTheChapter;
    private String improvementSuggestion;
    private long authorId;
    private long chapterId;
    private long courseId;

}

