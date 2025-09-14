package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.entity.CategoryEntity;
import com.example.moneymanager.entity.ExpenseEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    //Add a new expense to database
    public ExpenseDTO addExpense(ExpenseDTO expenseDTO){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        CategoryEntity categoryEntity= categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(() ->new RuntimeException("Category not found"));
        ExpenseEntity newExpenseEntity = toEntity(expenseDTO, profileEntity, categoryEntity);
        newExpenseEntity = expenseRepository.save(newExpenseEntity);
        return toDTO(newExpenseEntity);
    }

    //Retrieves all expense for current month/based on the start date and end date
    public List<ExpenseDTO> getCurrentExpenseMonthForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> listExpenseEntity = expenseRepository
                .findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return listExpenseEntity.stream().map(this::toDTO).toList();
    }

    //Delete expense by id for current user
    public void deleteExpense(long expenseId){
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        if(!expense.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    //Get latest 5 expenses for current user
    public List<ExpenseDTO> getFiveExpense(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> listExpense = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return listExpense.stream().map(this::toDTO).toList();
    }

    //Get total expense for current user
    public BigDecimal getTotalExpense(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    //Filter expense
    public List<ExpenseDTO>  filterExpense (LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        List<ExpenseEntity> listExpense =
                expenseRepository
                        .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase
                                (profileEntity.getId(), startDate, endDate,keyword, sort);
        return listExpense.stream().map(this::toDTO).toList();
    }

    //Notifications
    public List<ExpenseDTO> getExpensesForCurrentUserOnDate(Long profileId, LocalDate date){
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDTO).toList();
    }

    //helper method
    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profileEntity, CategoryEntity categoryEntity){
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profileEntity)
                .category(categoryEntity)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity expenseEntity){
        return  ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null )
                .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getName() : "N/A" )
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate())
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }
}
