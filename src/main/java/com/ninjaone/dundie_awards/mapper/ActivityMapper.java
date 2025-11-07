package com.ninjaone.dundie_awards.mapper;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.model.Activity;

public class ActivityMapper {


    public static Activity toEntity(ActivityDto activityDto) {
        Activity activity = new Activity();
        activity.setId(activityDto.getId());
        activity.setEvent(activityDto.getEvent());
        activity.setOccuredAt(activityDto.getOccuredAt());
        return activity;
    }

    public static ActivityDto toDto(Activity activity) {
        ActivityDto activityDto = new ActivityDto();
        activityDto.setId(activity.getId());
        activityDto.setEvent(activity.getEvent());
        activityDto.setOccuredAt(activity.getOccuredAt());
        return activityDto;
    }
}
