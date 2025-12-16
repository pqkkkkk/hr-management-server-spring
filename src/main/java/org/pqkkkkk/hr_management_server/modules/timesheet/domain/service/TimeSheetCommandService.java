package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;

public interface TimeSheetCommandService {
    // Need for request module when approving check-in/out requests
    public DailyTimeSheet createDailyTimeSheet(DailyTimeSheet dailyTimeSheet);
    // Need for timesheet updation requests
    public DailyTimeSheet updateDailyTimeSheet(DailyTimeSheet dailyTimeSheet);
}
