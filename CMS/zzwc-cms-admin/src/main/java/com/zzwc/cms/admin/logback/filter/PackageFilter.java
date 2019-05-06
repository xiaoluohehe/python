package com.zzwc.cms.admin.logback.filter;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class PackageFilter extends AbstractMatcherFilter<ILoggingEvent> {

    String packageNames = "com.zzwc.cms.admin";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // 这里不应该每次都split
        for (String packageName : packageNames.split(",")) {
            if (event.getLoggerName().indexOf(packageName) != -1) {
                return onMatch;
            }
        }

        return onMismatch;
    }

    public String getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames;
    }

}