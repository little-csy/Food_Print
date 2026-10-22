package com.foodprint.food.service;

import com.foodprint.food.dto.ManualMealLogRequest;
import com.foodprint.food.dto.MealItemDTO;
import com.foodprint.food.dto.WeeklyOverviewResponse;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.food.entity.NutritionData;
import com.foodprint.food.repository.FoodLogRepository;
import com.foodprint.food.repository.NutritionDataRepository;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import com.foodprint.ai.service.AIRequestRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FoodLoggingServiceTest {

    @Mock
    private FoodLogRepository foodLogRepository;

    @Mock
    private NutritionDataRepository nutritionDataRepository;

    @Mock
    private UserService userService;

    @Mock
    private AIRequestRouter aiRouter;

    @InjectMocks
    private FoodLoggingService foodLoggingService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        mockAuth();
    }

    private void mockAuth() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(userId.toString());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testSaveManualMealLog_SuccessWithNullProtection() {
        // Arrange
        ManualMealLogRequest request = new ManualMealLogRequest();
        request.setTitle("My Manual Meal");
        request.setMealTime(LocalDateTime.now());
        
        List<MealItemDTO> foods = new ArrayList<>();
        MealItemDTO item = new MealItemDTO();
        item.setNameEn("Chicken");
        item.setCalories(500.0);
        // Specifically leave proteinG as NULL to test protection
        item.setProteinG(null); 
        item.setFatG(20.0);
        item.setCarbsG(10.0);
        foods.add(item);
        request.setFoods(foods);

        FoodLog mockSavedLog = new FoodLog();
        mockSavedLog.setId(UUID.randomUUID());
        when(foodLogRepository.save(any(FoodLog.class))).thenReturn(mockSavedLog);

        // Act
        FoodLog result = foodLoggingService.saveManualMealLog(request);

        // Assert
        assertNotNull(result);
        verify(foodLogRepository, times(1)).save(any(FoodLog.class));
        verify(nutritionDataRepository, times(1)).save(any(NutritionData.class));
        
        // Ensure no re-analysis was triggered
        verify(aiRouter, never()).routeText(anyString(), any());
        verify(aiRouter, never()).routeImageUrl(anyString());
    }

    @Test
    void testWeeklyOverview_AggregationLogic() {
        // Arrange
        LocalDate today = LocalDate.now();
        List<FoodLog> logs = new ArrayList<>();
        FoodLog log = new FoodLog();
        log.setId(UUID.randomUUID());
        log.setLogDate(today);
        logs.add(log);

        when(foodLogRepository.findByUserIdAndLogDateBetween(any(), any(), any())).thenReturn(logs);
        
        NutritionData nut = new NutritionData();
        nut.setCalories(1000.0);
        nut.setProteinG(50.0);
        nut.setFatG(30.0);
        nut.setCarbsG(100.0);
        when(nutritionDataRepository.findByFoodLogId(any())).thenReturn(List.of(nut));

        // Act
        WeeklyOverviewResponse response = foodLoggingService.getWeeklyOverview(today);

        // Assert
        assertNotNull(response);
        assertEquals(7, response.getDays().size());
        
        // Find today's aggregate
        response.getDays().stream()
                .filter(d -> d.getDate().equals(today))
                .findFirst()
                .ifPresent(d -> {
                    assertEquals(1000.0, d.getCalories());
                    assertEquals(50.0, d.getProtein());
                    assertEquals(30.0, d.getFat());
                    assertEquals(100.0, d.getCarbs());
                });
    }
}
