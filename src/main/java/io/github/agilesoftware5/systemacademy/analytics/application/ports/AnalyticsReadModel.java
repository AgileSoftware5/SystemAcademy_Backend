package io.github.agilesoftware5.systemacademy.analytics.application.ports;

import io.github.agilesoftware5.systemacademy.analytics.application.dto.AnalyticsFilter;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport;

import java.time.LocalDate;

public interface AnalyticsReadModel {

    OverviewReport overview(AnalyticsFilter filter, LocalDate from, LocalDate to);
}
