package tech.zerofiltre.blog.domain.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static tech.zerofiltre.blog.domain.error.ErrorMessages.VIDEO_NOT_AVAILABLE_FOR_FREE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    private long id;
    private String title;
    private String content;
    private String summary;
    private String thumbnail;
    private String video;
    private boolean free;
    private String type;
    private long chapterId;
    private int number;
    private List<Resource> resources = new ArrayList<>();
    private boolean notEnrolledAccess;

    public String getVideo() {
        return notEnrolledAccess ? VIDEO_NOT_AVAILABLE_FOR_FREE : video;
    }

    public String getType() {
        if (video != null && !video.isEmpty())
            return "video";
        return "text";
    }

}
