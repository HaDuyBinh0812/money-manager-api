package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendDailyIncomeExpenseReminder(){
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> listProfile = profileRepository.findAll();
        for(ProfileEntity profile : listProfile){
            String body =
                    "Hi " + profile.getFullName() + ",<br><br>"
                            + "This is a friendly reminder to add your income and expenses for today in Money Manager.<br><br>"
                            + "<a href='" + frontendUrl + "' "
                            + "style='display:inline-block; padding:10px 20px; background-color:#4CAF50; color:#fff; "
                            + "text-decoration:none; border-radius:5px; font-weight:bold;'>Go to Money Manager</a>"
                            + "<br><br>Best regards,<br>Money Manager Team";

            emailService.sendEmail(profile.getEmail(), "Daily Reminder: Add your income and expenses", body);
        }
    }

//    @Scheduled(cron = "0 0 23 * * *", zone ="Asia/Ho_Chi_Minh")
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendDailyExpenseExpenseReminder() {
        log.info("Job started: SendDailyExpense()");

        List<ProfileEntity> listProfile = profileRepository.findAll();

        for (ProfileEntity profile : listProfile) {
            List<ExpenseDTO> todayExpense = expenseService.getExpensesForCurrentUserOnDate(
                    profile.getId(), LocalDate.now()
            );

            if (!todayExpense.isEmpty()) {
                StringBuilder table = new StringBuilder();

                // open table
                table.append("<table style='border-collapse: collapse; width:100%;'>");
                table.append("<tr style='background-color:#f2f2f2;'>")
                        .append("<th style='border:1px solid #ddd; padding:8px;'>S.No</th>")
                        .append("<th style='border:1px solid #ddd; padding:8px;'>Name</th>")
                        .append("<th style='border:1px solid #ddd; padding:8px;'>Amount</th>")
                        .append("<th style='border:1px solid #ddd; padding:8px;'>Category</th>")
                        .append("<th style='border:1px solid #ddd; padding:8px;'>Date</th>")
                        .append("</tr>");

                // add expense
                int i = 1;
                for (ExpenseDTO expense : todayExpense) {
                    table.append("<tr>")
                            .append("<td style='border:1px solid #ddd; padding:8px;'>").append(i++).append("</td>")
                            .append("<td style='border:1px solid #ddd; padding:8px;'>").append(expense.getName()).append("</td>")
                            .append("<td style='border:1px solid #ddd; padding:8px;'>").append(expense.getAmount()).append("</td>")
                            .append("<td style='border:1px solid #ddd; padding:8px;'>").append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A").append("</td>")
                            .append("<td style='border:1px solid #ddd; padding:8px;'>").append(expense.getDate()).append("</td>")
                            .append("</tr>");
                }

                // close table
                table.append("</table>");

                // body email
                String body = "Hi " + profile.getFullName() + ",<br><br>"
                        + "Here is a summary of your expenses for today:<br><br>"
                        + table
                        + "<br><br>Best regards,<br>Money Manager Team";

                // send email
                emailService.sendEmail(profile.getEmail(),
                        "Daily Expense Summary",
                        body);

            }
        }
    }

}
