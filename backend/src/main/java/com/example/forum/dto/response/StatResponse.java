package com.example.forum.dto.response;

public class StatResponse {
    private long totalPostCount;
    private long todayPostCount;
    private long pendingPostCount;
    private long publishedPostCount;
    private long totalUserCount;
    private long activeUserCount;
    private long bannedUserCount;
    private long adminUserCount;
    private long todayWarningCount;
    private long pendingWarningCount;
    private long totalAuditCount;
    private long positivePostCount;
    private long neutralPostCount;
    private long negativePostCount;

    public long getTotalPostCount() { return totalPostCount; }
    public void setTotalPostCount(long totalPostCount) { this.totalPostCount = totalPostCount; }
    public long getTodayPostCount() { return todayPostCount; }
    public void setTodayPostCount(long todayPostCount) { this.todayPostCount = todayPostCount; }
    public long getPendingPostCount() { return pendingPostCount; }
    public void setPendingPostCount(long pendingPostCount) { this.pendingPostCount = pendingPostCount; }
    public long getPublishedPostCount() { return publishedPostCount; }
    public void setPublishedPostCount(long publishedPostCount) { this.publishedPostCount = publishedPostCount; }
    public long getTotalUserCount() { return totalUserCount; }
    public void setTotalUserCount(long totalUserCount) { this.totalUserCount = totalUserCount; }
    public long getActiveUserCount() { return activeUserCount; }
    public void setActiveUserCount(long activeUserCount) { this.activeUserCount = activeUserCount; }
    public long getBannedUserCount() { return bannedUserCount; }
    public void setBannedUserCount(long bannedUserCount) { this.bannedUserCount = bannedUserCount; }
    public long getAdminUserCount() { return adminUserCount; }
    public void setAdminUserCount(long adminUserCount) { this.adminUserCount = adminUserCount; }
    public long getTodayWarningCount() { return todayWarningCount; }
    public void setTodayWarningCount(long todayWarningCount) { this.todayWarningCount = todayWarningCount; }
    public long getPendingWarningCount() { return pendingWarningCount; }
    public void setPendingWarningCount(long pendingWarningCount) { this.pendingWarningCount = pendingWarningCount; }
    public long getTotalAuditCount() { return totalAuditCount; }
    public void setTotalAuditCount(long totalAuditCount) { this.totalAuditCount = totalAuditCount; }
    public long getPositivePostCount() { return positivePostCount; }
    public void setPositivePostCount(long positivePostCount) { this.positivePostCount = positivePostCount; }
    public long getNeutralPostCount() { return neutralPostCount; }
    public void setNeutralPostCount(long neutralPostCount) { this.neutralPostCount = neutralPostCount; }
    public long getNegativePostCount() { return negativePostCount; }
    public void setNegativePostCount(long negativePostCount) { this.negativePostCount = negativePostCount; }
}
