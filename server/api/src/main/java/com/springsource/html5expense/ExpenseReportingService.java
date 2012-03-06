/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense;

import java.util.List;

/**
 * Manages user expense reports.
 *
 * @author Keith Donald
 * @author Josh Long
 */
public interface ExpenseReportingService{

    void deleteExpenseReport(Long expenseReportId);

    void updateExpenseReportPurpose(Long reportId, String title);

    /**
     * Creates a new expense report.
     *
     * @param purpose the purpose for this report, e.g., "Palo Alto Face to Face Meeting"
     * @return the unique ID of the expense report
     */
    Long createReport(String purpose);

    /**
     * Submit the expense report for approval.
     *
     * @param reportId the id of the report to file
     */
    void submitReport(Long reportId);

    /**
     * Returns all the expense reports the user has open.
     * An open report is not under review and is not closed.
     * It can be edited by the user and {@link #submitReport(Long) submitted}.
     *
     * @return the user's open expense reports
     */
    List<ExpenseReport> getOpenReports();

    /**
     * Returns all the expense reports the user has submitted.
     * A submitted report is under review or approved.
     *
     * @return the user's submitted expense reports
     */
    List<ExpenseReport> getSubmittedReports();

    ExpenseReport getExpenseReport(Long reportId);

}