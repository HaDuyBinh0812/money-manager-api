package com.example.moneymanager.service;

import com.example.moneymanager.dto.IncomeDTO;
import com.example.moneymanager.entity.CategoryEntity;
import com.example.moneymanager.entity.IncomeEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    //Add a new income to database
    public IncomeDTO addIncome(IncomeDTO incomeDTO){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        CategoryEntity categoryEntity= categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(() ->new RuntimeException("Category not found"));
        IncomeEntity newIncomeEntity = toEntity(incomeDTO, profileEntity, categoryEntity);
        newIncomeEntity = incomeRepository.save(newIncomeEntity);
        return toDTO(newIncomeEntity);
    }

    //Retrieves all income for current month/based on the start date and end date
    public List<IncomeDTO> getCurrentIncomeMonthForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> listIncomeEntity = incomeRepository
                .findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return listIncomeEntity.stream().map(this::toDTO).toList();
    }

    //Delete income by id for current user
    public void deleteIncome(long incomeId){
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
        if(!income.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(income);
    }

    //Get five income for current user
    public List<IncomeDTO> getFiveIncome(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> listIncome = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return listIncome.stream().map(this::toDTO).toList();
    }

    //Get total income for current user
    public BigDecimal getTotalIncome(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    //Filter incomes
    public List<IncomeDTO>  filterIncome (LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        List<IncomeEntity> listIncome =
                incomeRepository
                        .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase
                                (profileEntity.getId(), startDate, endDate,keyword, sort);
        return listIncome.stream().map(this::toDTO).toList();
    }

    //helper method
    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profileEntity, CategoryEntity categoryEntity){
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profileEntity)
                .category(categoryEntity)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity incomeEntity){
        return  IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null )
                .categoryName(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getName() : "N/A" )
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate())
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }
}
